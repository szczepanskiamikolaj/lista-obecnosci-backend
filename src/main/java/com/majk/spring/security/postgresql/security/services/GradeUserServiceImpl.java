
package com.majk.spring.security.postgresql.security.services;

/**
 *
 * @author Majkel
 */
import com.majk.spring.security.postgresql.models.Grade;
import com.majk.spring.security.postgresql.models.User;
import com.majk.spring.security.postgresql.repository.GradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GradeUserServiceImpl implements GradeUserService {

    @Autowired
    private GradeRepository gradeRepository;


    @Override
    public boolean isModeratorAssignedToGrade(String gradeName, String moderatorUsername) {
        return gradeRepository.existsByNameAndModeratorsUsername(gradeName, moderatorUsername);
    }

    @Override
    public void addUserToGrade(String gradeName, User user) {
        Grade grade = gradeRepository.findByName(gradeName)
                .orElseThrow(() -> new RuntimeException("Grade not found with name: " + gradeName));

        // Update the grade in the user's set of grades
        user.getGrades().add(grade);

        gradeRepository.save(grade);
    }
    
    @Override
    public boolean isUserInGrade(String gradeName, User user) {
        Grade grade = gradeRepository.findByName(gradeName)
                .orElseThrow(() -> new RuntimeException("Grade not found with name: " + gradeName));
                return grade.getUsers().contains(user);
        }

}



