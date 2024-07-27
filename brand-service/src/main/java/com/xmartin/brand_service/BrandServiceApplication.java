package com.xmartin.brand_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BrandServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrandServiceApplication.class, args);
	}

}
