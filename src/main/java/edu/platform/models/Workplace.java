package edu.platform.models;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "workplaces")
public class Workplace {
    @Id
    private String row;
    private String number;

    @OneToOne
    private User user;

    @ManyToOne
    private Cluster cluster;
}
