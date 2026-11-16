package com.agitex.catalog.tag;

import jakarta.persistence.*;

@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    protected Tag() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
