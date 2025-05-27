package com.soliner.digitalcard.webApi.controller;

import com.soliner.digitalcard.application.services.interfaces.UserService;
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;
import com.soliner.digitalcard.webApi.dto.auth.LoginRequest;
import com.soliner.digitalcard.webApi.dto.auth.LoginResponse;
import com.soliner.digitalcard.webApi.dto.auth.PasswordUpdateRequest;
import com.soliner.digitalcard.application.services.impl.UserDetailsServiceImpl;
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
import com.soliner.digitalcard.webApi.security.JwtUtils;
import com.soliner.digitalcard.application.services.impl.UserDetailsImpl; // UserDetailsImpl import edildi

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // Sadece tip için kalsın
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kimlik doğrulama (kayıt ve giriş) işlemlerini yöneten Controller sınıfı.
 * webApi katmanına aittir.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    // Constructor Injection
    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Yeni bir kullanıcı kaydı oluşturur.
     * Endpoint: /api/auth/register
     * @param userRequest Kayıt olacak kullanıcının bilgilerini içeren DTO.
     * @return Oluşturulan kullanıcıya ait UserResponse ve 201 Created durumu.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse registeredUser = userService.createUser(userRequest);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    /**
     * Kullanıcı girişi yapar ve kimlik doğrulaması gerçekleştirir.
     * Endpoint: /api/auth/login
     * @param loginRequest Giriş bilgilerini (username/email, password) içeren DTO.
     * @return Başarılı giriş durumunda LoginResponse ve 200 OK durumu.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // UserDetailsImpl'e cast ediyoruz, çünkü UserDetails arayüzünde getId, getEmail vs. yoktur.
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateJwtToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                                     .map(authority -> authority.getAuthority())
                                     .collect(Collectors.toList());

        // LoginResponse constructor'ını, UserDetailsImpl'den alınan tüm gerekli bilgilerle çağır
        LoginResponse loginResponse = new LoginResponse(
            jwtToken,
            "Bearer",
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            userDetails.getFirstName(),
            userDetails.getLastName(),
            roles
        );
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Kullanıcının şifresini günceller.
     * Endpoint: /api/auth/users/{userId}/password
     * HTTP Metodu: PUT
     * @param userId Güncellenecek kullanıcının ID'si (URL yolundan alınır).
     * @param passwordUpdateRequest Mevcut ve yeni şifreyi içeren DTO.
     * @param principal Oturum açmış kullanıcının kimlik bilgileri.
     * @return Başarılı güncelleme durumunda 200 OK veya hata durumunda 403 Forbidden.
     */
    @PutMapping("/users/{userId}/password")
    public ResponseEntity<String> updatePassword(@PathVariable Long userId, @Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest, Principal principal) {
        try {
            // Oturum açmış kullanıcının ID'sini almak için UserDetailsServiceImpl kullanıyoruz.
            // principal.getName() ile gelen username'i kullanarak UserDetailsImpl objesini çekiyoruz.
            UserDetailsImpl authenticatedUserDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(principal.getName());
            Long authenticatedUserId = authenticatedUserDetails.getId();

            // URL'den gelen userId ile oturum açmış kullanıcının ID'sini karşılaştır
            if (!authenticatedUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bu kullanıcıya ait şifreyi güncelleme yetkiniz yok.");
            }

            // Şifre güncelleme servis metodunu çağır
            userService.updatePassword(userId, passwordUpdateRequest.getCurrentPassword(), passwordUpdateRequest.getNewPassword());

            return ResponseEntity.ok("Şifre başarıyla güncellendi.");

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Şifre güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }
}
