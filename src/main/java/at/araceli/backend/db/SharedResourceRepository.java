package at.araceli.backend.db;

import at.araceli.backend.pojos.Resource;
import at.araceli.backend.pojos.SharedResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SharedResourceRepository extends JpaRepository<SharedResource, String> {
}
