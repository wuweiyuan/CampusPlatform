package com.campus.trade.campustradeserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CampusTradeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusTradeServerApplication.class, args);
    }

}
