package at.araceli.backend.conf;

import at.araceli.backend.io.IOAccess;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut
 * Created at: 05.06.24
 */

@Component
@ConfigurationProperties("at.araceli.backend")
@Data
@Slf4j
public class AraceliBackendConfig {
    /**
     * pathToFileSystem - path to file system which is used to save Resources e.g. /path/to/fs
     */
    private String pathToFileSystem;

    @PostConstruct
    public void init() {
        IOAccess.setFileDirectory(pathToFileSystem);
    }
}
