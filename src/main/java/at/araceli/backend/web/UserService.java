package at.araceli.backend.web;

import at.araceli.backend.db.UserRepository;
import at.araceli.backend.pojos.User;
import at.araceli.backend.pojos.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Project: araceli-backend
 * Created by: Michael Hütter
 * Created at: 18.06.2024
 */

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    @GetMapping
    public ResponseEntity<User> getUser(HttpServletRequest request) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/all")
    public ResponseEntity<List<String>> getAllUsers(HttpServletRequest request) {
        List<String> users = userRepo.findAllUsernames();
        users.remove(request.getUserPrincipal().getName());
        return ResponseEntity.ok(users);
    }

}
