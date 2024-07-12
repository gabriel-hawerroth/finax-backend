package br.finax.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @NotBlank
    @Column(nullable = false, length = 40)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String color;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String icon;

    @NotBlank
    @Column(nullable = false, length = 1, updatable = false)
    private String type;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean essential;

    public Category(String name, String color, String icon, String type, boolean essential) {
        this.id = null;
        this.userId = null;
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.type = type;
        this.active = true;
        this.essential = essential;
    }
}
