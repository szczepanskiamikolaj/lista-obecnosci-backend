
package com.majk.spring.security.postgresql.controllers;

/**
 *
 * @author Majkel
 */
import com.majk.spring.security.postgresql.models.ERole;
import com.majk.spring.security.postgresql.models.Role;
import com.majk.spring.security.postgresql.models.User;
import com.majk.spring.security.postgresql.repository.RoleRepository;
import com.majk.spring.security.postgresql.repository.UserRepository;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.autoconfigure.exclude=com.majk.spring.security.postgresql.AminUserInitializer")
@AutoConfigureMockMvc
@SpringBootTest
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;
    
    @MockBean
    private AuthenticationManager authenticationManager;

        @BeforeEach
    public void setup() {
        Role roleWithId2 = new Role(2, ERole.ROLE_MODERATOR);
        when(roleRepository.findById(2L)).thenReturn(Optional.of(roleWithId2));
        Role roleWithId3 = new Role(3, ERole.ROLE_ADMIN);
        when(roleRepository.findById(3L)).thenReturn(Optional.of(roleWithId3));
    }

    @Test
    public void testRegisterUser() throws Exception {
        when(userRepository.existsByUsername("existingUsername")).thenReturn(false);
        when(userRepository.existsByEmail("existingEmail@example.com")).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(new Role(ERole.ROLE_USER)));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newUsername\", \"email\":\"newEmail@example.com\", \"password\":\"Password123!@#\", \"name\":\"John\", \"surname\":\"Doe\", \"role\":[\"user\"]}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content: " + result.getResponse().getContentAsString());

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void testRegisterUserUsernameTaken() throws Exception {
        when(userRepository.existsByUsername("existingUsername")).thenReturn(true);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"existingUsername\", \"email\":\"newEmail@example.com\", \"password\":\"password\", \"name\":\"John\", \"surname\":\"Doe\", \"role\":[\"user\"]}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content: " + result.getResponse().getContentAsString());

        assertEquals(400, result.getResponse().getStatus());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    public void testCheckUser() throws Exception {
        String userEmail = "testUser@example.com";
        String userName = "Test";
        String userSurname = "User";

        Role userRole = new Role(ERole.ROLE_USER);

        User testUser = new User();
        testUser.setEmail(userEmail);
        testUser.setName(userName);
        testUser.setSurname(userSurname);
        testUser.setRoles(Collections.singleton(userRole));

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/check")
                .param("userEmail", userEmail))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content: " + result.getResponse().getContentAsString());

        assertEquals(200, result.getResponse().getStatus());

   }
    
    @WithMockUser(roles = "ADMIN")
    @Test
    public void testUpdateUser() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("yourUsername"); 
        mockUser.setEmail("existingEmail@example.com");
        Set<Role> rolesSet = new HashSet<>(Collections.singletonList(new Role(1, ERole.ROLE_USER)));
        mockUser.setRoles(rolesSet);
        when(userRepository.findByEmail("existingEmail@example.com")).thenReturn(Optional.of(mockUser));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/auth/update")
                .param("userEmail", "existingEmail@example.com"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content: " + result.getResponse().getContentAsString());

        assertEquals(200, result.getResponse().getStatus());
    }



}
