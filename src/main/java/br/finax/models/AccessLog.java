package br.finax.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
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

    @Column(name = "user_id", nullable = false)
    private long userId;

    @PastOrPresent
    @Column(name = "login_dt", nullable = false)
    private LocalDateTime loginDt;

    public AccessLog(long userId, LocalDateTime loginDt) {
        this.userId = userId;
        this.loginDt = loginDt;
    }
}
