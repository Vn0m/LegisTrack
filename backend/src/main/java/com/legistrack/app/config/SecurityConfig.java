package com.legistrack.app.config;

import com.nimbusds.jwt.JWTParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * Stateless resource server. Supabase-issued JWTs are validated locally —
 * no network round-trip per request. Route rules live here, not in controllers.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/bills/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/labels/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Supabase signs new tokens with an asymmetric key published at the project's
     * JWKS endpoint. Projects that have not rotated off the legacy shared secret
     * still issue HS256 tokens; those are verified with SUPABASE_JWT_SECRET when set.
     * The decoder routes on the token's alg header so both key types work during
     * and after rotation.
     */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.supabase.url}") String supabaseUrl,
                                 @Value("${app.supabase.jwtSecret:}") String legacySecret) {
        String issuer = supabaseUrl + "/auth/v1";
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        NimbusJwtDecoder jwksDecoder = NimbusJwtDecoder.withJwkSetUri(issuer + "/.well-known/jwks.json")
            .jwsAlgorithm(SignatureAlgorithm.ES256)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build();
        jwksDecoder.setJwtValidator(withIssuer);

        if (legacySecret.isBlank()) {
            return jwksDecoder;
        }

        NimbusJwtDecoder hs256Decoder = NimbusJwtDecoder
            .withSecretKey(new SecretKeySpec(legacySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
        hs256Decoder.setJwtValidator(withIssuer);

        return token -> "HS256".equals(algorithmOf(token))
            ? hs256Decoder.decode(token)
            : jwksDecoder.decode(token);
    }

    private static String algorithmOf(String token) {
        try {
            return JWTParser.parse(token).getHeader().getAlgorithm().getName();
        } catch (ParseException e) {
            throw new BadJwtException("Malformed JWT", e);
        }
    }
}
