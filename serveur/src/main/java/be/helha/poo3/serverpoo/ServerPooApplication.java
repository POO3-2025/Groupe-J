package be.helha.poo3.serverpoo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServerPooApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerPooApplication.class, args);
	}
}
