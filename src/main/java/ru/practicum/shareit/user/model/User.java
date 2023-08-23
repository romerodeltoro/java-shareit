package ru.practicum.shareit.user.model;


import lombok.*;
import ru.practicum.shareit.item.model.Item;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Getter @Setter @ToString
@Builder
@Entity
@Table(name = "users", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /*@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private Set<Item> items = new HashSet<>();*/

    /*@Enumerated(EnumType.STRING)
    private UserState state;

    private enum UserState {
        ACTIVE, BLOCKED, DELETED;
    }*/

   /* @ElementCollection
    @CollectionTable(name = "items", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "item_id")
    private Set<Long> items = new HashSet<>();*/

}
