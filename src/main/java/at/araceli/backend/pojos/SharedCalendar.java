package at.araceli.backend.pojos;

import at.araceli.backend.pojos.enums.Permission;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut, Michael Hütter
 * Created at: 10.04.24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SharedCalendar {

    @EmbeddedId
    private SharedCalendarId sharedCalendarId;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("calendarId")
    @JoinColumn(name = "calendar_id")
    private Calendar calendar;

    @Enumerated(EnumType.ORDINAL)
    private Permission permission;

}
