package com.majk.spring.security.postgresql.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.majk.spring.security.postgresql.models.Grade;
import com.majk.spring.security.postgresql.models.User;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    Optional<Grade> findByName(String name);
    
    @Query("SELECT COUNT(g) > 0 FROM Grade g JOIN g.users u WHERE g.name = :name AND u.username = :moderatorUsername")
    boolean existsByNameAndModeratorsUsername(@Param("name") String name, @Param("moderatorUsername") String moderatorUsername);
    
    @Query("SELECT g FROM Grade g JOIN g.users u WHERE u.username = :username")
    Set<Grade> findGradesByUserUsername(@Param("username") String username);

    Set<Grade> findGradesByUsersContains(User user);


}
