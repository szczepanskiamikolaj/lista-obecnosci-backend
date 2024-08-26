
package com.majk.spring.security.postgresql.payload.response;

import java.time.LocalDateTime;
import lombok.Data;

/**
 *
 * @author Majkel
 */
@Data
public class LectureResponse {

    private LocalDateTime timeLimit;
    private String name;
    private String gradeName;
    private String teacherName;

    public LectureResponse(LocalDateTime timeLimit, String name, String gradeName, String teacherName) {
        this.timeLimit = timeLimit;
        this.name = name;
        this.gradeName = gradeName;
        this.teacherName = teacherName;
    }
}

