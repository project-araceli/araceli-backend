package at.araceli.backend.io;

import at.araceli.backend.db.ResourceRepository;
import at.araceli.backend.pojos.Resource;
import at.araceli.backend.pojos.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut
 * Created at: 22.05.24
 */

public class IOAccess {

    public static final String FILE_DIRECTORY = "src/main/resources/temp";

    public static void writeFileToFileSystem(User user, Resource resource, MultipartFile multipartFile) {
        try (InputStream is = multipartFile.getInputStream()) {
            Path path = Path.of(FILE_DIRECTORY + user.getUsername() + File.separator +
                    resource.getParent() + File.separator + resource.getName());
            Files.write(path, is.readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFilePathByResource(Resource resource, ResourceRepository resourceRepo) {
        List<String> resourcePaths = new ArrayList<>();

        while (resource.getParent() != null) {
            resourcePaths.add(resource.getName());
            resource = resource.getParent();
        }
        Collections.reverse(resourcePaths);
        resourcePaths.add(0, FILE_DIRECTORY);
        resourcePaths.add(0, resource.getCreator().getUsername());
        return String.join(File.separator, resourcePaths);
    }
}
