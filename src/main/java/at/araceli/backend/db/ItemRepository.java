package at.araceli.backend.db;

import at.araceli.backend.pojos.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, String> {

}
