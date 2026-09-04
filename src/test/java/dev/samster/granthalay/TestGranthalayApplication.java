package dev.samster.granthalay;

import org.springframework.boot.SpringApplication;

public class TestGranthalayApplication {

	public static void main(String[] args) {
		SpringApplication.from(GranthalayApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
