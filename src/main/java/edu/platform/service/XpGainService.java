package edu.platform.service;

import edu.platform.repository.XpGainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class XpGainService {
    private final XpGainRepository xpGainRepository;
}
