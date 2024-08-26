
package com.majk.spring.security.postgresql.controllers;

import com.majk.spring.security.postgresql.models.Grade;
import com.majk.spring.security.postgresql.payload.request.GradeRequest;
import com.majk.spring.security.postgresql.security.services.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Majkel
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/grades")
@Tag(name = "GradeController", description = "Endpointy związane z zarządzaniem klasami")
public class GradeController {

    @Autowired
    private GradeService gradeService; 

    @Operation(
            summary = "Dodaj klasę",
            description = "Endpoint służący do dodawania nowej klasy.",
            tags = {"Klasy"}
    )
    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addGrade(@RequestBody GradeRequest gradeRequest) {
        try {

            Grade newGrade = new Grade();
            newGrade.setName(gradeRequest.getName());

            gradeService.saveGrade(newGrade);

            return ResponseEntity.ok("Klasa dodana pomyślnie");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd w dodawaniu klasy");
        }
    }
}

