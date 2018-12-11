package com.masteryoim.whatsapp.controller;

import com.masteryoim.whatsapp.service.WhatsappWebAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/login")
@Controller
public class LoginWhatsappController {
    private static Logger log = LoggerFactory.getLogger(LoginWhatsappController.class);

    WhatsappWebAgent whatsappWebAgent;

    @Autowired
    public LoginWhatsappController(WhatsappWebAgent whatsappWebAgent) {
        this.whatsappWebAgent = whatsappWebAgent;
    }

    @RequestMapping(value = "/barcode")
    public String loginBarcode(ModelMap model) {
        log.info("get login barcode");

        model.put("barcode", whatsappWebAgent.getLoginBarcode());
        return "barcode";
    }

    @RequestMapping(value = "/check")
    public ResponseEntity<String> check() {
        if (whatsappWebAgent.isLoggedIn())
            return ResponseEntity.ok("ok");
        else
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("not logged");
    }
}
