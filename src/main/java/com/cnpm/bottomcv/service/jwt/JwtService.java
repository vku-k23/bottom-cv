package com.cnpm.bottomcv.service.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

@Service
public class JwtService {

    private static final Logger logger = Logger.getLogger(JwtService.class.getName());

    @Value("${spring.security.jwt.secret-key}")
    private String secretKey;

    @Value("${spring.security.jwt.expirationMs:900000}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUsernameIgnoreExpiration(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .claim("roles", userDetails.getAuthorities())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusMillis(expiration)))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        boolean isUsernameValid = username.equals(userDetails.getUsername());
        boolean isTokenNotExpired = !isTokenExpired(token);

        logger.info("Token validation - Username match: " + isUsernameValid + ", Token not expired: " + isTokenNotExpired);
        logger.info("Token username: " + username + ", UserDetails username: " + userDetails.getUsername());
        logger.info("User authorities: " + userDetails.getAuthorities());

        return isUsernameValid && isTokenNotExpired;
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warning("Token expired: " + e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.warning("Unsupported JWT: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.warning("Malformed JWT: " + e.getMessage());
            throw e;
        } catch (SignatureException e) {
            logger.warning("Invalid JWT signature: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warning("JWT claims string is empty: " + e.getMessage());
            throw e;
        }
    }

    private Key getSignInKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.warning("Using fallback key creation method: " + e.getMessage());
            String fallbackSecret = secretKey + secretKey + secretKey + secretKey;
            return Keys.hmacShaKeyFor(fallbackSecret.getBytes());
        }
    }
}