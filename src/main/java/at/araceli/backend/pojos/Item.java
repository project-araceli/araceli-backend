package at.araceli.backend.pojos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut, Michael Hütter
 * Created at: 09.04.24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String itemId;
    private String name;
    private String description;
    private Boolean isDone;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "todo_list_id")
    private TodoList todoList;
}
