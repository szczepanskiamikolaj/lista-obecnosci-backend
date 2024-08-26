package com.majk.spring.security.postgresql.repository;

import com.majk.spring.security.postgresql.models.Attendance;
import com.majk.spring.security.postgresql.models.Lecture;
import com.majk.spring.security.postgresql.models.User;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByLectureAndUser(Lecture lecture, User user);
    int countByUserUsernameAndLectureGradeName(String username, String gradeName);
    boolean existsByUserAndLecture(User user, Lecture lecture);
    void deleteByLecture(Lecture lecture);
    List<Attendance> findByUser(User user);
    long countByUserUsernameAndLectureGradeNameAndLectureSubject(String username, String gradeName, String subject);    
}
