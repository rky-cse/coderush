package me.rkycse.coderush;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoderushApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoderushApplication.class, args);
	}

}
