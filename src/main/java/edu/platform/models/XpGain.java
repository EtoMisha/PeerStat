package edu.platform.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "xp_history")
public class XpGain {

    @Id
    private Long id;
    private LocalDate date;
    private int points;
}
