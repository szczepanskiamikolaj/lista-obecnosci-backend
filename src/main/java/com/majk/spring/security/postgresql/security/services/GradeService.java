
package com.majk.spring.security.postgresql.security.services;

import com.majk.spring.security.postgresql.models.Grade;
import java.util.List;

/**
 *
 * @author Majkel
 */
public interface GradeService {
    void saveGrade(Grade grade);
    List<Grade> getAllGrades();
    Grade getGradeById(Long id);
    Grade getGradeByName(String name);
}

