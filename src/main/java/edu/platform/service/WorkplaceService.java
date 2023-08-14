package edu.platform.service;

import edu.platform.repository.WorkplaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WorkplaceService {
    private final WorkplaceRepository workplaceRepository;

}
