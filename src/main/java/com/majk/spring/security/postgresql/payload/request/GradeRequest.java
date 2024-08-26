
package com.majk.spring.security.postgresql.payload.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 *
 * @author Majkel
 */


@Data
public class GradeRequest {   
    @NotBlank
    @Size(max=20)
    private String name;
}
