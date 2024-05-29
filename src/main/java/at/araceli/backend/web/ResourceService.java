package at.araceli.backend.web;

import at.araceli.backend.db.UserRepository;
import at.araceli.backend.io.IOAccess;
import at.araceli.backend.pojos.Resource;
import at.araceli.backend.pojos.SharedResource;
import at.araceli.backend.pojos.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.util.Optional;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut
 * Created at: 22.05.24
 */

@RestController
@RequestMapping("/resource")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class ResourceService {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Iterable<Resource>> getOwnResourcesByUserId(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            return ResponseEntity.ok(user.getResources());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/shared")
    public ResponseEntity<Iterable<Resource>> getSharedResourcesByUserId(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            return ResponseEntity.ok(user.getSharedResources().stream().map(SharedResource::getResource).toList());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Resource> createResource(@RequestHeader(name = "Authorization") String auth, @RequestParam MultipartFile file, @RequestBody Resource resource) {
        Resource parent = resource.getParent();

        if (parent != null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.badRequest().build();
    }

}
