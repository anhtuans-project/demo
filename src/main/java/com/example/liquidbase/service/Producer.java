package com.example.liquidbase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        try {
            kafkaTemplate.send("foo", message);
            System.out.println("Sent message=[" + message);
        } catch (Exception e) {
            System.out.println("Unable to send message=[" +
                    message + "] due to : " + e.getMessage());
        }
    }
}

