package com.ktsa.foosball;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FoosballApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoosballApplication.class, args);
	}

}
