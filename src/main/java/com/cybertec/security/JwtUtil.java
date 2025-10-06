package com.cybertec.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // ✅ Usa una clave BASE64 de ≥64 bytes (ejemplo seguro para dev)
    @Value("${jwt.secret:ZRlq2QKq5TgGgq1T3Qk3J5kY8lq1m4z6w7x9y1A3B5C7D9E1F3H5J7L9N1P3R5T7V9X1Z3b5d7f9h1j3l5n7p9r1t3v5x7z9==}")
    private String jwtSecretB64;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    private Key getSigningKey() {
        // ✅ Decodifica Base64 y garantiza tamaño correcto para HS512
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretB64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // ✅ HS512 con clave fuerte
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getRoleFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("JWT signature inválida");
        } catch (MalformedJwtException ex) {
            log.error("JWT token inválido");
        } catch (ExpiredJwtException ex) {
            log.error("JWT token expirado");
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token no soportado");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims vacío");
        }
        return false;
    }
}
