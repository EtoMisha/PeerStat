package models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "workplaces")
public class Workplace {

    @Id
    private String id;
    private String row;
    private int number;

    @OneToOne
    private User user;

    @ManyToOne
    private Cluster cluster;

    public String getFullName() {
        return cluster.getName() + "-" + row + number;
    }
}
