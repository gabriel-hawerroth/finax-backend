package br.finax.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "access_log")
public final class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private long userId;

    @PastOrPresent
    @Column(name = "login_dt", nullable = false, updatable = false)
    private LocalDateTime loginDt;

    public AccessLog(long userId, LocalDateTime loginDt) {
        this.userId = userId;
        this.loginDt = loginDt;
    }
}
