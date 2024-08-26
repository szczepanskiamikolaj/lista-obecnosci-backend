
package com.majk.spring.security.postgresql.security.services;

import com.majk.spring.security.postgresql.models.Lecture;
import com.majk.spring.security.postgresql.repository.AttendanceRepository;
import com.majk.spring.security.postgresql.repository.LectureRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Majkel
 */
@Service
public class LectureServiceImpl implements LectureService {

    @Autowired
    private LectureRepository lectureRepository;
    
    @Autowired 
    private AttendanceRepository attendanceRepository;

    @Override
    @Transactional
    public void saveLecture(Lecture lecture) {
        lectureRepository.save(lecture);
    }

    @Override
    public Lecture getLectureById(Long id) {
        return lectureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + id));
    }
    
     @Override
    public List<Lecture> findActiveLectures() {
        return lectureRepository.findByActiveTrue();
    }
    
     @Override
     public Optional<Lecture> findByName(String name) {
        return lectureRepository.findByName(name);
    }
    
     @Override
    public List<Lecture> findLecturesByTeacher(Long teacherId) {
        return lectureRepository.findByTeacherId(teacherId);
    }
    @Override
    public void deleteLecture(Long lectureId) {
        lectureRepository.deleteById(lectureId);
    }
    
    @Override
    public List<Lecture> findActiveLecturesByGrade(String gradeName) {
        return lectureRepository.findByGradeNameAndActive(gradeName, true);
    }
    
    @Override
    public int countTotalLecturesByGrade(String gradeName) {
        return lectureRepository.countByGradeName(gradeName);
    }
    
    @Override
    @Transactional
    public void deleteLectureByName(String lectureName) {
        Lecture lecture = lectureRepository.findByName(lectureName)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with name: " + lectureName));

        // Cascade delete attendances associated with the lecture
        attendanceRepository.deleteByLecture(lecture);

        lectureRepository.delete(lecture);
    }
    @Override
    public boolean existsByName(String name) {
        return lectureRepository.existsByName(name);
    }
    
    @Override
    public List<Lecture> findLecturesByGrade(String gradeName) {
        return lectureRepository.findByGradeName(gradeName);
    }
    
    @Override
    public List<String> findSubjectsByUserAndGrade(String username, String gradeName) {
        List<Lecture> lectures = lectureRepository.findByGradeNameAndTeacherUsername(gradeName, username);
        return lectures.stream()
                .map(Lecture::getSubject)
                .filter(subject -> subject != null && !subject.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public long countTotalLecturesByGradeAndSubject(String gradeName, String subject) {
        return lectureRepository.countByGradeNameAndSubject(gradeName, subject);
    }
    
    @Override
    public List<Lecture> findByGradeNameAndTeacherUsername(String gradeName, String teacherUsername) {
        return lectureRepository.findByGradeNameAndTeacherUsername(gradeName, teacherUsername);
    }
    
    @Override 
    public List<Lecture> findByGradeName(String name){
        return lectureRepository.findByGradeName(name);
    }
    @Override
    public Map<String, List<Lecture>> groupLecturesBySubject(String gradeName) {
        List<Lecture> lectures = lectureRepository.findByGradeName(gradeName);

        return lectures.stream()
                .collect(Collectors.groupingBy(
                        lecture -> (lecture.getSubject() != null) ? lecture.getSubject() : "null"));
    }




}
