package at.araceli.backend.io;

import at.araceli.backend.db.ResourceRepository;
import at.araceli.backend.pojos.Resource;
import at.araceli.backend.pojos.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut
 * Created at: 22.05.24
 */

@Slf4j
public class IOAccess {

    public static String FILE_DIRECTORY;

    public static boolean writeFileToFileSystem(Resource resource, MultipartFile multipartFile) {
        Path path = Path.of(getFilePathByResource(resource));
        if (Files.exists(path)) {
            return false;
        }
        try (InputStream is = multipartFile.getInputStream()) {
            Files.write(path, is.readAllBytes());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean writeFolderToFileSystem(Resource resource) {
        Path path = Path.of(getFilePathByResource(resource));
        if (Files.exists(path)) {
            return false;
        }
        try {
            Files.createDirectory(path);
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

    public static ByteArrayResource readFileFromFileSystem(Resource resource) {
        Path path = Paths.get(IOAccess.getFilePathByResource(resource));
        ByteArrayResource resourceAsByteArray;
        try {
            resourceAsByteArray = new ByteArrayResource(Files.readAllBytes(path));
            return resourceAsByteArray;
        } catch (IOException e) {
            return null;
        }
    }

    public static File getFileByResource(Resource resource) {
        Path path = Paths.get(IOAccess.getFilePathByResource(resource));
        return new File(path.toFile().getAbsolutePath());
    }

    public static boolean deleteFileByResource(Resource resource) {
        Path path = Paths.get(IOAccess.getFilePathByResource(resource));
        try {
            Files.delete(path);
        } catch (DirectoryNotEmptyException e) {
            try {
                FileUtils.deleteDirectory(path.toFile());
            } catch (IOException ex) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static String getFilePathByResource(Resource resource) {
        List<String> resourcePaths = new ArrayList<>();
        String creator = resource.getCreator().getUsername();

        while (resource != null) {
            resourcePaths.add(resource.getName());
            log.info(resourcePaths.toString());
            resource = resource.getParent();
        }
        log.info(resourcePaths.toString());
        Collections.reverse(resourcePaths);
        log.info(resourcePaths.toString());
        resourcePaths.add(0, creator);
        log.info(resourcePaths.toString());
        resourcePaths.add(0, FILE_DIRECTORY);
        log.info(resourcePaths.toString());
        return String.join(File.separator, resourcePaths);
    }

    public static String getRelativeFilePathByResource(Resource resource) {
        List<String> resourcePaths = new ArrayList<>();

        while (resource != null) {
            resourcePaths.add(resource.getName());
            log.info(resourcePaths.toString());
            resource = resource.getParent();
        }
        log.info(resourcePaths.toString());
        Collections.reverse(resourcePaths);
        log.info(resourcePaths.toString());
        return String.join(File.separator, resourcePaths);
    }

    public static boolean moveFile(Resource resource, Resource parentResource) {
        String parentPath = FILE_DIRECTORY + File.separator + resource.getCreator().getUsername();
        if (parentResource != null) {
            parentPath = IOAccess.getFilePathByResource(parentResource);
        }
        try {
            if (Files.exists(Paths.get(parentPath + File.separator + resource.getName()))) {
                return false;
            }
            Files.write(Paths.get(parentPath + File.separator + resource.getName()), Objects.requireNonNull(IOAccess.readFileFromFileSystem(resource)).getByteArray());
            IOAccess.deleteFileByResource(resource);
        } catch (IOException | NullPointerException e) {
            return false;
        }
        return true;
    }

    public static boolean renameFile(Resource resource, String newName) {
        String parentPath = FILE_DIRECTORY + File.separator + resource.getCreator().getUsername();
        if (resource.getParent() != null) {
            parentPath = IOAccess.getFilePathByResource(resource.getParent());
        }
        Path resourcePath = Paths.get(parentPath + File.separator + newName);
        if (Files.exists(resourcePath)) {
            return false;
        }
        try {
            Files.move(Paths.get(IOAccess.getFilePathByResource(resource)), resourcePath);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static void setFileDirectory(String fileDirectory) {
        if (FILE_DIRECTORY == null) {
            FILE_DIRECTORY = fileDirectory;
        }
    }
}
