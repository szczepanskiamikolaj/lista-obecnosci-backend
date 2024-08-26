
package com.majk.spring.security.postgresql.controllers;

import com.majk.spring.security.postgresql.models.Attendance;
import com.majk.spring.security.postgresql.models.Grade;
import com.majk.spring.security.postgresql.models.Lecture;
import com.majk.spring.security.postgresql.models.User;
import com.majk.spring.security.postgresql.payload.request.AttendanceRequest;
import com.majk.spring.security.postgresql.payload.response.LectureAttendanceResponse;
import com.majk.spring.security.postgresql.payload.response.SubjectAttendanceDetail;
import com.majk.spring.security.postgresql.payload.response.UserAttendanceDetail;
import com.majk.spring.security.postgresql.payload.response.UserAttendanceResponse;
import com.majk.spring.security.postgresql.repository.GradeRepository;
import com.majk.spring.security.postgresql.repository.UserRepository;
import com.majk.spring.security.postgresql.security.services.AttendanceService;
import com.majk.spring.security.postgresql.security.services.LectureService;
import com.majk.spring.security.postgresql.security.services.ResourceNotFoundException;
import com.majk.spring.security.postgresql.security.services.UserDetailsImpl;
import com.majk.spring.security.postgresql.security.services.UserService;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Majkel
 */
@Tag(name = "AttendanceController", description = "Kontroler zarządzający obecnościami.")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;
    
    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private LectureService lectureService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GradeRepository gradeRepository;

        @Operation(
            summary = "Dodaj obecność",
            description = "Endpoint służący do dodawania obecności na zajęciach.",
            tags = {"Obecność"}
    )
    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> addAttendance(@RequestBody AttendanceRequest attendanceRequest, HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userDetails.getUsername()));

            Lecture lecture = lectureService.findByName(attendanceRequest.getLectureName())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with name: " + attendanceRequest.getLectureName()));

            if (StringUtils.isNotEmpty(lecture.getIpAddress())) {
                String userIpAddress = request.getHeader("X-Forwarded-For");

                if (!Objects.equals(userIpAddress, lecture.getIpAddress())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized for secure lecture");
                }
            }

            if (!lecture.isActive() || LocalDateTime.now().isAfter(lecture.getTimeLimit())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lecture is not active or time limit has been reached");
            }

            if (attendanceService.existsByLectureAndUser(lecture, currentUser)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Attendance already added for this lecture");
            }
            
            Set<Grade> userGrades = gradeRepository.findGradesByUserUsername(currentUser.getUsername());

            if (!userGrades.contains(lecture.getGrade())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized for this lecture");
            }
            Attendance newAttendance = new Attendance();
            newAttendance.setLecture(lecture);
            newAttendance.setUser(currentUser);

            attendanceService.saveAttendance(newAttendance);

            return ResponseEntity.ok("Attendance added successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding attendance");
        }
    }

     @Operation(
            summary = "Pobierz obecności użytkownika",
            description = "Endpoint służący do pobierania informacji o obecnościach użytkownika.",
            tags = {"Obecność"}
    )
    @GetMapping("/attendances")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getAttendancesForUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userDetails.getUsername()));

            Map<String, List<LectureAttendanceResponse>> subjectAttendanceMap = attendanceService.getLectureAttendancesByUserGroupedBySubject(currentUser);

            return ResponseEntity.ok(subjectAttendanceMap);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd w pobieraniu");
        }
    }

    @Operation(
            summary = "Pobierz obecności moderatora",
            description = "Endpoint służący do pobierania informacji o obecnościach wszystkich użytkownikach którzy są w klasach moderatora.",
            tags = {"Obecność"}
    )
    @GetMapping("/moderator-attendance")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<?> getModeratorAttendance() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User moderator = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userDetails.getUsername()));

            List<UserAttendanceResponse> userAttendanceResponses = new ArrayList<>();

            Set<Grade> moderatorGrades = gradeRepository.findGradesByUserUsername(moderator.getUsername());

            for (Grade grade : moderatorGrades) {
                System.out.println("Processing grade: " + grade.getName());
                List<User> usersInGrade = userService.findUsersByGradeAndRole(grade.getName(), 1L);

                // Group lectures by subject
                Map<String, List<Lecture>> lecturesBySubject = lectureService.groupLecturesBySubject(grade.getName());

                List<UserAttendanceDetail> userAttendanceDetails = new ArrayList<>();

                for (User user : usersInGrade) {
                    System.out.println("Processing user: " + user.getUsername());

                    List<SubjectAttendanceDetail> subjectAttendanceDetails = new ArrayList<>();

                    for (Map.Entry<String, List<Lecture>> entry : lecturesBySubject.entrySet()) {
                        String subject = entry.getKey();
                        if ("null".equals(subject)) {
                            // Handle null case separately
                            subject = null;
                        }

                        long totalLectures = lectureService.countTotalLecturesByGradeAndSubject(grade.getName(), subject);
                        long attendedLectures = attendanceService.countAttendancesByUserAndGradeAndSubject(user.getUsername(), grade.getName(), subject);

                        double attendanceRatio = (totalLectures > 0) ? ((double) attendedLectures / totalLectures) * 100 : 0;

                        System.out.println("Subject: " + subject + ", Total Lectures: " + totalLectures + ", Attended Lectures: " + attendedLectures + ", Ratio: " + attendanceRatio);

                        SubjectAttendanceDetail subjectAttendanceDetail = new SubjectAttendanceDetail(
                                subject,
                                attendedLectures,
                                totalLectures,
                                attendanceRatio
                        );
                        subjectAttendanceDetails.add(subjectAttendanceDetail);
                    }

                    String username = user.getUsername();
                    String name = user.getName();
                    String surname = user.getSurname();

                    String userDisplayName = username;

                    if (name != null) {
                        userDisplayName += " " + name;
                    }

                    if (surname != null) {
                        userDisplayName += " " + surname;
                    }

                    UserAttendanceDetail userAttendanceDetail = new UserAttendanceDetail(
                        userDisplayName,
                        subjectAttendanceDetails
                    );
                    userAttendanceDetails.add(userAttendanceDetail);
                }

                UserAttendanceResponse userAttendanceResponse = new UserAttendanceResponse(
                        grade.getName(),
                        userAttendanceDetails
                );

                userAttendanceResponses.add(userAttendanceResponse);
            }

            return ResponseEntity.ok(userAttendanceResponses);

        } catch (ResourceNotFoundException e) {
            logger.error("ResourceNotFoundException: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching moderator attendance");
        }
    }

}