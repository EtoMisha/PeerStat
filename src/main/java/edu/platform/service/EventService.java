package edu.platform.service;

import edu.platform.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EventService {
    private final EventRepository eventRepository;

}
