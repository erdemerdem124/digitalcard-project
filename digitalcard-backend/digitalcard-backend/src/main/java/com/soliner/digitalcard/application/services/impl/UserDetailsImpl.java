package com.soliner.digitalcard.application.services.impl;

import com.soliner.digitalcard.domain.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore; // Şifrenin JSON'a serileştirilmesini engellemek için
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Spring Security'nin UserDetails arayüzünün özel implementasyonu.
 * Bu sınıf, kimlik doğrulama ve yetkilendirme sırasında kullanıcıya ait
 * özel bilgileri (ID, email, ad, soyad vb.) taşır.
 */
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    @JsonIgnore // Şifrenin JSON yanıtlarında görünmesini engeller
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    // Constructor: Tüm gerekli alanları alır
    public UserDetailsImpl(Long id, String username, String email, String firstName, String lastName,
                           String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.authorities = authorities;
    }

    // User entity'sinden UserDetailsImpl objesi oluşturmak için yardımcı metod
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPasswordHash(), // Hashlenmiş şifreyi kullan
                authorities);
    }

    // Getter metodları
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    // UserDetails arayüz metodları
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Varsayılan olarak true
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Varsayılan olarak true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Varsayılan olarak true
    }

    @Override
    public boolean isEnabled() {
        return true; // Varsayılan olarak true
    }

    // equals ve hashCode metodları (objelerin karşılaştırılması için önemlidir)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
