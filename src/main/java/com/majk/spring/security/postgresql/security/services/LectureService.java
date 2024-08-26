
package com.majk.spring.security.postgresql.security.services;

import com.majk.spring.security.postgresql.models.Lecture;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Majkel
 */
public interface LectureService {
    void saveLecture(Lecture lecture);
    Lecture getLectureById(Long id);
    List<Lecture> findActiveLectures();
    List<Lecture> findActiveLecturesByGrade(String gradeName);
    Optional<Lecture> findByName(String name);
    List<Lecture> findLecturesByTeacher(Long id);
    void deleteLecture(Long lectureId) ;
    public int countTotalLecturesByGrade(String gradeName);
    public void deleteLectureByName(String lectureName);
    public boolean existsByName(String name);
    List<Lecture> findLecturesByGrade(String name);
    public List<String> findSubjectsByUserAndGrade(String username, String gradeName);
    public long countTotalLecturesByGradeAndSubject(String gradeName, String subject);
    public List<Lecture> findByGradeNameAndTeacherUsername(String name, String username);
    public List<Lecture> findByGradeName(String name);
    public Map<String, List<Lecture>> groupLecturesBySubject(String gradeName);
}

