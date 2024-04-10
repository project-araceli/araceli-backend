package at.araceli.backend.pojos;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Project: araceli-backend
 * Created by: Nico Bulut, Michael Hütter
 * Created at: 10.04.24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class SharedCalendarId implements Serializable {

    private String calendarId;
    private Long userId;
}
