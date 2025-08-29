package fr.medilabo.solutions.rapport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class rapportApplication {

	public static void main(String[] args) {
		SpringApplication.run(rapportApplication.class, args);
	}

}
