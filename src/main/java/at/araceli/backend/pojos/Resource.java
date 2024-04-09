package at.araceli.backend.pojos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut, Michael Hütter
 * Created at: 09.04.24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String resourceId;
    private String name;
    private String description;

    @OneToOne
    private Resource parentResource;
}
