package com.masteryoim.whatsapp;

import com.masteryoim.whatsapp.model.SendMessageRequest;
import com.masteryoim.whatsapp.service.WhatsappWebAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
@EnableScheduling
public class WhatsappApplication {
	private static Logger log = LoggerFactory.getLogger(WhatsappApplication.class);

	@Value("${schedule.whatsapp.test.enable:false}")
	boolean enableWhatsappTest;

	@Value("${schedule.whatsapp.test.phoneNumbers:''}")
	String phoneNumbers;

	@Bean
	public WhatsappWebAgent whatsappWebAgent() {
		return new WhatsappWebAgent();
	}

	@Bean
	public LinkedBlockingQueue<SendMessageRequest> messageQueue() {
		return new LinkedBlockingQueue<>();
	}

	@Scheduled(cron = "${schedule.whatsapp.test.cron}")
	public void testWhatsappJob() {
		log.info("Start whatsapp test");
		if (enableWhatsappTest) {
			for (String phoneNumber : phoneNumbers.split(",")) {
				whatsappWebAgent().sendMsg(phoneNumber, "daily test");
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(WhatsappApplication.class, args);
	}
}
