package com.example.liquidbase.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class Consumer {
    @KafkaListener(
            topics = "foo", groupId = "my-group")
    public String listenToPartition(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        System.out.println(
                "Received Message: " + message
                        + "from partition: " + partition + " | Offset: " + offset);
        try {
            ack.acknowledge();
            Thread.sleep(10000);
        } catch (Exception e) {
            System.out.println("Error while listening to partition: " + partition);
        }
        return "Received Message: " + message + "from partition: " + partition;
    }
}
