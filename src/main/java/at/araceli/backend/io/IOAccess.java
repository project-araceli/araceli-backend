package at.araceli.backend.io;

import at.araceli.backend.db.ResourceRepository;
import at.araceli.backend.pojos.Resource;
import at.araceli.backend.pojos.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut
 * Created at: 22.05.24
 */

@Slf4j
public class IOAccess {

    public static String FILE_DIRECTORY;

    public static boolean writeFileToFileSystem(Resource resource, MultipartFile multipartFile, ResourceRepository resourceRepo) {
        try (InputStream is = multipartFile.getInputStream()) {
            log.info(getFilePathByResource(resource, resourceRepo));
            Path path = Path.of(getFilePathByResource(resource, resourceRepo));
            Files.write(path, is.readAllBytes());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean createFolderStructureForNewUser(User user) {
        try {
            Files.createDirectory(Paths.get(FILE_DIRECTORY + File.separator + user.getUsername()));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static ByteArrayResource readFileFromFileSystem(Resource resource, ResourceRepository resourceRepo) {
        Path path = Paths.get(IOAccess.getFilePathByResource(resource, resourceRepo));
        ByteArrayResource resourceAsByteArray;
        try {
            resourceAsByteArray = new ByteArrayResource(Files.readAllBytes(path));
            return resourceAsByteArray;
        } catch (IOException e) {
            return null;
        }
    }

    public static File getFileByResource(Resource resource, ResourceRepository resourceRepo) {
        Path path = Paths.get(IOAccess.getFilePathByResource(resource, resourceRepo));
        return new File(path.toFile().getAbsolutePath());
    }

    public static String getFilePathByResource(Resource resource, ResourceRepository resourceRepo) {
        List<String> resourcePaths = new ArrayList<>();

        while (resource != null && resource.getParent() != null) {
            resourcePaths.add(resource.getName());
            resource = resourceRepo.findById(resource.getParent().getResourceId()).orElse(null);
        }
        Collections.reverse(resourcePaths);
        resourcePaths.add(0, resource.getCreator().getUsername());
        resourcePaths.add(0, FILE_DIRECTORY);
        resourcePaths.add(resource.getName());
        return String.join(File.separator, resourcePaths);
    }

    public static void setFileDirectory(String fileDirectory) {
        if (FILE_DIRECTORY == null) {
            FILE_DIRECTORY = fileDirectory;
        }
    }
}
