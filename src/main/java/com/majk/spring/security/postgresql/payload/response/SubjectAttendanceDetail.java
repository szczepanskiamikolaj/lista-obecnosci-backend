
package com.majk.spring.security.postgresql.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Majkel
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectAttendanceDetail {
    private String subject;
    private long attendedLectures;
    private long totalLectures;
    private double attendanceRatio;
}
