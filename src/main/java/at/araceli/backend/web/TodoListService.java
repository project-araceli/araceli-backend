package at.araceli.backend.web;

import at.araceli.backend.db.ItemRepository;
import at.araceli.backend.db.TodoListRepository;
import at.araceli.backend.db.UserRepository;
import at.araceli.backend.pojos.Item;
import at.araceli.backend.pojos.TodoList;
import at.araceli.backend.pojos.User;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
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

    @GetMapping
    public ResponseEntity<Iterable<TodoList>> getAllTodoListsByUser(HttpServletRequest request) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(todoListRepo.findByUserId(user.getUserId()));
    }

    @GetMapping("/{todoListId}/item")
    public ResponseEntity<Iterable<Item>> getTodoListItemsByTodoListId(HttpServletRequest request, @PathVariable Long todoListId) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TodoList todoList = todoListRepo.findById(todoListId).orElse(null);
        if (todoList == null || !todoList.getCreator().equals(user)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(todoList.getItems());
    }

    @PostMapping
    public ResponseEntity<TodoList> createTodoList(HttpServletRequest request, @RequestBody TodoList todoList) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        user.getTodoLists().add(todoList);
        todoList.setCreator(user);
        todoListRepo.save(todoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(todoList);
    }

    @PostMapping("/{todoListId}/addItem")
    public ResponseEntity<Iterable<Item>> addItemToTodoList(HttpServletRequest request, @PathVariable Long todoListId, @RequestBody Item item) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TodoList todoList = todoListRepo.findById(todoListId).orElse(null);

        if (todoList != null && todoList.getCreator().equals(user)) {
            todoList.addToDoList(item);
            todoListRepo.save(todoList);
            return ResponseEntity.ok(todoList.getItems());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{todoListId}")
    public ResponseEntity<String> deleteTodoList(HttpServletRequest request, @PathVariable Long todoListId) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TodoList todoList = todoListRepo.findById(todoListId).orElse(null);
        if (todoList != null && todoList.getCreator().equals(user)) {
            todoListRepo.deleteById(todoListId);
        }

        return ResponseEntity.accepted().body("" + todoListId);
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<String> deleteItemFromTodoList(HttpServletRequest request, @PathVariable String itemId) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Item item = itemRepo.findById(itemId).orElse(null);
        if (item == null || !item.getTodoList().getCreator().equals(user)) {
            return ResponseEntity.notFound().build();
        }
        itemRepo.deleteById(itemId);
        return ResponseEntity.accepted().body(itemId);
    }

    @PatchMapping("/{todoListId}")
    public ResponseEntity<TodoList> updateTodoList(HttpServletRequest request, @PathVariable Long todoListId, @RequestBody TodoList todoListChanges) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TodoList todoList = todoListRepo.findById(todoListId).orElse(null);

        if (todoList != null && todoList.getCreator().equals(user)) {
            Field[] fields = todoList.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = ReflectionUtils.getField(field, todoListChanges);
                if (value != null) {
                    ReflectionUtils.setField(field, todoList, value);
                }
            }
            todoListRepo.save(todoList);
            return ResponseEntity.accepted().body(todoList);
        }

        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/item/{itemId}")
    public ResponseEntity<Item> updateItem(HttpServletRequest request, @PathVariable String itemId, @RequestBody Item itemUpdates) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Item item = itemRepo.findById(itemId).orElse(null);

        if (item != null && item.getTodoList().getCreator().equals(user)) {
            Field[] fields = item.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = ReflectionUtils.getField(field, itemUpdates);
                if (value != null) {
                    ReflectionUtils.setField(field, item, value);
                }
            }
            itemRepo.save(item);
            return ResponseEntity.accepted().body(item);
        }

        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/item/{itemId}/toggleDone")
    public ResponseEntity<Item> setDoneItem(HttpServletRequest request, @PathVariable String itemId) {
        User user = userRepo.findByUsername(request.getUserPrincipal().getName()).orElse(null);
        log.info("HEREEEEE");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Item item = itemRepo.findById(itemId).orElse(null);
        log.info("{}", item == null);

        if (item != null && item.getTodoList().getCreator().equals(user)) {
            item.setIsDone(!item.getIsDone());
            log.info(item.toString());
            log.info("HEREE");
            itemRepo.save(item);
            return ResponseEntity.accepted().body(item);
        }

        return ResponseEntity.notFound().build();
    }

}
