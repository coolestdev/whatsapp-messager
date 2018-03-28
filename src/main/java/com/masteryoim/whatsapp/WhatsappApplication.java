package com.masteryoim.whatsapp;

import com.masteryoim.whatsapp.model.SendMessageRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
public class WhatsappApplication {

	@Bean
	public LinkedBlockingQueue<SendMessageRequest> messageQueue() {
		return new LinkedBlockingQueue<>();
	}

	public static void main(String[] args) {
		SpringApplication.run(WhatsappApplication.class, args);
	}
}
