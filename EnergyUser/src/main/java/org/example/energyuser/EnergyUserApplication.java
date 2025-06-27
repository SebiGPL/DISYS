package org.example.energyuser;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EnergyUserApplication {

    @Bean
    public Queue producerQueue() {
        return new Queue("user_mq", true);
    }

    public static void main(String[] args) {
        SpringApplication.run(EnergyUserApplication.class, args);
    }

}
