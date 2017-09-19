package com.thepointmoscow.frws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.thepointmoscow.frws")
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }
}
