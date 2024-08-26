
package com.majk.spring.security.postgresql.security.services;
import com.majk.spring.security.postgresql.models.User;

/**
 *
 * @author Majkel
 */
public interface GradeUserService {
    boolean isModeratorAssignedToGrade(String gradeName, String moderatorUsername);

    void addUserToGrade(String gradeName, User user);
    public boolean isUserInGrade(String gradeName, User user);

}

