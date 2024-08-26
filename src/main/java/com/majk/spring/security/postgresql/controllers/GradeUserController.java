
package com.majk.spring.security.postgresql.controllers;

import com.majk.spring.security.postgresql.payload.request.GradeUserRequest;
import com.majk.spring.security.postgresql.models.Grade;
import com.majk.spring.security.postgresql.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.majk.spring.security.postgresql.repository.GradeRepository;
import com.majk.spring.security.postgresql.repository.UserRepository;
import com.majk.spring.security.postgresql.security.services.GradeUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
/**
 *
 * @author Majkel
 */
@Tag(name = "GradeUserController", description = "Endpointy związane z zarządzaniem klasami i użytkownikami")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/grades")
public class GradeUserController {

    @Autowired
    private GradeUserService gradeService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GradeRepository gradeRepository;

        @Operation(
            summary = "Dodaj klasę",
            description = "Endpoint służący do dodawania użytkowników do istniejącej klasy.",
            tags = {"Klasy"}
    )
    @PostMapping("/addUsers")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN')")
    public ResponseEntity<?> addUsersToGrade(@RequestBody GradeUserRequest gradeUserRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Set<String> omittedUsers = new HashSet<>();

            Optional<Grade> optionalGrade = gradeRepository.findByName(gradeUserRequest.getGradeName());
            if (optionalGrade.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Klasa o nazwie " + gradeUserRequest.getGradeName() + " nie została znaleziona."));
            }

            if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_MODERATOR"))) {
                if (!gradeService.isModeratorAssignedToGrade(gradeUserRequest.getGradeName(), authentication.getName())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nie jesteś przypisany do tej klasy.");
                }
            }

            for (String userEmail : gradeUserRequest.getUserEmails()) {
                Optional<User> optionalUser = userRepository.findByEmail(userEmail);
                if (gradeService.isUserInGrade(gradeUserRequest.getGradeName(), optionalUser.orElse(null))) {
                    omittedUsers.add(userEmail);
                    continue;
                }

                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();

                    if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                        if (user.getRoles().stream().anyMatch(role -> String.valueOf(role.getName()).equals("ROLE_MODERATOR"))) {
                            gradeService.addUserToGrade(gradeUserRequest.getGradeName(), user);
                        } else {
                            omittedUsers.add(userEmail);
                        }
                    } else if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_MODERATOR"))) {
                        if (user.getRoles().stream().anyMatch(role -> String.valueOf(role.getName()).equals("ROLE_USER"))) {
                            gradeService.addUserToGrade(gradeUserRequest.getGradeName(), user);
                        } else {
                            omittedUsers.add(userEmail);
                        }
                    }
                } else {
                    omittedUsers.add(userEmail);
                }
            }
            if (!omittedUsers.isEmpty()) {
                 return ResponseEntity.ok().body("{\"message\": \"Niektórzy użytkownicy zostali pominięci: " + omittedUsers + "\"}");
             } else {
                 return ResponseEntity.ok().body("{\"message\": \"Użytkownicy dodani do klasy pomyślnie\"}");
             }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd podczas dodawania użytkowników do klasy");
        }
    }}


