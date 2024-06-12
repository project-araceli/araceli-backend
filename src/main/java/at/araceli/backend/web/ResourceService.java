package at.araceli.backend.web;

import at.araceli.backend.db.ResourceRepository;
import at.araceli.backend.db.UserRepository;
import at.araceli.backend.io.IOAccess;
import at.araceli.backend.pojos.*;
import at.araceli.backend.pojos.enums.ResourceType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private final UserRepository userRepo;
    private final ResourceRepository resourceRepo;

    // TODO: remove after tests
    @PostConstruct
    public void test() {
        User user = new User();
        user.setUsername("test");
        user.setToken("TOKEN");
        user.setTokenExpiresAt(LocalDateTime.now().plusHours(1));
        userRepo.save(user);
        IOAccess.createFolderStructureForNewUser(user);
        // userRepo.save(new User(null, "test", "test@test.com", "", "", null, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    }

    @GetMapping
    public ResponseEntity<Iterable<Resource>> getOwnResources(@RequestHeader(name = "Authorization") String auth) {
        User user = userRepo.findByToken(auth).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Resource> resourceList = resourceRepo.findAllRootElementsByUserId(user.getUserId());

        if (resourceList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(resourceList);
    }

    @GetMapping("/shared")
    public ResponseEntity<Iterable<Resource>> getSharedResourcesByUserId(@RequestParam Long userId) {
        User user = userRepo.findById(userId).orElse(null);

        if (user != null) {
            return ResponseEntity.ok(user.getSharedResources().stream().map(SharedResource::getResource).toList());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Resource> createResource(@RequestHeader(name = "Authorization") String auth,
                                                   @RequestParam(required = false) MultipartFile file, @RequestParam String name,
                                                   @RequestParam String parentId,
                                                   @RequestParam String contentType) {
        User user = userRepo.findByToken(auth).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // get parent from DB
        Resource parent = resourceRepo.findById(parentId).orElse(null);
        if (parent == null && !parentId.equals("root")) {
            return ResponseEntity.badRequest().build();
        }

        Resource resource = new Resource();
        resource.setCreatedAt(LocalDateTime.now());
        resource.setName(name);
        resource.setType(ResourceType.FILE);
        resource.setContentType(contentType);
        if (contentType.equals("FOLDER")) {
            resource.setType(ResourceType.FOLDER);
            resource.setChildren(new ArrayList<>());
            resource.setContentType(null);
        }

        resource.setCreator(user);
        resource.setParent(parent);
        if (!parentId.equals("root")) {
            parent.getChildren().add(resource);
        }
        user.getResources().add(resource);

        if (contentType.equals("FOLDER")) {
            boolean didWriteToFS = IOAccess.writeFolderToFileSystem(resource, resourceRepo);
            if (!didWriteToFS) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } else {
            boolean didWriteToFS = IOAccess.writeFileToFileSystem(resource, file, resourceRepo);
            if (!didWriteToFS) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }

        resourceRepo.save(resource);
        userRepo.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(@PathVariable String id, @RequestHeader(name = "Authorization") String auth) {
        User user = userRepo.findByToken(auth).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Resource> optionalResource = user.getResources().stream().filter(x -> x.getResourceId().equals(id)).findFirst();
        if (optionalResource.isPresent()) {
            ByteArrayResource byteArrayResource = IOAccess.readFileFromFileSystem(optionalResource.get(), resourceRepo);
            Long fileLength = 0L;

            if (byteArrayResource != null) {
                try {
                    File file = IOAccess.getFileByResource(optionalResource.get(), resourceRepo);
                    fileLength = file.length();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return ResponseEntity.ok()
                    .contentLength(fileLength)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(byteArrayResource);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable String id, @RequestHeader(name = "Authorization") String auth) {
        User user = userRepo.findByToken(auth).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Resource resource = resourceRepo.findById(id).orElse(null);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        IOAccess.deleteFileByResource(resource, resourceRepo);

        resourceRepo.delete(resource);

        return ResponseEntity.accepted().build();
    }

}
