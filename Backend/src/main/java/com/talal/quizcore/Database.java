package com.talal.quizcore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Database {

    private JdbcTemplate db;

    private JwtUtil jwtUtil;

    @Autowired
    public Database(JdbcTemplate db) {
        jwtUtil = new JwtUtil();
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

    public String getSQLStudent(String email, String password) {
        String SQLTest = "SELECT * FROM student WHERE email = ? AND password = ?";
        try {
            List<Student> students = db.query(SQLTest, new Object[]{email, password}, new RowMapper<>() {
                @Override
                public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Student student = new Student();
                    student.setId(rs.getString("id"));
                    student.setName(rs.getString("name"));
                    student.setEmail(rs.getString("email"));
                    student.setEducation(rs.getString("education"));
                    student.setCommunity(rs.getString("community"));
                    return student;
                }
            });

            if (!students.isEmpty()) {
                return students.get(0).toJson(jwtUtil);
            } else {
                return "{\"status\": 400, \"message\": \"Student not found\"}";
            }
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage() + "\"}";
        }
    }

    public String checkSQLStudent(String email, String id) {
        String SQLTest = "SELECT * FROM student WHERE email = ? AND id = ?";
        try {
            List<Student> students = db.query(SQLTest, new Object[]{email, id}, new RowMapper<>() {
                @Override
                public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Student student = new Student();
                    student.setId(rs.getString("id"));
                    student.setName(rs.getString("name"));
                    student.setEmail(rs.getString("email"));
                    student.setEducation(rs.getString("education"));
                    student.setCommunity(rs.getString("community"));
                    return student;
                }
            });

            if (!students.isEmpty()) {
                String token = jwtUtil.generateToken(id);
                return "{\"status\": 200, \"message\": \"Student found\", \"token\": \"" + token + "\"}";
            } else {
                return "{\"status\": 400, \"message\": \"Student not found\"}";
            }
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage() + "\"}";
        }
    }

    public String getSQLQuiz(String id) {
        String SQLTest = "SELECT q.id, q.name, q.password, q.review, q.data, s.name, q.duration FROM quiz q JOIN student s ON q.creator = s.id WHERE q.id = ?";
        try {
            List<Quiz> quizzes = db.query(SQLTest, new Object[]{id}, new RowMapper<>() {
                @Override
                public Quiz mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Quiz quiz = new Quiz();
                    quiz.setId(rs.getString("q.id"));
                    quiz.setName(rs.getString("q.name"));
                    quiz.setPassword(rs.getString("q.password"));
                    quiz.setReview(rs.getInt("q.review"));
                    quiz.setCreator(rs.getString("s.name"));
                    quiz.setDuration(rs.getInt("q.duration"));

                    Blob blob = rs.getBlob("q.data");
                    if (blob != null) {
                        byte[] blobBytes = blob.getBytes(1, (int) blob.length());
                        String blobText = new String(blobBytes);
                        quiz.setData(blobText);
                    }
                    return quiz;
                }
            });

            if (!quizzes.isEmpty()) {
                return quizzes.get(0).toJson();
            } else {
                return "{\"status\": 400, \"message\": \"Quiz not found\"}";
            }
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage() + "\"}";
        }
    }
}
