package com.majk.spring.security.postgresql;



import com.majk.spring.security.postgresql.models.ERole;
import com.majk.spring.security.postgresql.models.Role;
import com.majk.spring.security.postgresql.models.User;
import com.majk.spring.security.postgresql.repository.RoleRepository;
import com.majk.spring.security.postgresql.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminUserInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
public void initializeAdminUser() {
    createRoleIfNotExists(ERole.ROLE_USER);
    createRoleIfNotExists(ERole.ROLE_MODERATOR);
    createRoleIfNotExists(ERole.ROLE_ADMIN);

    System.out.println(roleRepository.findAllRoleIds());

    String adminUsername = "systemadmin";

    if (!userRepository.findByUsername(adminUsername).isPresent()) {
        try {
            String adminPassword = getAdminPasswordFromEnvironment();
            String adminEmail = getAdminEmailFromEnviroment();

            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setName("sys"); adminUser.setSurname("admin"); adminUser.setEmail(adminEmail);
            
            Role adminRole = roleRepository.findById(3L)
                    .orElseGet(() -> {
                        log.error("Admin role not found. Admin user will be created without the admin role.");
                        return null; 
                    });

            if (adminRole != null) {
                adminUser.setRoles(Collections.singleton(adminRole));
                userRepository.save(adminUser);
                System.out.println("Admin user created successfully.");
            }
        } catch (Exception e) {
            log.error("An error occurred while creating the admin user.", e);
        }
    }
}

    
    private void createRoleIfNotExists(ERole roleName) {
        if (!roleRepository.findByName(roleName).isPresent()) {
            Role role = new Role(roleName);
            roleRepository.save(role);
        }
    }

    private String getAdminPasswordFromEnvironment() {
        String adminPassword = System.getenv("ADMIN_PASSWORD");
        return adminPassword != null ? adminPassword : "TheEscapee3125!!!";
    }

    private String getAdminEmailFromEnviroment() {
        String adminPassword = System.getenv("ADMIN_EMAIL");
        return adminPassword != null ? adminPassword : "admin@email.com";
    }
}

