package com.majk.spring.security.postgresql.controllers;

import com.majk.spring.security.postgresql.payload.response.UserCheckResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.majk.spring.security.postgresql.models.ERole;
import com.majk.spring.security.postgresql.models.Role;
import com.majk.spring.security.postgresql.models.User;
import com.majk.spring.security.postgresql.payload.request.LoginRequest;
import com.majk.spring.security.postgresql.payload.request.SignupRequest;
import com.majk.spring.security.postgresql.payload.response.JwtResponse;
import com.majk.spring.security.postgresql.payload.response.MessageResponse;
import com.majk.spring.security.postgresql.repository.LectureRepository;
import com.majk.spring.security.postgresql.repository.RoleRepository;
import com.majk.spring.security.postgresql.repository.UserRepository;
import com.majk.spring.security.postgresql.security.jwt.JwtUtils;
import com.majk.spring.security.postgresql.security.services.ResourceNotFoundException;
import com.majk.spring.security.postgresql.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
      
@Tag(name = "AuthController", description = "Kontroler zarządzający użytkownikami, głównie autoryzacją, informacjami o rolach itd.")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  LectureRepository lectureRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;
  
  @Operation(
      summary = "Logowanie",
      description = "Użytkownik loguje się podając swoje dane, otrzymuje token jwt od kontrolera.",
      tags = { "Autoryzacja"})
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
        .collect(Collectors.toList());

    return ResponseEntity
        .ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
  }


  @Operation(
      summary = "Utworzenie nowego konta",
      description = "Tworzone jest nowe konto użytkownika z rolą ucznia.",
      tags = { "Autoryzacja"})
  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Błąd: Nazwa użytkownika jest już zajęta!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Błąd: Email jest już w użyciu!"));
    }

    String password = signUpRequest.getPassword();
    if (!isPasswordSecure(password)) {
        return ResponseEntity.badRequest().body(new MessageResponse("Błąd: Hasło nie spełnia wymagań bezpieczeństwa!"));
    }
    
    User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getName(),
        signUpRequest.getSurname(), encoder.encode(signUpRequest.getPassword()));

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
          .orElseThrow(() -> new RuntimeException("Błąd: Rola nie została znaleziona."));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
        case "admin":
          Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
              .orElseThrow(() -> new RuntimeException("Błąd: Rola nie została znaleziona."));
          roles.add(adminRole);
          break;
        case "mod":
          Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
              .orElseThrow(() -> new RuntimeException("Błąd: Rola nie została znaleziona."));
          roles.add(modRole);
          break;
        default:
          Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Błąd: Rola nie została znaleziona."));
          roles.add(userRole);
        }
      });
    }

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("Użytkownik zarejestrowany pomyślnie!"));
  }
  
  private boolean isPasswordSecure(String password) {
    int minLength = 8;
    boolean hasUppercase = !password.equals(password.toLowerCase());
    boolean hasLowercase = !password.equals(password.toUpperCase());
    boolean hasDigit = password.matches(".*\\d.*");
    boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

    return password.length() >= minLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
}

    @Operation(
      summary = "Sprawdzenie roli",
      description = "Operacja uzyskania roli użytkownika za pomocą jego emaila. Rola jest zwracana w postaci Id roli.",
      tags = { "Autoryzacja"})
  @GetMapping("/check")
  @PreAuthorize("hasRole('ROLE_ADMIN')") // Dostosuj rolę, jeśli to konieczne
  public ResponseEntity<?> checkUser(@RequestParam String userEmail) {
    try {
      User user = userRepository.findByEmail(userEmail)
          .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie znaleziony: " + userEmail));

      UserCheckResponse response = new UserCheckResponse(user.getEmail(),
          user.getRoles().stream().findFirst().orElseThrow().getId());

      return ResponseEntity.ok(response);
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Błąd w sprawdzaniu użytkownika: " + e.getMessage());
    }
  }

      @Operation(
      summary = "Aktualizacja",
      description = "Operacja zmiany roli użytkownika na podstawie jego emaila. "
              + "Rola użytkownika jest zmieniana w zależności od tego jaką rolę posiada obecnie, "
              + "rola może być obniżona do zwykłego użytkownika lub podniesiona do moderatora (nauczyciela).",
      tags = { "Autoryzacja"})
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("/update")
  public ResponseEntity<String> updateUser(@RequestParam String userEmail) {
    try {
      User user = userRepository.findByEmail(userEmail)
          .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie znaleziony: " + userEmail));

      Integer userRoleId = user.getRoles().stream().findFirst().orElseThrow().getId();

      if (userRoleId.equals(1)) {
        user.getRoles().clear();
        user.getRoles().add(roleRepository.findById(2L).orElseThrow());
        user.getGrades().clear(); 
      } else if (userRoleId.equals(2)) {
        user.getRoles().clear();
        user.getRoles().add(roleRepository.findById(1L).orElseThrow());
        user.getLectures().forEach(lectureRepository::delete);
      }
      userRepository.save(user);
      return ResponseEntity.ok("Użytkownik zaktualizowany");
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Błąd w aktualizowaniu użytkownika: " + e.getMessage());
    }
  }
}
