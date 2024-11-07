package com.loveforest.loveforest.domain.pet.entity;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_pet")
@Getter
@NoArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int level = 1;

    @Column(nullable = false)
    private int experience = 0;

    @Column(name = "name", length = 20)
    private String name = "동글이";  // 기본 이름 설정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    public Pet(Couple couple) {
        this.couple = couple;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void addExperience(int exp) {
        this.experience += exp;
    }

    public void incrementLevel() {
        if (this.level < 20) {
            this.level++;
        }
    }

    public void resetExperience() {
        this.experience = 0;
    }
}