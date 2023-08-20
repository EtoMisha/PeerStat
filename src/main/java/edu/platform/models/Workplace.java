package edu.platform.models;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "workplaces")
public class Workplace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String row;
    private String number;

    @OneToOne
    private User user;

    @ManyToOne
    private Cluster cluster;
}
