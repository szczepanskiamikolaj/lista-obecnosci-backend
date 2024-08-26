
package com.majk.spring.security.postgresql.controllers;

import com.majk.spring.security.postgresql.security.services.GradeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
/**
 *
 * @author Majkel
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(GradeController.class)
@ContextConfiguration
@AutoConfigureMockMvc(addFilters = false)
public class GradeTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GradeService gradeServiceMock;

    @WithMockUser(roles = "ADMIN")
    @Test
    public void testAddGrade() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/grades/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"GradeName\"}"))
                .andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content: " + result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }
}
