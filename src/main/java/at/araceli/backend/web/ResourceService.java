package at.araceli.backend.web;

import at.araceli.backend.db.ResourceRepository;
import at.araceli.backend.db.SharedResourceRepository;
import at.araceli.backend.db.UserRepository;
import at.araceli.backend.io.IOAccess;
import at.araceli.backend.pojos.*;
import at.araceli.backend.pojos.enums.Permission;
import at.araceli.backend.pojos.enums.ResourceType;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
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

    @GetMapping
    public ResponseEntity<Iterable<Resource>> getOwnResources(HttpServletRequest request, @RequestParam(required = false) String search, @RequestParam(required = false) String fileExtension) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);

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
    public ResponseEntity<Iterable<Resource>> getSharedResourcesByUserId(HttpServletRequest request) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);

        if (user != null) {
            return ResponseEntity.ok(user.getSharedResources().stream().map(SharedResource::getResource).toList());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Resource> createResource(HttpServletRequest request,
                                                   @RequestParam(required = false) MultipartFile file, @RequestParam String name,
                                                   @RequestParam String parentId,
                                                   @RequestParam String contentType) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
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

    @PatchMapping("/share/{username}")
    public ResponseEntity<Resource> shareResource(HttpServletRequest request, @PathVariable String username, @RequestParam String id) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Resource> optionalResource = user.getResources().stream().filter(x -> x.getResourceId().equals(id)).findFirst();
        if (optionalResource.isPresent()) {
            User userForSharing = userRepo.findByUsername(username).orElse(null);
            if (userForSharing == null) {
                return ResponseEntity.notFound().build();
            }
            SharedResource sharedResource = new SharedResource(new SharedResourceId(id, userForSharing.getUserId()), optionalResource.get(), userForSharing, Permission.READ);
            userForSharing.getSharedResources().add(sharedResource);
            userRepo.save(userForSharing);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(HttpServletRequest request, @PathVariable String id) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
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
                    return ResponseEntity.badRequest().build();
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
    public ResponseEntity<?> deleteResource(HttpServletRequest request, @PathVariable String id) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Resource resource = resourceRepo.findById(id).orElse(null);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        IOAccess.deleteFileByResource(resource);

        resource.setParent(null);
        resource.setCreator(null);
        resourceRepo.save(resource);
        resourceRepo.deleteById(id);
        resourceRepo.delete(resource);

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<String> getResourcePath(HttpServletRequest request, @PathVariable String id) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
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

    @PatchMapping("/{id}")
    public ResponseEntity<Resource> updateResourceName(HttpServletRequest request, @PathVariable String id, @RequestParam(required = false) String name, @RequestParam(required = false) String description) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Resource resource = resourceRepo.findById(id).orElse(null);
        if (resource == null || !resource.getCreator().getUserId().equals(user.getUserId())) {
            return ResponseEntity.notFound().build();
        }

        if (name != null) {
            boolean isSuccessful = IOAccess.renameFile(resource, name);
            if (!isSuccessful) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            resource.setName(name);
        }
        if (description != null) {
            resource.setDescription(description);
        }

        resourceRepo.save(resource);

        return ResponseEntity.ok().body(resource);
    }

    @PatchMapping("/{id}/path")
    public ResponseEntity<Resource> changeResourcePath(HttpServletRequest request, @PathVariable String id, @RequestParam String newParentId) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
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
        // moving the file to the same location does not make sense
        if (oldParentResource == newParentResource) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build();
        }

        boolean isSuccessful = IOAccess.moveFile(resource, newParentResource);
        if (!isSuccessful) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (oldParentResource == null) {
            user.getResources().remove(resource);
        } else {
            oldParentResource.getChildren().remove(resource);
        }

        resource.setParent(newParentResource);

        if (newParentResource == null) {
            user.getResources().add(resource);
        } else {
            newParentResource.getChildren().add(resource);
        }

        // I don't know what happens here, but these two log statements make this function work correctly when you try to set the parent to null
        log.info(resource.toString());
        log.info(user.getResources().toString());
        if (oldParentResource != null) {
            resourceRepo.save(oldParentResource);
        }
        if (newParentResource == null || oldParentResource == null) {
            userRepo.save(user);
        }
        if (newParentResource != null) {
            resourceRepo.save(newParentResource);
        }

        return ResponseEntity.accepted().body(resource);
    }

}
