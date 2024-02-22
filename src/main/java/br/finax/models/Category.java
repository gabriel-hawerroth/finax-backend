package br.finax.models;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id")
    private long userId;

    private String name;

    private String color;

    private String icon;

    private String type;

    private boolean active;

    private boolean essential;
}
