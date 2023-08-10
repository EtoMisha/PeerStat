package edu.platform.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "events")
public class Event {

    @Id
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private String type;
    private String title;
    private String description;
    private int maxParticipants;
    private int registered;

    @ManyToOne
    private User author;
}
