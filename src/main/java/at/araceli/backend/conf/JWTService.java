package at.araceli.backend.conf;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Project: araceli-backend
 * Created by: Michael Hütter
 * Created at: 15.06.2024
 */

@Service
@RequiredArgsConstructor
public class JWTService {

    private static final String SECRET = "5eef78740d3a4bfadbc3892a07bc5d0909af11862c84919521d73fcb8c8ce5ba";
    private final AraceliBackendConfig config;

    /**
     * Extracts Username from JWT-Token
     * @param token
     * @return
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Provides a ClaimsExtracter to extract data from the JWT
     * @param token
     * @param claimsResolver
     * @return
     * @param <T>
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all the claims from the JWT
     * @param token
     * @return
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Decodes the Sign in Key for the JWT generation
     * @return SecretKey
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(config.getSecurityKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * There to get an Object Date that is a specified number of days after the current Date.
     * @param days
     * @return Date
     */
    private Date getDateInFuture(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    /**
     *
     * @param userDetails
     * @return String
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT token Based on the Userdetails provided
     * @param extractClaims
     * @param userDetails
     * @return String
     */
    public String generateToken(Map<String, Object> extractClaims, UserDetails userDetails) {

        return Jwts
                .builder()
                .claims(extractClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(getDateInFuture(30))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Checks whether the User extracted from the Token exists in the database and if the token is still valid.
     * @param token
     * @param userDetails
     * @return
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks whether token is expired.
     * @param token
     * @return boolean
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a give token.
     * @param token
     * @return Date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
