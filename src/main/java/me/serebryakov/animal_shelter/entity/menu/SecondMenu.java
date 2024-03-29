package me.serebryakov.animal_shelter.entity.menu;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Класс хранит пукнты меню второго уровня
 */
@Entity
@Table(name = "second_menu")
@Data
public class SecondMenu {
    @Id
    @Column(name = "second_menu_id")
    private int id;

    @Column(name = "menu_item")
    private String item;

    @ManyToOne()
    @JoinColumn(name = "main_menu_id", referencedColumnName = "main_menu_id")
    private MainMenu mainMenuId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "secondMenu")
    private Set<Info> infoSet = new HashSet<>();
}
