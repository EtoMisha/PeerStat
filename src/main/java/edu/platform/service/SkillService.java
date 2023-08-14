package edu.platform.service;

import edu.platform.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SkillService {
    private final SkillRepository skillRepository;

}
