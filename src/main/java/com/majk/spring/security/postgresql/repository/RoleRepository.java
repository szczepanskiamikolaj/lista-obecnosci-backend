package com.majk.spring.security.postgresql.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.majk.spring.security.postgresql.models.ERole;
import com.majk.spring.security.postgresql.models.Role;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
  @SuppressWarnings("null")
  @Override
  List<Role> findAll();
  
  @Query("SELECT id FROM Role")
  List<Long> findAllRoleIds();
}