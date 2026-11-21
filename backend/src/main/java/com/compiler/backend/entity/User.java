package com.compiler.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL
    )
    private UserFolder rootFolder;
    @Enumerated(EnumType.STRING)
    private Gender gender;
}