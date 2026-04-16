package com.example.liquidbase.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

//@Service
public class Consumer {
    @KafkaListener(
            topics = "foo", groupId = "my-group")
    public void listenToPartition(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        System.out.println(
                "Received Message: " + message
                        + "from partition: " + partition + " | Offset: " + offset);
        try {
            //Check xem đã nhận message này chưa (truy vấn DB, ...)
            if (isAlreadyProcessed(message)) {
                ack.acknowledge(); // Commit offset để bỏ qua
                return;
            }
            //Xử lý bussiness logic
            processBusinessLogic(message);

            ack.acknowledge();
        } catch (Exception e) {
            System.out.println("Error while listening to partition: " + partition);
            throw e;
        }
    }

    private boolean isAlreadyProcessed(String message) {
        //Implement logic kiểm tra trong DB/Redis xem message_id này đã tồn tại chưa
        return false;
    }

    private void processBusinessLogic(String message) {
        //Logic nghiệp vụ thực tế
    }
}
