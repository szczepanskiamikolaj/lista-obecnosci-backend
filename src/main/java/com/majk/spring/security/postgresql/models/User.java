package com.majk.spring.security.postgresql.models;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;
import lombok.Data;
@Data
@Entity
@Table( name = "users", 
        uniqueConstraints = { 
          @UniqueConstraint(columnNames = "username"),
          @UniqueConstraint(columnNames = "email") 
        })
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 20)
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  private String email;
  
  @Size(max = 20)
  private String name;
  
  @Size(max = 50)
  private String surname;

  @NotBlank
  @Size(max = 120)
  private String password;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "user_grades", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "grade_id"))
  private Set<Grade> grades = new HashSet<>();

  @OneToMany(mappedBy = "user")
  private List<Attendance> attendances;
  
  @OneToMany(mappedBy = "teacher")
  private List<Lecture> lectures;
  
  @Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return Objects.equals(id, user.id);
}

@Override
public int hashCode() {
    return Objects.hash(id);
}
  
  public User() {
  }

  public User(String username, String email, String name, String surname, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.name = name; this.surname = surname;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }
}
