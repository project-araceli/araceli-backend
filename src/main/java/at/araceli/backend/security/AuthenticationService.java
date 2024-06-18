package at.araceli.backend.security;

import at.araceli.backend.conf.JWTService;
import at.araceli.backend.db.UserRepository;
import at.araceli.backend.io.IOAccess;
import at.araceli.backend.pojos.User;
import at.araceli.backend.pojos.enums.Role;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

/**
 * Project: araceli-backend
 * Created by: Michael Hütter
 * Created at: 16.06.2024
 */

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Check if user is a valid Google User and extract User data
     * @param request
     * @return Optional<User>
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private Optional<User> googleAuthenticate(GoogleAuthenticationRequest request) throws GeneralSecurityException, IOException {
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory factory = new GsonFactory();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, factory)
                .setAudience(Collections.singletonList("367685876658-85cfq004p82u8jbv1km7869osee3piip.apps.googleusercontent.com"))
                .build();

        GoogleIdToken idToken = verifier.verify(request.getToken());
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();


            // Get profile information from payload
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            User user = new User();
            user.setEmail(email);
            user.setUsername(name);
            user.setRole(Role.USER);
            user.setImageUrl(pictureUrl);

            return Optional.of(user);
        }

        return Optional.empty();
    }

    /**
     * Logs in a User if already existing and creates new one if not.
     * @param request
     * @return AuthenticationResponse
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public AuthenticationResponse authenticateWithGoogle(GoogleAuthenticationRequest request) throws GeneralSecurityException, IOException {

        Optional<User> optionalUser = googleAuthenticate(request);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<User> existingUser = userRepo.findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                String jwtToken = jwtService.generateToken(existingUser.get());
                return new AuthenticationResponse(jwtToken);
            }

            IOAccess.createFolderStructureForNewUser(user);

            userRepo.save(user);
            String jwt = jwtService.generateToken(user);
            return new AuthenticationResponse(jwt);
        }

        return null;
    }

    /**
     * Creates a User and their folder Structure.
     * @param request
     * @return AuthenticationResponse.
     */
    public AuthenticationResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setImageUrl(request.getImageUrl());
        user.setRole(Role.USER);
        userRepo.save(user);

        IOAccess.createFolderStructureForNewUser(user);

        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);

    }

    /**
     * Verifies whether User is valid.
     * @param request
     * @return AuthenticationResponse
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepo.findByUsername(request.getUsername()).orElseThrow();

        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }
}
