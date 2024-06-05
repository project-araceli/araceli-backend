package at.araceli.backend.pojos;

import at.araceli.backend.pojos.enums.ResourceType;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String resourceId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.ORDINAL)
    private ResourceType type;
    private Integer size;
    private String contentType;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Resource parent;

    @JsonManagedReference
    @OneToMany(mappedBy = "parent")
    private List<Resource> children;

    @OneToMany(mappedBy = "resource")
    private List<SharedResource> sharedResources = new ArrayList<>();

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

}
