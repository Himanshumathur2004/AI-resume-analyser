package Resume.Analyser.AI.Resume.Analyser.controller;

import Resume.Analyser.AI.Resume.Analyser.dto.AuthRequest;
import Resume.Analyser.AI.Resume.Analyser.dto.AuthResponse;
import Resume.Analyser.AI.Resume.Analyser.model.User;
import Resume.Analyser.AI.Resume.Analyser.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, "Username already exists"));
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // Storing raw password for simplicity of the demo
        user.setToken(UUID.randomUUID().toString());
        
        userRepository.save(user);
        
        return ResponseEntity.ok(new AuthResponse(user.getToken(), user.getUsername(), "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(request.getPassword())) {
            User user = userOpt.get();
            user.setToken(UUID.randomUUID().toString()); // Rotate token on login
            userRepository.save(user);
            return ResponseEntity.ok(new AuthResponse(user.getToken(), user.getUsername(), "Login successful"));
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null, null, "Invalid credentials"));
    }
}
