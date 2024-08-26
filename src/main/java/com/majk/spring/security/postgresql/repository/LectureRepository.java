package com.majk.spring.security.postgresql.repository;

import com.majk.spring.security.postgresql.models.Grade;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.majk.spring.security.postgresql.models.Lecture;
import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    
        List<Lecture> findByActiveTrue();
        List<Lecture> findByGradeNameAndActive(String gradeName, boolean active);
        Optional<Lecture> findByName(String name);
        List<Lecture> findByTeacherId(Long teacherId);
        List<Lecture> findByGrade(Grade grade);
        int countByGradeName(String gradeName);
        boolean existsByName(String name);
        List<Lecture> findByGradeName(String gradeName);
        long countByGradeNameAndSubject(String gradeName, String subject);
        List<Lecture> findByGradeNameAndTeacherUsername(String gradeName, String teacherUsername);
}
