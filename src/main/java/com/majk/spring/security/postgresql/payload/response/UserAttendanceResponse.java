
package com.majk.spring.security.postgresql.payload.response;

/**
 *
 * @author Majkel
 */
import java.util.List;
import lombok.Data;
@Data
public class UserAttendanceResponse {

    private String gradeName;
    private List<UserAttendanceDetail> userAttendanceDetails;

    public UserAttendanceResponse(String gradeName, List<UserAttendanceDetail> userAttendanceDetails) {
        this.gradeName = gradeName;
        this.userAttendanceDetails = userAttendanceDetails;
    }

}
