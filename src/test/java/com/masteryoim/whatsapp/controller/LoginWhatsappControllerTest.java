package com.masteryoim.whatsapp.controller;

import com.masteryoim.whatsapp.model.SendMessageRequest;
import com.masteryoim.whatsapp.service.SendMessageConsumer;
import com.masteryoim.whatsapp.service.WhatsappWebAgent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LoginWhatsappControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private WhatsappWebAgent whatsappWebAgent;

    @Test
    public void loginBarcode_shouldGivenBarcodeImage() throws Exception {
        when(whatsappWebAgent.getLoginBarcode()).thenReturn("http://testing.com/img.jpg");

        MvcResult result = this.mockMvc.perform(
                get("/login/barcode")
        )
                .andExpect(status().isOk())
                .andExpect(view().name("barcode"))
                .andReturn();

        assertEquals("http://testing.com/img.jpg", result.getModelAndView().getModelMap().get("barcode"));
    }

    @Test
    public void check_givenNotLogin_shouldFail() throws Exception {
        when(whatsappWebAgent.isLoggedIn()).thenReturn(false);

        this.mockMvc
                .perform(get("/login/check"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void check_givenIsLogin_shouldSuccess() throws Exception {
        when(whatsappWebAgent.isLoggedIn()).thenReturn(true);

        this.mockMvc
                .perform(get("/login/check"))
                .andExpect(status().isOk())
                .andReturn();
    }

}