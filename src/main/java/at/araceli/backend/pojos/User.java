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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "user_seq", allocationSize = 1)
    private Long userId;
    private String username;
    private String email;
    private String passwordHash;
    private String token;
    private String tokenExpiresAt;

    @OneToMany
    private List<SharedResource> sharedResources = new ArrayList<>();
}
