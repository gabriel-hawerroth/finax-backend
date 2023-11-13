package br.finax.finax.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String access;

    private boolean activate;

    @Column(name = "can_change_password")
    private Boolean canChangePassword;

    private String signature;

    @Column(name = "signature_expiration")
    private Date signatureExpiration;

    @Column(name = "profile_image")
    private byte[] profileImage;
}
