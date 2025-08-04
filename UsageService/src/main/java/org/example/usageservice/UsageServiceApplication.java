package org.example.usageservice;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UsageServiceApplication {

    @Bean
    public Queue currentPercentageQueue() {
        return new Queue("current_percentage_mq", true);
    }

    public static void main(String[] args) {
        SpringApplication.run(UsageServiceApplication.class, args);
    }

}
