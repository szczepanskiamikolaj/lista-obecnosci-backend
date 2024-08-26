
package com.majk.spring.security.postgresql.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.majk.spring.security.postgresql.models.Grade;
import com.majk.spring.security.postgresql.models.Lecture;
import com.majk.spring.security.postgresql.payload.request.LectureRequest;
import com.majk.spring.security.postgresql.repository.AttendanceRepository;
import com.majk.spring.security.postgresql.repository.GradeRepository;
import com.majk.spring.security.postgresql.repository.LectureRepository;
import com.majk.spring.security.postgresql.repository.UserRepository;
import com.majk.spring.security.postgresql.scheduler.LectureSchedulingService;
import com.majk.spring.security.postgresql.security.services.LectureService;
import com.majk.spring.security.postgresql.security.services.ResourceNotFoundException;
import com.majk.spring.security.postgresql.security.services.UserDetailsServiceImpl;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 *
 * @author Majkel
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(LectureController.class)
@ContextConfiguration(classes = UserDetailsImplTestConfig.class )
@AutoConfigureMockMvc(addFilters = false)
public class LectureTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LectureSchedulingService lectureSchedulingService;
    
    @MockBean
    private LectureService lectureService;
    
    @MockBean
    private SecurityContextHolder securtyContextHolder;
    
    @MockBean
    private SecurityContext securityContext;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private GradeRepository gradeRepository;
    
    @MockBean
    private AttendanceRepository attendanceRepository;
    
    @MockBean
    private LectureRepository lectureRepository;
    
    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;
    
    @InjectMocks
    private LectureController lectureController;

    @WithMockUser(roles = "MODERATOR", username = "moderator")
    @Test
    public void testCreateLecture() throws Exception {            
        // Prepare a valid LectureRequest
        LectureRequest lectureRequest = new LectureRequest();
        lectureRequest.setName("New Lecture");
        lectureRequest.setSubject("Math");
        lectureRequest.setTimeLimit(60L);  // 60 minutes
        lectureRequest.setGradeName("Ia");
        
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
System.out.println("Principal: " + principal);
        
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/lectures/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lectureRequest))) 
                .andReturn();
       
        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content: " + result.getResponse().getContentAsString());

        assertEquals(500, result.getResponse().getStatus());
    }
        
    @Test
    @WithMockUser(roles = "MODERATOR")
    public void testEditLecture() throws Exception {

        Lecture existingLecture = new Lecture();
        existingLecture.setName("ExistingLecture");
        existingLecture.setGrade(new Grade("ExistingGrade"));
        existingLecture.setSubject("ExistingSubject");

        LectureRequest lectureRequest = new LectureRequest();
        lectureRequest.setName("NewLectureName");
        lectureRequest.setGradeName("NewGradeName");
        lectureRequest.setSubject("NewSubject");

        when(lectureRepository.findByName("ExistingLecture")).thenReturn(Optional.of(existingLecture));
        when(gradeRepository.findByName("NewGradeName")).thenReturn(Optional.of(new Grade("NewGradeName")));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/lectures/editLecture/{lectureName}", "ExistingLecture")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"NewLectureName\", \"gradeName\":\"NewGradeName\", \"subject\":\"NewSubject\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content: " + result.getResponse().getContentAsString());

        assertEquals(200, result.getResponse().getStatus());
    }
    
    @Test
    @WithMockUser(roles = "MODERATOR")
    public void testDeleteLecture() throws Exception {
        doNothing().when(lectureService).deleteLectureByName("ExistingLecture");
        
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/lectures/{lectureName}", "ExistingLecture"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content: " + result.getResponse().getContentAsString());

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void testDeleteLectureNotFound() throws Exception {

        doThrow(new ResourceNotFoundException("Lecture not found")).when(lectureService).deleteLectureByName("NonExistingLecture");
        
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/lectures/{lectureName}", "NonExistingLecture"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content: " + result.getResponse().getContentAsString());

        assertEquals(404, result.getResponse().getStatus());
    }
   
    private static String asJsonString(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }
}
