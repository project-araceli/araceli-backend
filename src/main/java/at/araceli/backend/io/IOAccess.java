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

public class IOAccess {

    /**
     * root of the user file structure, in this folder will all the user folders be stored
     */
    public static String FILE_DIRECTORY;

    /**
     * writes a multipart file to the file system, (gets path by looping through parents)
     * @param resource
     * @param multipartFile
     * @return true - if the operation was successful; false - if the operation failed / the file already exists
     */
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

    /**
     * creates a folder
     * @param resource
     * @return true - if the operation was successful; false - if the operation failed / the folder already exists
     */
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

    /**
     * creates a folder for a new user in the file system; needs to be executed when a new user registers
     * @param user
     * @return true - if the operation was successful; false - if the operation failed / the folder already exists
     */
    public static boolean createFolderStructureForNewUser(User user) {
        try {
            Files.createDirectory(Paths.get(FILE_DIRECTORY + File.separator + user.getUsername()));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     *  reads file from the file system and converts it to a ByteArrayResource (format used for OCTET STREAMS in REST APIs)
     * @param resource
     * @return ByteArrayResource - if the file was successfully read, null - if any error occurred
     */
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

    /**
     * deletes the file / folder specified in the parameter
     * @param resource
     * @return true - if the operation was successful; false - if the operation failed / the resource does not exist on the file system
     */
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

    /**
     * iterates through the tree of resources all the way to the root to calculate the path of the given resource
     * @param resource
     * @return String - absolute file path of the resource
     */
    public static String getFilePathByResource(Resource resource) {
        List<String> resourcePaths = new ArrayList<>();
        String creator = resource.getCreator().getUsername();

        while (resource != null) {
            resourcePaths.add(resource.getName());
            resource = resource.getParent();
        }
        Collections.reverse(resourcePaths);
        resourcePaths.add(0, creator);
        resourcePaths.add(0, FILE_DIRECTORY);
        return String.join(File.separator, resourcePaths);
    }

    /**
     * iterates through the tree of resources all the way to the resource's creator's folder to calculate the path of the given resource
     * @param resource
     * @return String - relative file path of the resource
     */
    public static String getRelativeFilePathByResource(Resource resource) {
        List<String> resourcePaths = new ArrayList<>();

        while (resource != null) {
            resourcePaths.add(resource.getName());
            resource = resource.getParent();
        }
        Collections.reverse(resourcePaths);
        return String.join(File.separator, resourcePaths);
    }

    /**
     * moves a file from resource's location to the path of parentResource + resource.getName()
     * @param resource
     * @param parentResource
     * @return true - if the operation was successful; false - if the operation failed / the resource at parentResource + resource.getName() already exists
     */
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

    /**
     * renames a file (by moving it)
     * @param resource
     * @param newName
     * @return true - if the operation was successful; false - if the operation failed / the resource at resource.getName() already exists
     */
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
