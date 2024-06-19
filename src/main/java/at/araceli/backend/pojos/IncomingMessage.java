package at.araceli.backend.pojos;

import lombok.Data;


/**
 * Project: araceli-backend
 * Created by: Nico Bulut
 * Created at: 16.06.24
 */

@Data
public class IncomingMessage {
    private String content;
    private String username;
}
