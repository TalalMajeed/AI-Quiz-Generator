package com.talal.quizcore;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.ComponentScan;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.List;

@CrossOrigin(origins = "*")
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

    @PostMapping("/check/login")
    public String loginStudent(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String password = data.getString("password");
            String email = data.getString("email");
            return database.getSQLStudent(email, password);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/check/user")
    public String checkUser(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String id = data.getString("id");
            String email = data.getString("email");
            return database.checkSQLStudent(email, id);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/get/quiz")
    public String getQuiz(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String id = data.getString("id");
            return database.getSQLQuiz(id);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/create/user")
    public String createUser(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String name = data.getString("name");
            String email = data.getString("email");
            String password = data.getString("password");
            String education = data.getString("education");
            String community = data.getString("community");
            return database.createSQLStudent(name, email, password, education, community);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/update/user")
    public String updateUser(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String id = data.getString("id");
            String name = data.getString("name");
            String education = data.getString("education");
            String community = data.getString("community");
            String image;
            String description;
            try {
                image = data.getString("image");
            }
            catch (Exception e) {
                image = null;
            }
            try {
                description = data.getString("description");
            }
            catch (Exception e) {
                description = null;
            }
            System.out.println();
            return database.updateSQLStudent(id, name, education, community, image, description);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/delete/user")
    public String deleteUser(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String id = data.getString("id");
            return database.deleteSQLStudent(id);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/generate/quiz")
    public String generateQuiz(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            int amount = data.getInt("amount");
            int difficulty = data.getInt("difficulty");
            String subject = data.getString("subject");
            String details = data.getString("details");
            return MCQGenerator.generateMCQs(amount, difficulty, subject, details).toString();
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/save/quiz")
    public String saveQuiz(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String name = data.getString("name");
            String password;
            try {
                password = data.getString("password");
            }
            catch (Exception e) {
                password = null;
            }

            String creator = data.getString("creator");
            Boolean review = data.getBoolean("review");
            String data2 = data.getString("data");
            int duration = data.getInt("duration");
            return database.createSQLQuiz(name, password, creator, review, data2, duration);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }


    @PostConstruct
    public void init() {
        database.initializeDatabase();
    }
}
