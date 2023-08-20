package edu.platform.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
