
package com.majk.spring.security.postgresql.payload.response;

import java.time.LocalDateTime;
import lombok.Data;

/**
 *
 * @author Majkel
 */
@Data
public class LectureModeratorResponse {
    private String name;
    private LocalDateTime date;
    private String gradeName;
    private String subject;

    public LectureModeratorResponse(String name, LocalDateTime date, String gradeName, String subject) {
        this.name = name;
        this.date = date;
        this.gradeName = gradeName;  
        this.subject = subject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }
}
