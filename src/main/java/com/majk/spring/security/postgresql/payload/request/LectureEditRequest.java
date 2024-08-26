
package com.majk.spring.security.postgresql.payload.request;

import lombok.Data;

/**
 *
 * @author Majkel
 */
@Data
public class LectureEditRequest {

    private String name;
    private Long timeLimit;
    private String gradeName; 
    private String subject;
    private Boolean secure;
}
