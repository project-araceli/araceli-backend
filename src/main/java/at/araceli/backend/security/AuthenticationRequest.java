package at.araceli.backend.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project: araceli-backend
 * Created by: Michael Hütter
 * Created at: 16.06.2024
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    private String username;
    private String password;
}
