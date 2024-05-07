package at.araceli.backend.db;

import at.araceli.backend.pojos.Item;
import at.araceli.backend.pojos.TodoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TodoListRepository extends JpaRepository<TodoList, Long> {

    @Query("SELECT t FROM TodoList t WHERE t.creator.userId = :userId")
    List<TodoList> findByUserId(Long userId);

    @Query("SELECT t.items FROM TodoList t WHERE t.todoListId = :todoListId")
    List<Item> findItemsByTodoListId(Long todoListId);
}
