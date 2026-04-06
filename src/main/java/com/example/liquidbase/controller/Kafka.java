package com.example.liquidbase.controller;

import com.example.liquidbase.service.Consumer;
import com.example.liquidbase.service.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kafka")
public class Kafka {
    @Autowired
    private Producer producer;

    @PostMapping("/produce")
    public void produce(@RequestBody List<String> messages) {
        for (String msg : messages) {
            producer.sendMessage(msg);
        }
    }
}
