package at.araceli.backend.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* Project: araceli-backend
* Created by: Michael Hütter
* Created at: 18.06.2024
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleAuthenticationRequest {

    private String token;

}
