package edu.platform.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String avatarUrl;
}
