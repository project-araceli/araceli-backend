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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDate;
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

    private static final SecureRandom SECURE_RANDOM = new SecureRandom(); //threadsafe
    private static final Base64.Encoder BASE_64_ENCODER = Base64.getUrlEncoder(); //threadsafe
    private static final BCryptPasswordEncoder B_CRYPT_PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        SECURE_RANDOM.nextBytes(randomBytes);
        return BASE_64_ENCODER.encodeToString(randomBytes);
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

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody String username,
                                           @RequestBody String email,
                                           @RequestBody String imageUrl,
                                           @RequestBody String password) {

        Optional<User> userExists = userRepo.findByEmail(email);
        if (userExists.isPresent()) {
            return ResponseEntity.badRequest().body("User already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        if (!imageUrl.isEmpty()) user.setImageUrl(imageUrl);
        user.setPasswordHash(B_CRYPT_PASSWORD_ENCODER.encode(password));
        user.setToken(generateNewToken());
        user.setTokenExpiresAt(LocalDateTime.now().plusDays(30));
        userRepo.save(user);

        return ResponseEntity.ok(user.getToken());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String email, @RequestBody String password) {

        Optional<User> userInDB = userRepo.findByEmail(email);
        if (userInDB.isEmpty()) {
            userInDB = userRepo.findByUsername(email);
        }

        if (userInDB.isPresent()) {
            Optional<String> hashedPasswordOptional = userRepo.getUserPassword(userInDB.get().getUsername());
            if (hashedPasswordOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Use a different login method");
            }

            String hashedPassword = hashedPasswordOptional.get();
            if (!B_CRYPT_PASSWORD_ENCODER.matches(password, hashedPassword)) {
                return ResponseEntity.badRequest().body("Invalid email or password");
            }

            User user = userInDB.get();
            user.setToken(generateNewToken());
            user.setTokenExpiresAt(LocalDateTime.now().plusDays(30));
            userRepo.save(user);

            return ResponseEntity.ok().body(user.getToken());
        }

        return ResponseEntity.badRequest().body("Invalid email or password");
    }

    @PostMapping("/googleAuthenticate")
    public ResponseEntity<String> googleAuthenticate(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
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
            userInDb.setToken(user.getToken());
            userInDb.setTokenExpiresAt(user.getTokenExpiresAt());

            userRepo.save(userInDb);

            return ResponseEntity.status(200).body(userInDb.getToken());
        }

        userRepo.save(user);

        return ResponseEntity.status(200).body(user.getToken());
    }
}
