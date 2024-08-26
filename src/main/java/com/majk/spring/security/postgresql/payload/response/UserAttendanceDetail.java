
package com.majk.spring.security.postgresql.payload.response;

import java.util.List;
import lombok.Data;

@Data
public class UserAttendanceDetail {
    private String username;
    private List<SubjectAttendanceDetail> subjectAttendanceDetails;
    
        public UserAttendanceDetail(String username, List<SubjectAttendanceDetail> subjectAttendanceDetails) {
        this.username = username;
        this.subjectAttendanceDetails = subjectAttendanceDetails;
    }
    
}
