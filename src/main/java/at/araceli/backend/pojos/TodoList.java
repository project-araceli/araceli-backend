package at.araceli.backend.pojos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut, Michael Hütter
 * Created at: 10.04.24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TodoList {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "todo_list_seq")
    @SequenceGenerator(name = "todo_list_seq", allocationSize = 1)
    private Long todoListId;
    private String title;

    @JsonManagedReference
    @OneToMany(mappedBy = "todoList", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Item> items = new ArrayList<>();

    @JsonBackReference
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "creator_id")
    private User creator;

    public void addToDoList(Item item) {
        items.add(item);
        item.setTodoList(this);
    }
}
