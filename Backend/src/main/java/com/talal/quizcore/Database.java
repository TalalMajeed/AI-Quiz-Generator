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
                    student.setDescription(rs.getString("description"));

                    try {
                        Blob blob = rs.getBlob("photo");
                        if (blob != null) {
                            byte[] blobBytes = blob.getBytes(1, (int) blob.length());
                            String blobText = new String(blobBytes);
                            student.setImage(blobText);
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Error: " + e);
                    }


                    return student;
                }
            });

            if (!students.isEmpty()) {
                return students.get(0).toJson(jwtUtil);
            } else {
                return "{\"status\": 400, \"message\": \"Student not found\"}";
            }
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
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
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
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
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String createSQLStudent(String name, String email, String password, String education, String community) {
        // If the email already exists, return an error
        String SQLTest = "SELECT * FROM student";
        String id = Student.generateID();
        try {
            List<Student> students = db.query(SQLTest, new Object[]{}, new RowMapper<>() {
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

            for(Student student : students) {
                if (student.getEmail().equals(email)) {
                    return "{\"status\": 400, \"message\": \"Email already exists\"}";
                }
            }
            for(Student student : students) {
                if (student.getId().equals(id)) {
                    id = Student.generateID();
                }
            }
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }

        // If the email does not exist, create the student
        String SQLInsert = "INSERT INTO student (id, name, email, password, education, community) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            db.update(SQLInsert, id, name, email, password, education, community);
            return getSQLStudent(email, password);
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String updateSQLStudent(String id, String name, String education, String community, String image, String description) {
        String SQLUpdate = "UPDATE student SET name = ?, education = ?, community = ?, photo = ?, description = ? WHERE id = ?";
        try {
            //Convert the image from String to Blob
            Blob blob = null;
            if (image != null) {
                byte[] imageBytes = image.getBytes();
                blob = new javax.sql.rowset.serial.SerialBlob(imageBytes);
            }
            db.update(SQLUpdate, name, education, community, blob, description, id);
            return "{\"status\": 200, \"message\": \"Student updated\"}";
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String deleteSQLStudent(String id) {

        //First Delete From Quiz and Attempt due to Foreign Key Constraint
        String SQLDeleteQuiz = "DELETE FROM quiz WHERE creator = ?";
        String SQLDeleteAttempt = "DELETE FROM attempt WHERE studentid = ?";
        try {
            db.update(SQLDeleteQuiz, id);
            db.update(SQLDeleteAttempt, id);
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }

        String SQLDelete = "DELETE FROM student WHERE id = ?";
        try {
            db.update(SQLDelete, id);
            return "{\"status\": 200, \"message\": \"Student deleted\"}";
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String createSQLQuiz(String name, String password, String creator, Boolean review, String data, int duration) {
        String id = Student.generateID();

        //Keep generating the id until it is unique
        String SQLTest = "SELECT * FROM quiz";
        try {
            List<Quiz> quizzes = db.query(SQLTest, new Object[]{}, new RowMapper<>() {
                @Override
                public Quiz mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Quiz quiz = new Quiz();
                    quiz.setId(rs.getString("id"));
                    quiz.setName(rs.getString("name"));
                    quiz.setPassword(rs.getString("password"));
                    quiz.setReview(rs.getInt("review"));
                    quiz.setCreator(rs.getString("creator"));
                    quiz.setDuration(rs.getInt("duration"));
                    return quiz;
                }
            });

            for(Quiz quiz : quizzes) {
                if (quiz.getId().equals(id)) {
                    id = Student.generateID();
                }
            }
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }

        //Convert data from String to Blob
        Blob blob = null;
        try {
            byte[] dataBytes = data.getBytes();
            blob = new javax.sql.rowset.serial.SerialBlob(dataBytes);
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }

        String SQLInsert = "INSERT INTO quiz (id, name, password, creator, data, duration, review) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            db.update(SQLInsert, id, name, password, creator, blob, duration, review ? 1 : 0);
            return "{\"status\": 200, \"message\": \"Quiz created\"}";
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }
}
