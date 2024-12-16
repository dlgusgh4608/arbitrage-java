package main.arbitrage.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`user`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "email", nullable = false, length = 30, unique = true)
    private String email;

    @Column(name = "nickname", nullable = false, length = 60, unique = true)
    private String nickname;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Builder
    public User(Long userId, String password, String email, String nickname) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
    }

    public void updateUserNickname(String nickname) {
        this.nickname = nickname;
    }

}