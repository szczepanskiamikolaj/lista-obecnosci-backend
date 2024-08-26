
package com.majk.spring.security.postgresql.payload.response;

import lombok.Data;

/**
 *
 * @author Majkel
 */
@Data
public class LectureAttendanceResponse {

    private String lectureName;
    private boolean attended;

    public LectureAttendanceResponse(String lectureName, boolean attended) {
        this.lectureName = lectureName;
        this.attended = attended;
    }
}
