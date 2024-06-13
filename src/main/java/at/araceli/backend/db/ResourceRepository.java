package at.araceli.backend.db;

import at.araceli.backend.pojos.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, String> {

    @Query("SELECT r FROM Resource r WHERE r.creator.userId = :userId AND r.parent IS NULL")
    List<Resource> findAllRootElementsByUserId(Long userId);

    @Query("SELECT r FROM Resource r WHERE r.creator.userId = :userId AND r.name LIKE %:name%")
    List<Resource> findAllByUserIdAndLikeName(Long userId, String name);

    @Query("SELECT r FROM Resource r WHERE r.creator.userId = :userId AND UPPER(r.name) LIKE %:extension")
    List<Resource> findAllByUserAndFileExtension(Long userId, String extension);
}
