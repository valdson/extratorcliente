package com.br.norris;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NorrisApplication {

	public static void main(String[] args) {
		SpringApplication.run(NorrisApplication.class, args);
	}

}
