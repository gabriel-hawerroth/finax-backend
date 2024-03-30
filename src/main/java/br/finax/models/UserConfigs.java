package br.finax.models;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "user_configs")
public class UserConfigs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id")
    private long userId;

    private String theme;

    @Column(name = "adding_material_goods_to_patrimony")
    private boolean addingMaterialGoodsToPatrimony;

    private String language;

    private String currency;

    @Column(name = "releases_view_mode")
    private String releasesViewMode;
}
