
package com.majk.spring.security.postgresql.models;

/**
 *
 * @author Majkel
 */
import lombok.Data;

import jakarta.persistence.*;

@Entity
@Data
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    

}
