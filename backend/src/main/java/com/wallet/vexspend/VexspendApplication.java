package com.wallet.vexspend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public class VexspendApplication {

	public static void main(String[] args) {
		Locale.setDefault(Locale.ROOT);
		SpringApplication.run(VexspendApplication.class, args);
	}

}


