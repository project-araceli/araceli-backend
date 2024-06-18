package at.araceli.backend.web;

import at.araceli.backend.db.UserRepository;
import at.araceli.backend.pojos.User;
import at.araceli.backend.security.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Project: araceli-backend
 * Created by: Michael Hütter
 * Created at: 15.05.2024
 */

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final AuthenticationService service;

    @PostMapping("/googleAuthenticate")
    public ResponseEntity<AuthenticationResponse> googleAuthenticate(@RequestBody GoogleAuthenticationRequest request) {

        AuthenticationResponse authResponse = null;
        try {
            authResponse = service.authenticateWithGoogle(request);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        if (authResponse != null) {
            return ResponseEntity.ok(authResponse);
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

}
