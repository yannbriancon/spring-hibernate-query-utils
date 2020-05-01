package com.yannbriancon.utils.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class DomainUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brother_id")
    private DomainUser brother;

    public DomainUser() {
    }

    public DomainUser(String name) {
        this.name = name;
    }

    public DomainUser(String name, DomainUser brother) {
        this.name = name;
        this.brother = brother;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DomainUser getBrother() {
        return brother;
    }

    public void setBrother(DomainUser brother) {
        this.brother = brother;
    }
}
