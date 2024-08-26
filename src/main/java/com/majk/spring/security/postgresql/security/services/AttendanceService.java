
package com.majk.spring.security.postgresql.security.services;

import com.majk.spring.security.postgresql.models.Attendance;
import com.majk.spring.security.postgresql.models.Grade;
import com.majk.spring.security.postgresql.models.Lecture;
import com.majk.spring.security.postgresql.models.User;
import com.majk.spring.security.postgresql.payload.response.LectureAttendanceResponse;
import com.majk.spring.security.postgresql.repository.AttendanceRepository;
import com.majk.spring.security.postgresql.repository.GradeRepository;
import com.majk.spring.security.postgresql.repository.LectureRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Majkel
 */
@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private LectureRepository lectureRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private LectureService lectureService;

    public boolean existsByLectureAndUser(Lecture lecture, User user) {
        return attendanceRepository.existsByLectureAndUser(lecture, user);
    }

    public void saveAttendance(Attendance attendance) {
        attendanceRepository.save(attendance);
    }
    
    public List<List<LectureAttendanceResponse>> getLectureAttendancesByUser(User user) {
        List<List<LectureAttendanceResponse>> attendanceData = new ArrayList<>();

        Set<Grade> userGrades = gradeRepository.findGradesByUserUsername(user.getUsername());

        for (Grade grade : userGrades) {
            List<LectureAttendanceResponse> gradeAttendances = new ArrayList<>();
            List<Lecture> gradeLectures = lectureRepository.findByGrade(grade);

            for (Lecture lecture : gradeLectures) {
                boolean attended = attendanceRepository.existsByUserAndLecture(user, lecture);

                LectureAttendanceResponse response = new LectureAttendanceResponse(
                        lecture.getName(),
                        attended
                );
                gradeAttendances.add(response);
            }
            attendanceData.add(gradeAttendances);
        }
        return attendanceData;
    }

    
    public int countAttendancesByUserAndGrade(String username, String gradeName) {
        return attendanceRepository.countByUserUsernameAndLectureGradeName(username, gradeName);
    }

    public Map<String, List<LectureAttendanceResponse>> getLectureAttendancesByUserGroupedBySubject(User user) {
        Set<Grade> userGrades = gradeRepository.findGradesByUsersContains(user);

        
        List<Lecture> qualifiedLectures = userGrades.stream()
                .flatMap(grade -> lectureService.findLecturesByGrade(grade.getName()).stream())
                .collect(Collectors.toList());     

        List<Attendance> attendances = attendanceRepository.findByUser(user);
        Map<String, List<LectureAttendanceResponse>> subjectAttendanceMap = new HashMap<>();

        for (Lecture qualifiedLecture : qualifiedLectures) {
            String subject = qualifiedLecture.getSubject();
            subject = (subject == null || subject.isEmpty()) ? "brak" : subject;

            List<LectureAttendanceResponse> subjectAttendances = subjectAttendanceMap.getOrDefault(subject, new ArrayList<>());

            boolean attended = attendances.stream()
                    .anyMatch(attendance -> attendance.getLecture().equals(qualifiedLecture));

            subjectAttendances.add(new LectureAttendanceResponse(
                    qualifiedLecture.getName(),
                    attended
            ));

            subjectAttendanceMap.put(subject, subjectAttendances);
        }

        return subjectAttendanceMap;
    }

    public long countAttendancesByUserAndGradeAndSubject(String username, String gradeName, String subject) {
        return attendanceRepository.countByUserUsernameAndLectureGradeNameAndLectureSubject(username, gradeName, subject);
    }

}

