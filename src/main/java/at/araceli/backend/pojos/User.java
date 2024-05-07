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
 * Created at: 09.04.24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "araceliuser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", allocationSize = 1)
    private Long userId;
    private String username;
    private String email;
    private String passwordHash;
    private String token;
    private String tokenExpiresAt;

    @OneToMany(mappedBy = "creator")
    private List<Resource> resources = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<SharedResource> sharedResources = new ArrayList<>();

    @OneToMany(mappedBy = "creator")
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<SharedCalendar> sharedCalendars = new ArrayList<>();

    @OneToMany(mappedBy = "creator")
    private List<TodoList> todoLists = new ArrayList<>();
}
