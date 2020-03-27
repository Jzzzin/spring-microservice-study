package com.bloknoma.organization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class Application {
    /*
        @Bean
        public Filter userContextFilter() {
            UserContextFilter userContextFilter = new UserContextFilter();
            return userContextFilter;
        }
    */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
