package at.araceli.backend.web;

import at.araceli.backend.db.TodoListRepository;
import at.araceli.backend.pojos.Item;
import at.araceli.backend.pojos.TodoList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;


/**
 * Project: araceli-backend
 * Created by: Nico Bulut
 * Created at: 07.05.24
 */

@RestController
@RequestMapping("/todolist")
@RequiredArgsConstructor
public class TodoListService {

    private final TodoListRepository todoListRepo;

    @GetMapping
    public ResponseEntity<Iterable<TodoList>> getAllTodoListsByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(todoListRepo.findByUserId(userId));
    }

    @GetMapping("/{todoListId}/items")
    public ResponseEntity<Iterable<Item>> getTodoListItemsByTodoListId(@PathVariable Long todoListId) {
        return ResponseEntity.ok(todoListRepo.findItemsByTodoListId(todoListId));
    }

    @PostMapping
    public ResponseEntity<TodoList> createTodoList(@RequestBody TodoList todoList) {
        return ResponseEntity.status(HttpStatus.CREATED).body(todoListRepo.save(todoList));
    }

    @PostMapping("/{todoListId}/addItem")
    public ResponseEntity<Iterable<Item>> addItemToTodoList(@PathVariable Long todoListId, @RequestBody Item item) {
        Optional<TodoList> optionalTodoList = todoListRepo.findById(todoListId);

        if (optionalTodoList.isPresent()) {
            TodoList todoList = optionalTodoList.get();
            todoList.addToDoList(item);
            todoListRepo.save(todoList);
            return ResponseEntity.ok(todoList.getItems());
        }

        return ResponseEntity.notFound().build();
    }
}
