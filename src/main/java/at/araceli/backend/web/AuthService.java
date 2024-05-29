package at.araceli.backend.web;

import at.araceli.backend.db.UserRepository;
import at.araceli.backend.pojos.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

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

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe


    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    private Optional<User> isValidUser(String token, Integer tokenExpiresIn) throws GeneralSecurityException, IOException {
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory factory = new GsonFactory();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, factory)
                .setAudience(Collections.singletonList("367685876658-85cfq004p82u8jbv1km7869osee3piip.apps.googleusercontent.com"))
                .build();

        GoogleIdToken idToken = verifier.verify(token);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
//            boolean emailVerified = payload.getEmailVerified();
//            String locale = (String) payload.get("locale");
//            String familyName = (String) payload.get("family_name");
//            String givenName = (String) payload.get("given_name");

            User user = new User();
            user.setEmail(email);
            user.setUsername(name);
            user.setToken(generateNewToken());
            user.setTokenExpiresAt(LocalDateTime.now().plusDays(tokenExpiresIn));
            user.setImageUrl(pictureUrl);

            return Optional.of(user);
        }

        return Optional.empty();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        Optional<User> optionalUser = Optional.empty();

        try {
            optionalUser = isValidUser(token, 30);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
//            throw new RuntimeException(e);
        }

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body("Not a valid Google account!");
        }

        User user = optionalUser.get();
        optionalUser = userRepo.findByEmail(user.getEmail());
        if (optionalUser.isPresent()) {

            User userInDb = optionalUser.get();
            if (!userInDb.getTokenExpiresAt().isAfter(LocalDateTime.now())) {
                userInDb.setToken(user.getToken());
                userInDb.setTokenExpiresAt(user.getTokenExpiresAt());
                
                userRepo.save(userInDb);
            }

            return ResponseEntity.status(200).body(optionalUser.get().getToken());
        }

        userRepo.save(user);

        return ResponseEntity.status(200).body(user.getToken());
    }
}
