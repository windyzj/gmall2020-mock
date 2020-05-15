package com.atgugu.gmall2020.mock.log;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Gmall2020MockLogApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Gmall2020MockLogApplication.class, args);
        MockTask mockTask = context.getBean(MockTask.class);

        mockTask.mainTask();
    }
}
