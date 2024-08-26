
package com.majk.spring.security.postgresql;

/**
 *
 * @author Majkel
 */

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

    @Value("${majk.openapi.dev-url}")
    private String devUrl;


    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("URL Serwera");


        Contact contact = new Contact();
        contact.setEmail("szczepanskiamikolaj@gmail.com");
        contact.setName("Mikołaj Szczepański");

        Info info = new Info()
                .title("eObecność API")
                .version("1.0")
                .contact(contact)
                .description("To API służy do zarządzania elektroniczą listą obecności");

        return new OpenAPI().info(info).servers(List.of(devServer));
    }
}

