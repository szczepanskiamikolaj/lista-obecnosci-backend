
package com.majk.spring.security.postgresql.controllers;

import com.majk.spring.security.postgresql.payload.response.LectureModeratorResponse;
import com.majk.spring.security.postgresql.payload.response.LectureResponse;
import com.majk.spring.security.postgresql.models.Grade;
import com.majk.spring.security.postgresql.models.Lecture;
import com.majk.spring.security.postgresql.models.User;
import com.majk.spring.security.postgresql.payload.request.LectureRequest;
import com.majk.spring.security.postgresql.repository.AttendanceRepository;
import com.majk.spring.security.postgresql.repository.GradeRepository;
import com.majk.spring.security.postgresql.repository.LectureRepository;
import com.majk.spring.security.postgresql.repository.UserRepository;
import com.majk.spring.security.postgresql.scheduler.LectureSchedulingService;
import com.majk.spring.security.postgresql.security.services.LectureService;
import com.majk.spring.security.postgresql.security.services.ResourceNotFoundException;
import com.majk.spring.security.postgresql.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Majkel
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/lectures")
@Tag(name = "LectureController", description = "Endpointy związane z zarządzaniem zajęciami")
public class LectureController {

    private final LectureSchedulingService schedulingService;
    
    private static final Logger logger = LoggerFactory.getLogger(LectureController.class);

    @Autowired
    public LectureController(LectureSchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }
    
    @Autowired
    private LectureService lectureService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private LectureRepository lectureRepository;
        @Operation(
            summary = "Utwórz zajęcia",
            description = "Endpoint służący do tworzenia nowych zajęć.",
            tags = {"Zajęcia"}
        )
    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> createLecture(@RequestBody LectureRequest lectureRequest, HttpServletRequest request) {
        try {
            if (lectureService.existsByName(lectureRequest.getName())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Zajęcia z taką nazwą już istnieją");
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie znaleziony: " + userDetails.getUsername()));

            Lecture newLecture = new Lecture();

            newLecture.setName(lectureRequest.getName());

            newLecture.setActive(true);
            
            newLecture.setSubject(lectureRequest.getSubject());
            newLecture.setDate(LocalDateTime.now());

            // Calculate the timeLimit based on the current date and minutes from the request
            LocalDateTime timeLimit = LocalDateTime.now().plusMinutes(lectureRequest.getTimeLimit());
            newLecture.setTimeLimit(timeLimit);

            newLecture.setTeacher(currentUser);

            Grade grade = gradeRepository.findByName(lectureRequest.getGradeName())
                    .orElseThrow(() -> new ResourceNotFoundException("Klasa nie znaleziona: " + lectureRequest.getGradeName()));
            newLecture.setGrade(grade);

            if (lectureRequest.getSecure() != null && lectureRequest.getSecure()) {
                String lectureIpAddress = request.getHeader("X-Forwarded-For");
                if (lectureIpAddress == null || lectureIpAddress.isEmpty()) {
                    lectureIpAddress = request.getRemoteAddr();
                }

                newLecture.setIpAddress(lectureIpAddress);
            }
            // Schedule the deactivation task
            LocalDateTime deactivationTime = newLecture.getTimeLimit();
            schedulingService.scheduleLectureDeactivation(deactivationTime, newLecture.getId());
            
            lectureService.saveLecture(newLecture);

            return ResponseEntity.ok("Zajęcia dodane");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd w tworzeniu zajęć");
        }
    }
    
    @Operation(
            summary = "Pobierz aktywne zajęcia dla użytkownika",
            description = "Endpoint służący do pobierania informacji o aktywnych zajęciach dla danego użytkownika.",
            tags = {"Zajęcia"}
    )
    @GetMapping("/active")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getActiveLecturesForUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userDetails.getUsername()));

            String username = userDetails.getUsername();
            Set<Grade> userGrades = gradeRepository.findGradesByUserUsername(username);

            List<LectureResponse> lectureResponses = userGrades.stream()
                    .flatMap(grade -> {
                        List<Lecture> activeLectures = lectureService.findActiveLecturesByGrade(grade.getName());
                        return activeLectures.stream();
                    })
                    .filter(lecture -> !attendanceRepository.existsByUserAndLecture(currentUser, lecture))
                    .map(lecture -> new LectureResponse(
                            lecture.getTimeLimit(),
                            lecture.getName(),
                            lecture.getGrade().getName(),
                            lecture.getTeacher().getName() + " " + lecture.getTeacher().getSurname()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(lectureResponses);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching active lectures");
        }
    }

     @Operation(
            summary = "Pobierz zajęcia moderatora",
            description = "Endpoint służący do pobierania informacji o zajęciach stworzonych przez moderatora.",
            tags = {"Zajęcia"}
    )
    @GetMapping("/moderator-lectures")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> getModeratorLectures() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User currentModerator = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userDetails.getUsername()));

            List<LectureModeratorResponse> lectureResponses = currentModerator.getLectures().stream()
                    .map(lecture -> {
                        return new LectureModeratorResponse(
                                lecture.getName(),
                                lecture.getDate(),
                                lecture.getGrade().getName(),
                                lecture.getSubject()
                        );
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(lectureResponses);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching moderator's lectures");
        }
    }

    @Operation(
            summary = "Usuń zajęcia",
            description = "Endpoint służący do usuwania zajęć.",
            tags = {"Zajęcia"}
    )
    @DeleteMapping("/{lectureName}")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> deleteLecture(@PathVariable String lectureName) {
        try {
            lectureService.deleteLectureByName(lectureName);
            return ResponseEntity.ok("Lecture deleted successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting lecture");
        }
    }
    
    @Operation(
            summary = "Edytuj zajęcia",
            description = "Endpoint służący do edytowania informacji o zajęciach.",
            tags = {"Zajęcia"}
    )
    @PutMapping("/editLecture/{lectureName}")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<String> editLecture(@PathVariable String lectureName, @RequestBody LectureRequest lectureRequest) {
        try {
            Optional<Lecture> optionalExistingLecture = lectureRepository.findByName(lectureName);

            if (optionalExistingLecture.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Lecture existingLecture = optionalExistingLecture.get();

            if (lectureRequest.getName() != null && !lectureRequest.getName().isEmpty()) {
                existingLecture.setName(lectureRequest.getName());
            }
            if (lectureRequest.getGradeName() != null && !lectureRequest.getGradeName().isEmpty()) {
                Optional<Grade> optionalGrade = gradeRepository.findByName(lectureRequest.getGradeName());

                if (optionalGrade.isEmpty()) {
                    return ResponseEntity.badRequest().body("Klasa z nazwą " + lectureRequest.getGradeName() + " nie znaleziona");
                }

                Grade newGrade = optionalGrade.get();
                existingLecture.setGrade(newGrade);
            }
            if (lectureRequest.getSubject() != null && !lectureRequest.getSubject().isEmpty()) {
                existingLecture.setSubject(lectureRequest.getSubject());
            }

            lectureRepository.save(existingLecture);

            return ResponseEntity.ok("Zajęcia zaktualizowane");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Błąd w aktualizowaniu zajęcia: " + e.getMessage());
        }
    }
    
}

