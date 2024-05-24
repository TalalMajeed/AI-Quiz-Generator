package com.talal.quizcore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class Database {

    private JdbcTemplate db;

    @Autowired
    public Database(JdbcTemplate db) {
        try {
            this.db = db;
            System.out.println("Database Connected successfully!");
        }
        catch (Exception e) {
            System.out.println("Error: " + e);
        }

    }

    public void initializeDatabase() {
        //jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255))");
        //jdbcTemplate.update("INSERT INTO users (name) VALUES (?)", "John Doe");
    }
}
