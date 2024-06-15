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
        // TODO: add this line to register function, must be executed for every new user
        IOAccess.createFolderStructureForNewUser(user);
    }

    @GetMapping
    public ResponseEntity<Iterable<Resource>> getOwnResources(@RequestHeader(name = "Authorization") String auth, @RequestParam(required = false) String search, @RequestParam(required = false) String fileExtension) {
        User user = userRepo.findByToken(auth).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Resource> resourceList = resourceRepo.findAllRootElementsByUserId(user.getUserId());

        if (search != null && !search.isBlank() && fileExtension != null && !fileExtension.isBlank()) {
            resourceList.retainAll(resourceRepo.findAllByUserIdAndLikeName(user.getUserId(), search));
            resourceList.retainAll(resourceRepo.findAllByUserAndFileExtension(user.getUserId(), fileExtension.toUpperCase()));
        } else if (search != null && !search.isBlank()) {
            resourceList = resourceRepo.findAllByUserIdAndLikeName(user.getUserId(), search);
        } else if (fileExtension != null && !fileExtension.isBlank()) {
            resourceList = resourceRepo.findAllByUserAndFileExtension(user.getUserId(), fileExtension.toUpperCase());
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
            boolean didWriteToFS = IOAccess.writeFolderToFileSystem(resource);
            if (!didWriteToFS) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } else {
            boolean didWriteToFS = IOAccess.writeFileToFileSystem(resource, file);
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
            ByteArrayResource byteArrayResource = IOAccess.readFileFromFileSystem(optionalResource.get());
            Long fileLength = 0L;

            if (byteArrayResource != null) {
                try {
                    File file = IOAccess.getFileByResource(optionalResource.get());
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

        IOAccess.deleteFileByResource(resource);

        resourceRepo.delete(resource);

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<String> getResourcePath(@PathVariable String id, @RequestHeader(name = "Authorization") String auth) {
        User user = userRepo.findByToken(auth).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Resource resource = resourceRepo.findById(id).orElse(null);
        if (resource == null || !resource.getCreator().getUserId().equals(user.getUserId())) {
            return ResponseEntity.badRequest().build();
        }

        String path = "/" + IOAccess.getRelativeFilePathByResource(resource);
        if (!File.separator.equals("/")) {
            path = path.replaceAll("\\\\", "/");
        }
        return ResponseEntity.ok(path);
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<Resource> updateResourceName(@PathVariable String id, @RequestParam String name, @RequestHeader(name = "Authorization") String auth) {
        User user = userRepo.findByToken(auth).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Resource resource = resourceRepo.findById(id).orElse(null);
        if (resource == null || !resource.getCreator().getUserId().equals(user.getUserId())) {
            return ResponseEntity.notFound().build();
        }

        boolean isSuccessful = IOAccess.renameFile(resource, name);
        if (!isSuccessful) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        resource.setName(name);
        resourceRepo.save(resource);

        return ResponseEntity.ok().body(resource);
    }

    @PatchMapping("/{id}/path")
    public ResponseEntity<Resource> changeResourcePath(@PathVariable String id, @RequestParam String newParentId, @RequestHeader(name = "Authorization") String auth) {
        User user = userRepo.findByToken(auth).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Resource resource = resourceRepo.findById(id).orElse(null);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        Resource oldParentResource = resource.getParent();
        Resource newParentResource = resourceRepo.findById(newParentId).orElse(null);

        if (!newParentId.equals("root") && newParentResource == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!resource.getCreator().getUserId().equals(user.getUserId()) || (newParentResource != null && !newParentResource.getCreator().getUserId().equals(user.getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (oldParentResource == newParentResource) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        boolean isSuccessful = IOAccess.moveFile(resource, newParentResource);
        if (!isSuccessful) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (oldParentResource == null) {
            user.getResources().remove(resource);
            log.info(user.toString());
        } else {
            oldParentResource.getChildren().remove(resource);
            log.info("oldParentResource#1: {}", oldParentResource);
        }

        resource.setParent(newParentResource);
        log.info("newParentResource#1: {}", newParentResource);

        if (newParentResource == null) {
            user.getResources().add(resource);
            log.info(user.toString());
        } else {
            newParentResource.getChildren().add(resource);
            log.info("newParentResource#2: {}", newParentResource);
        }

        if (oldParentResource != null) {
            log.info("OLD SAVED LOL");
            resourceRepo.save(oldParentResource);
        }
        if (newParentResource == null || oldParentResource == null) {
            log.info("USER SAVED LOL");
            userRepo.save(user);
        }
        if (newParentResource != null) {
            log.info("NEW SAVED LOL");
            resourceRepo.save(newParentResource);
        }

        return ResponseEntity.accepted().body(resource);
    }

}
