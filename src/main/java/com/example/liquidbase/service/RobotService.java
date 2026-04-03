package com.example.liquidbase.service;

import com.example.liquidbase.repository.RobotRepository;
import org.springframework.stereotype.Service;

@Service
public class RobotService {
    private final RobotRepository robotRepository;

    public RobotService(RobotRepository robotRepository) {
        this.robotRepository = robotRepository;
    }
}
