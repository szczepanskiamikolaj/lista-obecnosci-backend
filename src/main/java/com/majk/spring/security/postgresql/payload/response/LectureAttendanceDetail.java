
package com.majk.spring.security.postgresql.payload.response;

import lombok.Data;

/**
 *
 * @author Majkel
 */
@Data
public class LectureAttendanceDetail {

    private String lectureName;
    private boolean attended;

    public LectureAttendanceDetail(String lectureName, boolean attended) {
        this.lectureName = lectureName;
        this.attended = attended;
    }

}

