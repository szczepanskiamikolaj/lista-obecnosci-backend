
package com.majk.spring.security.postgresql.security.services;

import com.majk.spring.security.postgresql.models.User;
import com.majk.spring.security.postgresql.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Majkel
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
     public List<User> findUsersByGradeAndRole(String gradeName, Long roleId) {
        return userRepository.findUsersByGradesNameAndRolesId(gradeName, roleId);
    }

}