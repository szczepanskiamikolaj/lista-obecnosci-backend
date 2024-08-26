
package com.majk.spring.security.postgresql.controllers;

/**
 *
 * @author Majkel
 */
import com.majk.spring.security.postgresql.security.services.UserDetailsImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class UserDetailsImplTestConfig {

    @Bean
    public UserDetailsImpl userDetailsImpl() {
        return new UserDetailsImpl(1L, "testuser", "testuser@example.com", "password", null);
    }
}

