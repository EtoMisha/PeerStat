package edu.platform.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "clusters")
public class Cluster {

    @Id
    private Long id;
    private String name;
    private int capacity;

    @ManyToOne
    private Campus campus;

    @OneToMany(mappedBy = "cluster")
    private List<Workplace> workplaces;
}
