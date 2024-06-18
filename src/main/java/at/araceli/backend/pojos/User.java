package at.araceli.backend.pojos;

import at.araceli.backend.pojos.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
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
@Builder
@Table(name = "araceliuser")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", allocationSize = 1)
    private Long userId;
    @Column(unique = true, nullable = false)
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
    @Enumerated(EnumType.ORDINAL)
    private Role role;

    @JsonIgnore
    @JsonManagedReference
    @OneToMany(mappedBy = "creator", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Resource> resources = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
