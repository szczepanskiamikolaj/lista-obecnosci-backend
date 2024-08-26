
package com.majk.spring.security.postgresql.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max=20)
    private String name;

    @ManyToMany(mappedBy = "grades")
    private Set<User> users;
    
    @OneToMany(mappedBy = "grade")
    private List<Lecture> lectures;

    public Grade() {
    }
    
    
    public Grade(String name) {
        this.name = name;
    }
    
     @Override
    public String toString() {
        return "Grade{" +
                "id=" + id +
                ", name='" + name + '\'' +
                // Exclude users to avoid cyclic reference
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grade grade = (Grade) o;
        return Objects.equals(id, grade.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    
}
