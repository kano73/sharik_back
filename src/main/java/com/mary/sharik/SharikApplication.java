package com.mary.sharik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SharikApplication {

	public static void main(String[] args) {
		SpringApplication.run(SharikApplication.class, args);
	}

}
