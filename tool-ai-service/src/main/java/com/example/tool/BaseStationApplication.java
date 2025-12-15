package com.example.tool;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.tool.**.mapper*")
@ConfigurationProperties
@EnableScheduling
public class BaseStationApplication {
	public static void main(String[] args) {
		SpringApplication.run(BaseStationApplication.class, args);
	}
}
