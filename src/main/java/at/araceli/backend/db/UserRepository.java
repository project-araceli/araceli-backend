package at.araceli.backend.db;

import at.araceli.backend.pojos.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByToken(String token);
    Optional<User> findByUsername(String username);
    @Query("SELECT u.passwordHash FROM User u WHERE u.email = :email")
    Optional<String> getUserPasswordHash(String email);
    @Query("SELECT u.username FROM User u")
    List<String> findAllUsernames();
}
