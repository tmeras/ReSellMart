package com.tmeras.resellmart.token;

import com.tmeras.resellmart.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue
    private Integer id;

    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime validatedAt;

    private boolean revoked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

}
