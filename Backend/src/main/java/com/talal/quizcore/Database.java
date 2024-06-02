package com.talal.quizcore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    }

    public String getSQLUser(String email, String password) {
        String SQLTest = "SELECT * FROM student WHERE email = ? AND password = ?";
        try {
            List<Map<String, Object>> results = db.query(SQLTest, new Object[]{email, password}, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", rs.getString("id"));
                    result.put("name", rs.getString("name"));
                    result.put("email", rs.getString("email"));
                    result.put("education", rs.getString("education"));
                    result.put("community", rs.getString("community"));
                    return result;
                }
            });

            if (!results.isEmpty()) {
                Map<String, Object> result = results.get(0);
                return "{\"status\": 200, \"message\": \"User Found\", \"id\": \"" + result.get("id") +  "\", \"username\": \"" + result.get("name") + "\", \"email\": \"" + result.get("email") + "\", \"education\": \"" + result.get("education") + "\", \"community\": \"" + result.get("community") + "\"}";
            } else {
                return "{\"status\": 400, \"message\": \"User not found\"}";
            }
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage() + "\"}";
        }
    }
}
