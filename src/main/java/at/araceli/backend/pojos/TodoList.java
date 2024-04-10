package at.araceli.backend.pojos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "todoList")
    private List<Item> items = new ArrayList<>();
}
