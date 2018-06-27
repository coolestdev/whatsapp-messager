package com.masteryoim.whatsapp.service;

import com.masteryoim.whatsapp.model.SendMessageRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class SendMessageConsumer {
    private static final Logger log = LoggerFactory.getLogger(SendMessageConsumer.class);

    private WhatsappWebAgent agent;
    private LinkedBlockingQueue<SendMessageRequest> queue;

    @Autowired
    public SendMessageConsumer(LinkedBlockingQueue<SendMessageRequest> queue, WhatsappWebAgent agent) {
        this.queue = queue;
        this.agent = agent;
    }

    @PostConstruct
    public void init() {
        startListeningRequest();
    }

    public void startListeningRequest() {
        Thread consumer = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    SendMessageRequest request = queue.take();
                    if (StringUtils.isNotBlank(request.groupName))
                        agent.sendToGroup(request.groupName, request.message);
                    else
                        agent.sendMsg(request.phoneNumber, request.message);
                } catch (Exception e) {
                    log.error("Error when sending message", e);
                }
            }
        });
        consumer.start();
    }
}
