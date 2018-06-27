package com.masteryoim.whatsapp.service;

import com.masteryoim.whatsapp.model.SendMessageRequest;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SendMessageConsumerTest {
    private static Logger log = LoggerFactory.getLogger(SendMessageConsumerTest.class);

    @Test
    public void startListeningRequest_shouldCallAgentByMessage() throws InterruptedException {
        LinkedBlockingQueue<SendMessageRequest> queue = new LinkedBlockingQueue<>();
        WhatsappWebAgent mockAgent = Mockito.mock(WhatsappWebAgent.class);

        SendMessageConsumer consumer = new SendMessageConsumer(queue, mockAgent);
        consumer.startListeningRequest();
        queue.offer(SendMessageRequest.buildRequestByPhoneNumber("12345678", "test1"));
        queue.offer(SendMessageRequest.buildRequestByPhoneNumber("12345678", "test2"));

        while (queue.size() > 0) {
            log.info("{}", queue.size());
            Thread.sleep(100);
        }
        verify(mockAgent, times(2)).sendMsg(any(), any());
    }

    @Test
    public void startListeningRequest_shouldCallSendToGroupByMessage() throws InterruptedException {
        LinkedBlockingQueue<SendMessageRequest> queue = new LinkedBlockingQueue<>();
        WhatsappWebAgent mockAgent = Mockito.mock(WhatsappWebAgent.class);

        SendMessageConsumer consumer = new SendMessageConsumer(queue, mockAgent);
        consumer.startListeningRequest();
        queue.offer(SendMessageRequest.buildRequestByGroupName("Group XYZ", "test1"));
        queue.offer(SendMessageRequest.buildRequestByGroupName("Group ABC", "test2"));

        while (queue.size() > 0) {
            log.info("{}", queue.size());
            Thread.sleep(100);
        }
        verify(mockAgent, times(2)).sendToGroup(any(), any());
    }
}