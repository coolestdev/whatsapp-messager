package com.masteryoim.whatsapp.controller;

import com.masteryoim.whatsapp.model.SendMessageRequest;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;

public class MessageControllerTest {

    @Test
    public void sendMessage_shouldPutIntoMessageQueue() throws InterruptedException {
        LinkedBlockingQueue<SendMessageRequest> queue = new LinkedBlockingQueue<>();

        MessageController controller = new MessageController(queue);
        controller.sendMessage("test", "12345678");

        // verify
        SendMessageRequest request = queue.take();
        assertEquals("test", request.message);
        assertEquals("12345678", request.phoneNumber);
    }
}