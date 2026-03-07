package Resume.Analyser.AI.Resume.Analyser.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Service
public class FileParserService {

    /**
     * Extract text from raw file bytes + metadata from the MultipartFile.
     * Uses bytes so the caller can read the file only once.
     */
    public String extractTextFromBytes(byte[] fileBytes, String contentType, String originalFilename)
            throws IOException {
        String fname = originalFilename != null ? originalFilename.toLowerCase() : "";

        if (isPdf(contentType, fname)) {
            return extractFromPdfBytes(fileBytes);
        } else if (isDocx(contentType, fname)) {
            return extractFromDocxBytes(fileBytes);
        } else if (isPlainText(contentType, fname)) {
            return cleanText(new String(fileBytes));
        } else {
            throw new IllegalArgumentException(
                    "Unsupported file type. Please upload PDF, DOCX, or TXT files.");
        }
    }

    /**
     * Convenience overload that reads bytes from the MultipartFile itself.
     * Only safe to call BEFORE transferTo() is ever called on the file.
     */
    public String extractText(MultipartFile file) throws IOException {
        return extractTextFromBytes(
                file.getBytes(),
                file.getContentType(),
                file.getOriginalFilename());
    }

    public String getFileType(String contentType, String originalFilename) {
        String fname = originalFilename != null ? originalFilename.toLowerCase() : "";
        if (isPdf(contentType, fname)) return "PDF";
        if (isDocx(contentType, fname)) return "DOCX";
        if (isPlainText(contentType, fname)) return "TXT";
        return "UNKNOWN";
    }

    // ---- private helpers ----

    private boolean isPdf(String contentType, String filename) {
        return "application/pdf".equalsIgnoreCase(contentType) || filename.endsWith(".pdf");
    }

    private boolean isDocx(String contentType, String filename) {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                .equalsIgnoreCase(contentType)
                || "application/msword".equalsIgnoreCase(contentType)
                || filename.endsWith(".docx")
                || filename.endsWith(".doc");
    }

    private boolean isPlainText(String contentType, String filename) {
        return "text/plain".equalsIgnoreCase(contentType) || filename.endsWith(".txt");
    }

    private String extractFromPdfBytes(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return cleanText(stripper.getText(document));
        }
    }

    private String extractFromDocxBytes(byte[] bytes) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : paragraphs) {
                String text = p.getText();
                if (text != null && !text.trim().isEmpty()) {
                    sb.append(text).append("\n");
                }
            }
            return cleanText(sb.toString());
        }
    }

    private String cleanText(String text) {
        if (text == null) return "";
        return text
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{4,}", "\n\n\n")
                .trim();
    }
}
