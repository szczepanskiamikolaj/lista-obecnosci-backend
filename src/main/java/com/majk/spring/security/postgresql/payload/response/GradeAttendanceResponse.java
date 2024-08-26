
package com.majk.spring.security.postgresql.payload.response;

import java.util.List;
import lombok.Data;

/**
 *
 * @author Majkel
 */
@Data
public class GradeAttendanceResponse {

    private String gradeName;
    private List<LectureAttendanceResponse> lectures;

    public GradeAttendanceResponse(String gradeName, List<LectureAttendanceResponse> lectures) {
        this.gradeName = gradeName;
        this.lectures = lectures;
    }
}
