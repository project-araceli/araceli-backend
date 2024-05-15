package at.araceli.backend.db;

import at.araceli.backend.pojos.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
