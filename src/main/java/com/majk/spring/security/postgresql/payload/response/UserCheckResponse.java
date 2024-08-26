
package com.majk.spring.security.postgresql.payload.response;

import lombok.Data;

/**
 *
 * @author Majkel
 */
@Data
public class UserCheckResponse {
    private String email;
    private Integer roleId;

    public UserCheckResponse(String email, Integer id) {
        this.email = email; this.roleId = id;
    }
}
