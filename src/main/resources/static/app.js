// ====================================
// ResumeIQ — Frontend JavaScript
// ====================================

const API_BASE = '/api/resume';

// ---- State ----
let selectedFile = null;
let currentAnalysis = null;
let selectedJdFile = null;

// ====================================
// AUTHENTICATION
// ====================================

function getToken() {
    return localStorage.getItem('resumeIqToken');
}

function getAuthHeaders(isFormData = false) {
    const headers = {};
    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    if (!isFormData) {
        headers['Content-Type'] = 'application/json';
    }
    return headers;
}

function checkAuth() {
    const token = getToken();
    if (!token) {
        showSection('auth');
        document.getElementById('nav-logout').style.display = 'none';
        document.getElementById('nav-history').style.display = 'none';
        return false;
    } else {
        document.getElementById('nav-logout').style.display = 'block';
        document.getElementById('nav-history').style.display = 'block';
        return true;
    }
}

function toggleAuthForm() {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    if (loginForm.style.display === 'none') {
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
    }
}

async function login() {
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    if (!username || !password) return showToast('Please fill all fields', 'error');

    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (res.ok) {
            localStorage.setItem('resumeIqToken', data.token);
            clearState(); // Clear any previous user's leftovers
            showToast('Login successful!', 'success');
            checkAuth();
            showSection('upload');
            loadDashboardStats(); // Refresh stats for the new user
        } else {
            showToast(data.message || 'Login failed', 'error');
        }
    } catch (err) {
        showToast('Error connecting to server', 'error');
    }
}

async function register() {
    const username = document.getElementById('reg-username').value;
    const password = document.getElementById('reg-password').value;
    if (!username || !password) return showToast('Please fill all fields', 'error');

    try {
        const res = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (res.ok) {
            localStorage.setItem('resumeIqToken', data.token);
            clearState();
            showToast('Registration successful!', 'success');
            checkAuth();
            showSection('upload');
            loadDashboardStats();
        } else {
            showToast(data.message || 'Registration failed', 'error');
        }
    } catch (err) {
        showToast('Error connecting to server', 'error');
    }
}

function logout() {
    localStorage.removeItem('resumeIqToken');
    clearState();
    showSection('auth');
    checkAuth();
    showToast('Logged out successfully', 'success');
}

function clearState() {
    selectedFile = null;
    selectedJdFile = null;
    currentAnalysis = null;

    // Clear Resume preview
    const resumePreview = document.getElementById('file-preview');
    if (resumePreview) resumePreview.classList.add('hidden');
    const resumeInput = document.getElementById('file-input');
    if (resumeInput) resumeInput.value = '';

    // Clear JD preview and text
    clearJdFile();
    const jdText = document.getElementById('jd-text');
    if (jdText) jdText.value = '';

    // Reset buttons
    const analyseBtn = document.getElementById('analyse-btn');
    if (analyseBtn) analyseBtn.disabled = true;

    // Reset dashboard stats displays to placeholder
    const statTotal = document.getElementById('stat-total');
    if (statTotal) statTotal.textContent = '0';
    const statAvg = document.getElementById('stat-avg');
    if (statAvg) statAvg.textContent = '—';
}

// ====================================
// SECTION NAVIGATION
// ====================================
function showSection(name) {
    if (name !== 'auth' && !checkAuth()) {
        return; // checkAuth will automatically redirect to auth section
    }

    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));

    const sectionMap = {
        'auth': 'section-auth',
        'upload': 'section-upload',
        'results': 'section-results',
        'history': 'section-history',
        'about': 'section-about'
    };
    const navMap = {
        'auth': 'nav-home',
        'upload': 'nav-home',
        'results': 'nav-home',
        'history': 'nav-history',
        'about': 'nav-about'
    };

    const sectionEl = document.getElementById(sectionMap[name]);
    if (sectionEl) sectionEl.classList.add('active');

    if (name !== 'auth') {
        const navEl = document.getElementById(navMap[name]);
        if (navEl) navEl.classList.add('active');
    }

    if (name === 'history') loadHistory();

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ====================================
// FILE HANDLING
// ====================================
function handleFileSelect(event) {
    const file = event.target.files[0];
    if (file) setFile(file);
}

function handleDragOver(event) {
    event.preventDefault();
    document.getElementById('upload-zone').classList.add('drag-over');
}

function handleDragLeave(event) {
    document.getElementById('upload-zone').classList.remove('drag-over');
}

function handleDrop(event) {
    event.preventDefault();
    document.getElementById('upload-zone').classList.remove('drag-over');
    const file = event.dataTransfer.files[0];
    if (file) setFile(file);
}

function setFile(file) {
    const allowedTypes = ['application/pdf',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/msword', 'text/plain'];
    const allowedExts = ['.pdf', '.docx', '.doc', '.txt'];
    const ext = '.' + file.name.split('.').pop().toLowerCase();

    if (!allowedTypes.includes(file.type) && !allowedExts.includes(ext)) {
        showToast('❌ Unsupported file type. Please upload PDF, DOCX, or TXT.', 'error');
        return;
    }
    if (file.size > 10 * 1024 * 1024) {
        showToast('❌ File too large. Maximum size is 10MB.', 'error');
        return;
    }

    selectedFile = file;
    showFilePreview(file);
    document.getElementById('analyse-btn').disabled = false;
}

function showFilePreview(file) {
    document.getElementById('file-preview').classList.remove('hidden');
    document.getElementById('file-preview-name').textContent = file.name;
    document.getElementById('file-preview-size').textContent = formatFileSize(file.size);

    // Set icon color based on file type
    const iconEl = document.getElementById('file-preview-icon');
    const ext = file.name.split('.').pop().toLowerCase();
    const colors = { pdf: '#ef4444', docx: '#3b82f6', doc: '#3b82f6', txt: '#10b981' };
    iconEl.style.background = `linear-gradient(135deg, ${colors[ext] || '#6366f1'}, ${shadeColor(colors[ext] || '#6366f1', -30)})`;
}

function clearFile() {
    selectedFile = null;
    document.getElementById('file-input').value = '';
    document.getElementById('file-preview').classList.add('hidden');
    document.getElementById('analyse-btn').disabled = true;
}

function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function shadeColor(color, percent) {
    const num = parseInt(color.replace('#', ''), 16);
    const amt = Math.round(2.55 * percent);
    const R = Math.max(0, Math.min(255, (num >> 16) + amt));
    const G = Math.max(0, Math.min(255, ((num >> 8) & 0x00FF) + amt));
    const B = Math.max(0, Math.min(255, (num & 0x0000FF) + amt));
    return '#' + [R, G, B].map(v => v.toString(16).padStart(2, '0')).join('');
}

// ====================================
// ANALYSE
// ====================================
async function analyseResume() {
    if (!selectedFile) return;

    const btn = document.getElementById('analyse-btn');
    const btnText = btn.querySelector('.analyse-btn-text');
    const btnLoading = btn.querySelector('.analyse-btn-loading');
    const progressCard = document.getElementById('progress-card');

    // UI → loading state
    btn.disabled = true;
    btnText.classList.add('hidden');
    btnLoading.classList.remove('hidden');
    progressCard.classList.remove('hidden');

    setProgressStep(1, 25, 'Uploading your resume...');

    const formData = new FormData();
    formData.append('file', selectedFile);

    if (selectedJdFile) {
        formData.append('jdFile', selectedJdFile);
    } else {
        const jdText = document.getElementById('jd-text').value;
        if (jdText && jdText.trim().length > 0) {
            formData.append('jdText', jdText.trim());
        }
    }

    try {
        // Simulate progress steps with timing
        setTimeout(() => setProgressStep(2, 45, 'Extracting text content...'), 800);
        setTimeout(() => setProgressStep(3, 70, 'AI is analysing your resume... This may take 15-30 seconds.'), 2000);

        const response = await fetch(`${API_BASE}/analyse`, {
            method: 'POST',
            headers: getAuthHeaders(true),
            body: formData
        });

        const data = await response.json();

        if (!response.ok || !data.success) {
            throw new Error(data.message || 'Analysis failed. Please try again.');
        }

        setProgressStep(4, 100, 'Report ready!');

        setTimeout(() => {
            currentAnalysis = data.data;
            progressCard.classList.add('hidden');
            renderResults(data.data);
            showSection('results');
            showToast('✅ Resume analysed successfully!', 'success');
        }, 800);

    } catch (err) {
        console.error('Analysis error:', err);
        progressCard.classList.add('hidden');
        showToast(`❌ ${err.message}`, 'error');
    } finally {
        btn.disabled = false;
        btnText.classList.remove('hidden');
        btnLoading.classList.add('hidden');
        resetProgress();
    }
}

function setProgressStep(step, percent, message) {
    // Update progress bar
    document.getElementById('progress-bar').style.width = percent + '%';
    document.getElementById('progress-message').textContent = message;

    // Update step dots
    for (let i = 1; i <= 4; i++) {
        const el = document.getElementById(`step-${i}`);
        el.classList.remove('active', 'done');
        if (i < step) el.classList.add('done');
        else if (i === step) el.classList.add('active');
    }
}

function resetProgress() {
    document.getElementById('progress-bar').style.width = '0%';
    for (let i = 1; i <= 4; i++) {
        const el = document.getElementById(`step-${i}`);
        el.classList.remove('active', 'done');
    }
}

// ====================================
// RENDER RESULTS
// ====================================
function renderResults(data) {
    // Candidate info
    const name = data.candidateName || 'Unknown Candidate';
    const email = data.candidateEmail ? ` — ${data.candidateEmail}` : '';
    document.getElementById('result-candidate-name').textContent = name + email;

    // Score ring animation
    animateScoreRing(data.overallScore || 0);
    document.getElementById('result-score').textContent = data.overallScore || 0;
    document.getElementById('result-grade').textContent = data.grade || '—';

    // Grade badge
    const gradeBadge = document.getElementById('result-grade-badge');
    gradeBadge.textContent = `${data.grade} — ${data.gradeLabel}`;
    gradeBadge.style.background = getGradeGradient(data.overallScore);

    document.getElementById('result-summary').textContent = data.summary || 'Analysis complete.';

    // Score bars with animation
    setTimeout(() => {
        animateBar('bar-ats', 'bar-ats-val', data.atsScore);
        animateBar('bar-skills', 'bar-skills-val', data.skillsScore);
        animateBar('bar-exp', 'bar-exp-val', data.experienceScore);
        animateBar('bar-edu', 'bar-edu-val', data.educationScore);
        animateBar('bar-fmt', 'bar-fmt-val', data.formattingScore);

        const jdContainer = document.getElementById('jd-score-container');
        if (data.jdMatchScore != null && data.jdMatchScore > 0) {
            jdContainer.style.display = 'flex';
            animateBar('bar-jd', 'bar-jd-val', data.jdMatchScore);
        } else {
            jdContainer.style.display = 'none';
        }
    }, 300);

    // Skills
    renderTags('result-skills', data.extractedSkills || [], 'skill-tag');

    // Strengths
    renderList('result-strengths', data.strengths || [], 'strength-list');

    // Weaknesses
    renderList('result-weaknesses', data.weaknesses || [], 'weakness-list');

    // Suggestions
    renderSuggestions('result-suggestions', data.suggestions || []);

    // Keywords
    renderTags('result-keywords-found', data.keywordsFound || [], 'keyword-found-tag');
    renderTags('result-keywords-missing', data.keywordsMissing || [], 'keyword-missing-tag');

    // Interview Questions
    renderSuggestions('result-interview-questions', data.interviewQuestions || []);
}

function animateScoreRing(score) {
    const circle = document.getElementById('score-ring-circle');
    const circumference = 534;
    const offset = circumference - (score / 100) * circumference;

    setTimeout(() => {
        circle.style.transition = 'stroke-dashoffset 1.5s cubic-bezier(0.4, 0, 0.2, 1)';
        circle.style.strokeDashoffset = offset;

        // Change gradient color based on score
        const gradient = document.getElementById('scoreGradient');
        if (score >= 70) {
            gradient.children[0].setAttribute('stop-color', '#10b981');
            gradient.children[1].setAttribute('stop-color', '#06b6d4');
        } else if (score >= 50) {
            gradient.children[0].setAttribute('stop-color', '#f59e0b');
            gradient.children[1].setAttribute('stop-color', '#ef4444');
        }
        // else default indigo/purple stays
    }, 100);

    // Animate number
    animateNumber('result-score', 0, score, 1400);
}

function animateNumber(elementId, from, to, duration) {
    const el = document.getElementById(elementId);
    const start = performance.now();
    function update(timestamp) {
        const elapsed = timestamp - start;
        const progress = Math.min(elapsed / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);
        el.textContent = Math.round(from + (to - from) * eased);
        if (progress < 1) requestAnimationFrame(update);
    }
    requestAnimationFrame(update);
}

function animateBar(barId, valId, score) {
    const bar = document.getElementById(barId);
    const val = document.getElementById(valId);
    const s = score || 0;
    bar.style.width = s + '%';
    animateNumber(valId, 0, s, 1000);
}

function renderTags(containerId, items, tagClass) {
    const container = document.getElementById(containerId);
    if (!items || items.length === 0) {
        container.innerHTML = '<span style="color: var(--text-muted); font-size: 0.85rem;">None detected</span>';
        return;
    }
    container.innerHTML = items.map(item =>
        `<span class="tag ${tagClass}">${escapeHtml(item)}</span>`
    ).join('');
}

function renderList(containerId, items, listClass) {
    const container = document.getElementById(containerId);
    if (!items || items.length === 0) {
        container.innerHTML = '<li style="color: var(--text-muted); font-size: 0.85rem;">None identified</li>';
        return;
    }
    container.innerHTML = items.map(item =>
        `<li>${escapeHtml(item)}</li>`
    ).join('');
}

function renderSuggestions(containerId, suggestions) {
    const container = document.getElementById(containerId);
    if (!suggestions || suggestions.length === 0) {
        container.innerHTML = '<p style="color: var(--text-muted); font-size: 0.85rem;">No suggestions available</p>';
        return;
    }
    container.innerHTML = suggestions.map((s, i) => `
        <div class="suggestion-item">
            <div class="suggestion-number">${i + 1}</div>
            <p class="suggestion-text">${escapeHtml(s)}</p>
        </div>
    `).join('');
}

function getGradeGradient(score) {
    if (score >= 80) return 'linear-gradient(135deg, #10b981, #06b6d4)';
    if (score >= 65) return 'linear-gradient(135deg, #6366f1, #8b5cf6)';
    if (score >= 50) return 'linear-gradient(135deg, #f59e0b, #ef4444)';
    return 'linear-gradient(135deg, #ef4444, #ec4899)';
}

// ====================================
// HISTORY
// ====================================
async function loadHistory() {
    const grid = document.getElementById('history-grid');
    grid.innerHTML = '<div class="loading-placeholder"><div class="spinner large-spinner"></div><p>Loading history...</p></div>';

    try {
        const response = await fetch(`${API_BASE}/history`, {
            headers: getAuthHeaders()
        });
        const data = await response.json();

        if (!data.success || !data.data.length) {
            grid.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">📄</div>
                    <p>No analyses yet. Upload a resume to get started!</p>
                </div>`;
            return;
        }

        grid.innerHTML = data.data.map(item => `
            <div class="history-card" onclick="loadHistoryItem(${item.id})">
                <div class="history-card-header">
                    <div>
                        <div class="history-card-name">${escapeHtml(item.candidateName || 'Unknown Candidate')}</div>
                        <div class="history-card-file">📄 ${escapeHtml(item.originalFileName || 'resume')}</div>
                    </div>
                    <div class="history-score-badge" style="background: ${getGradeGradient(item.overallScore)}">
                        ${item.overallScore ?? '—'}
                    </div>
                </div>
                <div class="history-card-footer">
                    <span class="history-card-date">${formatDate(item.createdAt)}</span>
                    <span class="history-card-grade">Grade: ${item.grade || '—'} · ${item.gradeLabel || ''}</span>
                </div>
            </div>
        `).join('');

    } catch (err) {
        grid.innerHTML = '<div class="loading-placeholder"><p>Failed to load history. Is the backend running?</p></div>';
        console.error('History load error:', err);
    }
}

async function loadHistoryItem(id) {
    try {
        const response = await fetch(`${API_BASE}/${id}`, {
            headers: getAuthHeaders()
        });
        const data = await response.json();
        if (data.success) {
            currentAnalysis = data.data;
            renderResults(data.data);
            showSection('results');
        }
    } catch (err) {
        showToast('❌ Failed to load analysis', 'error');
    }
}

function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

// ====================================
// DASHBOARD STATS
// ====================================
async function loadDashboardStats() {
    try {
        const res = await fetch(`${API_BASE}/stats/dashboard`, {
            headers: getAuthHeaders()
        });
        const data = await res.json();
        if (data.success && data.data) {
            const s = data.data;
            document.getElementById('stat-total').textContent = s.totalAnalyses ?? '0';
            const avg = s.averageScore ? Math.round(s.averageScore) : null;
            document.getElementById('stat-avg').textContent = avg ? `${avg}/100` : '—';
        }
    } catch (e) {
        // silently fail — backend might not be up yet
    }
}

// ====================================
// TOAST
// ====================================
let toastTimer;
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    const msg = document.getElementById('toast-msg');
    const icon = document.getElementById('toast-icon');

    msg.textContent = message;
    const icons = { success: '✅', error: '❌', info: 'ℹ️' };
    icon.textContent = icons[type] || 'ℹ️';

    toast.className = 'toast show';
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove('show'), 4000);
}

// ====================================
// UTILS
// ====================================
function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// ====================================
// JD HANDLING
// ====================================

function handleJdFileSelect(event) {
    const file = event.target.files[0];
    if (file) setJdFile(file);
}

function setJdFile(file) {
    const allowedTypes = ['application/pdf',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/msword', 'text/plain'];
    const allowedExts = ['.pdf', '.docx', '.doc', '.txt'];
    const ext = '.' + file.name.split('.').pop().toLowerCase();

    if (!allowedTypes.includes(file.type) && !allowedExts.includes(ext)) {
        showToast('❌ Unsupported JD file type. Please upload PDF, DOCX, or TXT.', 'error');
        return;
    }
    if (file.size > 5 * 1024 * 1024) {
        showToast('❌ JD file too large. Maximum size is 5MB.', 'error');
        return;
    }

    selectedJdFile = file;
    document.getElementById('jd-file-preview').style.display = 'flex';
    document.getElementById('jd-file-name').textContent = file.name;
    document.getElementById('jd-text').disabled = true;
    document.getElementById('jd-text').placeholder = "JD file selected. Remove it to paste text instead.";
}

function clearJdFile() {
    selectedJdFile = null;
    document.getElementById('jd-file-input').value = '';
    document.getElementById('jd-file-preview').style.display = 'none';

    const jdTextEl = document.getElementById('jd-text');
    if (jdTextEl) {
        jdTextEl.disabled = false;
        jdTextEl.placeholder = "Paste job description text here to get match score and specific keyword suggestions...";
    }
}

// ====================================
// INIT
// ====================================
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    showSection('upload');
    loadDashboardStats();

    // Navbar scroll effect
    window.addEventListener('scroll', () => {
        const navbar = document.getElementById('navbar');
        if (window.scrollY > 20) {
            navbar.style.borderBottomColor = 'rgba(255,255,255,0.1)';
        } else {
            navbar.style.borderBottomColor = 'rgba(255,255,255,0.08)';
        }
    });
});
