package com.masteryoim.whatsapp.controller;

import com.masteryoim.whatsapp.whatsapp.WhatsappWebAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class MessageController {
    private static Logger log = LoggerFactory.getLogger(MessageController .class);

    @Autowired
    WhatsappWebAgent agent;

    @RequestMapping(value = "/send/{message}/to/{phoneNumber}", method = GET)
    public ResponseEntity searchImage(@PathVariable String message, @PathVariable String phoneNumber) {
        log.info("send {} to {}", message, phoneNumber);

        if (agent.sendMsg(phoneNumber, message)) {
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("failed");
        }
    }
}
