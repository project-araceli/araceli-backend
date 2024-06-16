package at.araceli.backend.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    @JsonIgnore
    private String email;
    @JsonIgnore
    private String passwordHash;
    @JsonIgnore
    private String token;
    @JsonIgnore
    private LocalDateTime tokenExpiresAt;
    private String imageUrl;

    @JsonIgnore
    @JsonManagedReference
    @OneToMany(mappedBy = "creator", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Resource> resources = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<SharedResource> sharedResources = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "creator")
    private List<Event> events = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<SharedCalendar> sharedCalendars = new ArrayList<>();

    @JsonIgnore
    @JsonManagedReference
    @OneToMany(mappedBy = "creator", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<TodoList> todoLists = new ArrayList<>();
}
