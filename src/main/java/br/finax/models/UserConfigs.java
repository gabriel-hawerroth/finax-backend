package br.finax.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "user_configs")
public class UserConfigs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String theme;

    @Column(name = "adding_material_goods_to_patrimony", nullable = false)
    private boolean addingMaterialGoodsToPatrimony;

    @NotBlank
    @Column(nullable = false, length = 5)
    private String language;

    @NotBlank
    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "releases_view_mode", nullable = false, length = 8)
    private String releasesViewMode;
}
