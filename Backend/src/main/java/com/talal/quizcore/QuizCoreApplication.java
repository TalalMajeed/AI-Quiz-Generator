package com.talal.quizcore;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.ComponentScan;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;

import java.util.List;

@RestController
@SpringBootApplication(scanBasePackages = "com.talal.quizcore")
@ComponentScan
public class QuizCoreApplication {


    @Autowired
    private com.talal.quizcore.Database database;

    public static void main(String[] args) {
        SpringApplication.run(QuizCoreApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {
        return "<h1>Welcome to QuizCore API</h1><p>Powered by SpringBoot Java!</p><p>© Muhammad Talal Majeed</p>";
    }

    @PostConstruct
    public void init() {
        database.initializeDatabase();
    }
}
