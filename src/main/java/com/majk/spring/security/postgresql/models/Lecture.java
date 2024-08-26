
package com.majk.spring.security.postgresql.models;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private boolean active;
    private LocalDateTime date;
    private LocalDateTime timeLimit;
    private String ipAddress;
    private String subject;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User teacher;

    @ManyToOne
    @JoinColumn(name = "grade_id")
    private Grade grade;
    
    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances;
}
