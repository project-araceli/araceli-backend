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
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String calendarId;
    private String title;

    @OneToMany(mappedBy = "calendar")
    private List<Event> events;

    @OneToMany(mappedBy = "calendar")
    private List<SharedCalendar> sharedCalendars = new ArrayList<>();
}
