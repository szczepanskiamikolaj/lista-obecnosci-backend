
package com.majk.spring.security.postgresql.payload.request;

import java.util.List;
import lombok.Data;

/**
 *
 * @author Majkel
 */
@Data
public class GradeUserRequest {
    private String gradeName;
    private List<String> userEmails;    
}
