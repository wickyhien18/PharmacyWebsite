package Pharmacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//= @Configuration + @EnableAutoConfiguration + @ComponentScan
@SpringBootApplication

//Search/Scan all Beans (Component, Service, Repository, Entity, Controller) from Package
@ComponentScan(basePackages = "Pharmacy")

//Search/Scan all Entities
@EntityScan(basePackages = "Pharmacy.Entities")

//Search/Scan All Repository
@EnableJpaRepositories(basePackages = "Pharmacy.Repositories")
public class DemoApplication {

    public static void main(String[] args) {

        SpringApplication.run(DemoApplication.class, args);
    }

}
