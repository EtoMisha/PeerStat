package edu.platform.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    private Long id;
    private String category;
}
