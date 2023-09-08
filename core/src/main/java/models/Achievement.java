package models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    private Long id;
    private String name;
    private String avatarUrl;
}
