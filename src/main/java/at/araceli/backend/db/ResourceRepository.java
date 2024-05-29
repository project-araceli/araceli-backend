package at.araceli.backend.db;

import at.araceli.backend.pojos.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
