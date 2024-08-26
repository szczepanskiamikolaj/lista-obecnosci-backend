package com.majk.spring.security.postgresql.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.majk.spring.security.postgresql.models.User;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);

    public List<User> findByEmailIn(List<String> userEmails);

    public Optional<User> findByEmail(String userEmail);
    
    List<User> findUsersByGradesNameAndRolesId(String gradeName, Long roleId);
}
