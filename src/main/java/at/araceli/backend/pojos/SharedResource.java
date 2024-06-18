package at.araceli.backend.pojos;

import at.araceli.backend.pojos.enums.Permission;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut,  Michael Hütter
 * Created at: 10.04.24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SharedResource {

    @EmbeddedId
    private SharedResourceId id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("resourceId")
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.ORDINAL)
    private Permission permission;
}
