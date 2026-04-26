package com.example.Pharmacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//= @Configuration + @EnableAutoConfiguration + @ComponentScan
@SpringBootApplication

//Search/Scan all Beans (Component, Service, Repository, Entity, Controller) from Package
@ComponentScan(basePackages = "com.example.Pharmacy")

//Search/Scan all Entities
@EntityScan(basePackages = "com.example.Pharmacy.Entities")

//Search/Scan All Repository
@EnableJpaRepositories(basePackages = "com.example.Pharmacy.Repositories")
public class DemoApplication {

    public static void main(String[] args) {

        SpringApplication.run(DemoApplication.class, args);
    }

}
