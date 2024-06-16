package at.araceli.backend.pojos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
* Project: araceli-backend
* Created by: Nico Bulut
* Created at: 15.06.24  
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String key;
    private User sender;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;
}
