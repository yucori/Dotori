package com.dotori.dotori.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 25)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;  // 예: ROLE_USER, ROLE_ADMIN

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // -----------------------------------
    // 🔐 UserDetails 구현
    // -----------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role)); // "ROLE_USER" 등
    }

    @Override
    public String getUsername() {
        return email; // 로그인 ID는 email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 관리 필요 없으면 true
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 잠금 관리 필요 없으면 true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 관리 필요 없으면 true
    }

    @Override
    public boolean isEnabled() {
        return true; // 탈퇴/정지 처리 없으면 true
    }
}
