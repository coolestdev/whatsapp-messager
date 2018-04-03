package com.masteryoim.whatsapp.controller;

import com.masteryoim.whatsapp.model.SendMessageRequest;
import com.masteryoim.whatsapp.service.SendMessageConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MessageControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private LinkedBlockingQueue<SendMessageRequest> messageQueue;

    @MockBean private SendMessageConsumer mockSendMessageConsumer;

    @Test
    public void sendMessage_shouldPutIntoMessageQueue() throws Exception  {
        // execute
        this.mockMvc.perform(
            get("/sendto/12345678").param("message","abc%0Adef")
        ).andExpect(status().isOk());

        SendMessageRequest requestMessage = messageQueue.take();
        assertEquals("12345678", requestMessage.phoneNumber);
        assertEquals("abc%0Adef", requestMessage.message);
    }

}