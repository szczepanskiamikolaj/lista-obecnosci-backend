
package com.majk.spring.security.postgresql.security.services;

import com.majk.spring.security.postgresql.models.Grade;
import com.majk.spring.security.postgresql.repository.GradeRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Majkel
 */
@Service
public class GradeServiceImpl implements GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Transactional
    public void saveGrade(Grade grade) {
        Optional<Grade> existingGrade = gradeRepository.findByName(grade.getName());
        
        if (existingGrade.isPresent()) {
            throw new IllegalArgumentException("Grade with name " + grade.getName() + " already exists.");
        }
        
        gradeRepository.save(grade);
    }

    @Override
    public List<Grade> getAllGrades() {
        return gradeRepository.findAll();
    }

    @Override
    public Grade getGradeById(Long id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));
    }

    @Override
    public Grade getGradeByName(String name) {
        return gradeRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with name: " + name));
    }
}
