package edu.platform.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "coalitions")
public class Coalition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    private Campus campus;

    @OneToMany(mappedBy = "coalition")
    private List<User> users;
}
