package br.finax.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "access_log")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "login_dt")
    private LocalDateTime loginDt;

    public AccessLog(long userId, LocalDateTime loginDt) {
        this.userId = userId;
        this.loginDt = loginDt;
    }
}
