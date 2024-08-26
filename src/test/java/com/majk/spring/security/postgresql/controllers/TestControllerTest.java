package com.majk.spring.security.postgresql.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testAllAccess() {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/api/test/all", String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Witaj na elektronicznej liście obecności.", responseEntity.getBody());
    }
}
