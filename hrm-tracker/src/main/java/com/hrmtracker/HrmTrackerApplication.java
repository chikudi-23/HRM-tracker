package com.hrmtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling

@SpringBootApplication
public class HrmTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrmTrackerApplication.class, args);
	}

}
