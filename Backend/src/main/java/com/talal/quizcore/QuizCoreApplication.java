package com.talal.quizcore;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.parameters.P;
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
            String id;
            try {
                password = data.getString("password");
            }
            catch (Exception e) {
                password = null;
            }

            try {
                id = data.getString("id");
            }
            catch (Exception e) {
                id = null;
            }

            String creator = data.getString("creator");
            Boolean review = data.getBoolean("review");
            String data2 = data.getString("data");
            int duration = data.getInt("duration");
            return database.createSQLQuiz(name, password, creator, review, data2, duration,id);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/create/attempt")
    public String createAttempt(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String studentid = data.getString("studentid");
            String quizid = data.getString("quizid");
            int page = data.getInt("page");
            String data2 = data.getString("data");
            long end = data.getLong("end");
            return database.createSQLAttempt(studentid, quizid, page, data2, end);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/update/attempt")
    public String updateAttempt(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String data2;
            String id = data.getString("id");
            int page;

            try {
                data2 = data.getString("data");
                page = data.getInt("page");
            }
            catch (Exception e) {
                data2 = null;
                page = 0;
            }

            if(data2 == null) {
                return database.getSQLAttempt(id);
            }
            else {
                return database.updateSQLAttempt(id, data2, page);
            }
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/set/completed")
    public String setCompleted(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String id = data.getString("id");
            return database.setSQLCompleted(id);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/get/quizzes")
    public String getQuizzes(@RequestBody String raw) {
        System.out.println("REQUEST RECIEVED");
        try {
            JSONObject data = new JSONObject(raw);
            return database.getSQLQuizzes();
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/user/quizzes")
    public String getUserQuizzes(@RequestBody String raw) {
        System.out.println("REQUEST RECIEVED");
        try {
            JSONObject data = new JSONObject(raw);
            String id = data.getString("id");
            return database.getUserSQLQuizzes(id);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/delete/quiz")
    public String deleteQuiz(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String id = data.getString("id");
            return database.deleteSQLQuiz(id);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/get/user/attempt")
    public String getUserAttempt(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String studentid = data.getString("studentid");
            String quizid = data.getString("quizid");
            return database.getSQLAttempt(studentid, quizid);
        }
        catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    @PostMapping("/get/user/analytics")
    public String getUserAnalytics(@RequestBody String raw) {
        try {
            JSONObject data = new JSONObject(raw);
            String studentid = data.getString("studentid");
            return database.getSQLAnalytics(studentid);
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
