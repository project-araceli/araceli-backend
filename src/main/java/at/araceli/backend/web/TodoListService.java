package at.araceli.backend.web;

import at.araceli.backend.db.ItemRepository;
import at.araceli.backend.db.TodoListRepository;
import at.araceli.backend.db.UserRepository;
import at.araceli.backend.pojos.Item;
import at.araceli.backend.pojos.TodoList;
import at.araceli.backend.pojos.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;


/**
 * Project: araceli-backend
 * Created by: Nico Bulut
 * Created at: 07.05.24
 */

@RestController
@RequestMapping("/todolist")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class TodoListService {

    private final TodoListRepository todoListRepo;
    private final ItemRepository itemRepo;
    private final UserRepository userRepo;

    // TODO: remove after tests
    @PostConstruct
    public void test() {
        userRepo.save(new User(null, "test", "test@test.com", "", "", null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    }

    @GetMapping
    public ResponseEntity<Iterable<TodoList>> getAllTodoListsByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(todoListRepo.findByUserId(userId));
    }

    @GetMapping("/{todoListId}/item")
    public ResponseEntity<Iterable<Item>> getTodoListItemsByTodoListId(@PathVariable Long todoListId) {
        return ResponseEntity.ok(todoListRepo.findItemsByTodoListId(todoListId));
    }

    @PostMapping
    public ResponseEntity<TodoList> createTodoList(@RequestParam Long userId, @RequestBody TodoList todoList) {
        Optional<User> optionalUser = userRepo.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.getTodoLists().add(todoList);
            todoList.setCreator(user);
            todoListRepo.save(todoList);
            return ResponseEntity.status(HttpStatus.CREATED).body(todoList);
        }

        return ResponseEntity.notFound().build();
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

    @DeleteMapping("/{todoListId}")
    public ResponseEntity<String> deleteTodoList(@PathVariable Long todoListId) {
        todoListRepo.deleteById(todoListId);
        return ResponseEntity.accepted().body("" + todoListId);
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<String> deleteItemFromTodoList(@PathVariable String itemId) {
        itemRepo.deleteById(itemId);
        return ResponseEntity.accepted().body(itemId);
    }

    @PatchMapping("/{todoListId}")
    public ResponseEntity<TodoList> updateTodoList(@PathVariable Long todoListId, @RequestBody TodoList todoListChanges) {
        Optional<TodoList> optionalTodoList = todoListRepo.findById(todoListId);

        if (optionalTodoList.isPresent()) {
            TodoList todoListInDB = optionalTodoList.get();
            Field[] fields = todoListInDB.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = ReflectionUtils.getField(field, todoListChanges);
                if (value != null) {
                    ReflectionUtils.setField(field, todoListInDB, value);
                }
            }
            todoListRepo.save(todoListInDB);
            return ResponseEntity.accepted().body(todoListInDB);
        }

        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/item/{itemId}")
    public ResponseEntity<Item> updateItem(@PathVariable String itemId, @RequestBody Item itemUpdates) {
        Optional<Item> optionalItem = itemRepo.findById(itemId);

        if (optionalItem.isPresent()) {
            Item itemInDB = optionalItem.get();
            Field[] fields = itemInDB.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = ReflectionUtils.getField(field, itemUpdates);
                if (value != null) {
                    ReflectionUtils.setField(field, itemInDB, value);
                }
            }
            itemRepo.save(itemInDB);
            return ResponseEntity.accepted().body(itemInDB);
        }

        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/item/{itemId}/toggleDone")
    public ResponseEntity<Item> setDoneItem(@PathVariable String itemId) {
        Optional<Item> optionalItem = itemRepo.findById(itemId);

        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();
            item.setIsDone(!item.getIsDone());
            itemRepo.save(item);
            return ResponseEntity.accepted().body(item);
        }

        return ResponseEntity.notFound().build();
    }

}
