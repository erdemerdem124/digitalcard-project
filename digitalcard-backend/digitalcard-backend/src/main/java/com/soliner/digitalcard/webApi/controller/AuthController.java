package com.soliner.digitalcard.webApi.controller;

import com.soliner.digitalcard.application.services.interfaces.UserService;
import com.soliner.digitalcard.application.services.impl.UserDetailsImpl;
import com.soliner.digitalcard.webApi.dto.auth.LoginRequest;
import com.soliner.digitalcard.webApi.dto.auth.LoginResponse;
import com.soliner.digitalcard.webApi.dto.auth.MessageResponse;
import com.soliner.digitalcard.webApi.dto.auth.RegisterRequest;
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.security.JwtUtils;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Kimlik doğrulama ve kullanıcı kayıt/giriş işlemlerini yöneten REST Controller.
 * Bu sınıf, kullanıcıların sisteme kayıt olmasını ve giriş yapmasını sağlayan endpoint'leri içerir.
 * webApi katmanına aittir.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    // Constructor Injection: Gerekli tüm bağımlılıklar enjekte edildi.
    public AuthController(AuthenticationManager authenticationManager, UserService userService,
                          JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Kullanıcı girişi için endpoint.
     * @param loginRequest Giriş bilgileri (username/email ve password).
     * @return JWT token ve kullanıcı bilgileri içeren LoginResponse.
     */
    // KRİTİK DÜZELTME: Endpoint'i /login olarak değiştirildi.
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Kullanıcı giriş isteği alındı: {}", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        log.info("Kullanıcı başarıyla giriş yaptı: {}", userDetails.getUsername());
        
        return ResponseEntity.ok(new LoginResponse(jwt,
                "Bearer",
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                roles));
    }

    /**
     * Yeni kullanıcı kaydı için endpoint.
     * @param registerRequest Kayıt bilgileri (username, email, password, firstName, lastName).
     * @return Başarı mesajı içeren MessageResponse.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Kullanıcı kayıt isteği alındı: {}", registerRequest.getUsername());

        UserRequest userRequest = registerRequest.toUserRequest();
        userService.createUser(userRequest);
        
        log.info("Kullanıcı başarıyla kaydedildi: {}", registerRequest.getUsername());
        return ResponseEntity.ok(new MessageResponse("Kullanıcı başarıyla kaydedildi!"));
    }
}
