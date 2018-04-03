package com.masteryoim.whatsapp.controller;

import com.masteryoim.whatsapp.model.SendMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.LinkedBlockingQueue;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class MessageController {
    private static Logger log = LoggerFactory.getLogger(MessageController .class);

    LinkedBlockingQueue<SendMessageRequest> messageQueue;

    @Autowired
    public MessageController(LinkedBlockingQueue<SendMessageRequest> messageQueue) {
        this.messageQueue = messageQueue;
    }

    @RequestMapping(value = "/sendto/{phoneNumber}", method = GET)
    public ResponseEntity sendMessage(@PathVariable String phoneNumber, @RequestParam(value = "message", defaultValue = "") String message) {
        log.info("send {} to {}", message, phoneNumber);

        if (messageQueue.offer(new SendMessageRequest(phoneNumber, message))) {
            return ResponseEntity.ok("request received");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("failed to process");
        }
    }
}
