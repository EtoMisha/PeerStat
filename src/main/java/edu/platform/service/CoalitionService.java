package edu.platform.service;

import edu.platform.repository.CoalitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CoalitionService {
    private final CoalitionRepository coalitionRepository;

}
