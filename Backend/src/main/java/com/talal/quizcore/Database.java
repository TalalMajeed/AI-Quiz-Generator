package com.talal.quizcore;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

            //Check for any Attempts or Quizzes

            if (!students.isEmpty()) {
                String token = jwtUtil.generateToken(id);
                //Get all the users attempts
                String SQLTestAttempt = "SELECT a.*, s.duration FROM attempt a JOIN quiz s ON a.quizid = s.id WHERE a.studentid = ?";
                List<Attempt> attempts = db.query(SQLTestAttempt, new Object[]{id}, new RowMapper<>() {
                    @Override
                    public Attempt mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Attempt attempt = new Attempt();
                        attempt.setDuration(rs.getInt("s.duration"));
                        attempt.setStudentId(rs.getString("a.studentid"));
                        attempt.setQuizId(rs.getString("a.quizid"));
                        attempt.setPage(rs.getInt("a.page"));
                        Timestamp t = rs.getTimestamp("a.end");
                        attempt.setEnd((long) t.getTime());
                        attempt.setId(rs.getString("a.id"));
                        attempt.setCompleted(rs.getInt("completed") == 1 ? true : false);

                        Blob blob = rs.getBlob("a.data");
                        if (blob != null) {
                            byte[] blobBytes = blob.getBytes(1, (int) blob.length());
                            String blobText = new String(blobBytes);
                            attempt.setData(blobText);
                        }
                        return attempt;
                    }
                });

                List<Attempt> validAttempts = new ArrayList<>();
                for (Attempt attempt : attempts) {
                    if (attempt.getEnd() + 60 * attempt.getDuration() * 1000 > System.currentTimeMillis()) {
                        if(!attempt.isCompleted()) {
                            validAttempts.add(attempt);
                        }
                    }
                }

                Attempt attempt = validAttempts.isEmpty() ? null : validAttempts.get(0);

                JSONObject validation = new JSONObject();
                validation.put("status", 200);
                validation.put("message", "Student found");
                validation.put("token", token);

                if(attempt != null) {
                    validation.put("attempt", attempt.toJson());
                }
                else {
                    validation.put("attempt", new JSONObject());
                }


                return validation.toString();
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
        String SQLDeleteAttempt = "DELETE FROM attempt WHERE studentid = ?";
        String SQLDeleteQuiz = "DELETE FROM quiz WHERE creator = ?";

        try {
            db.update(SQLDeleteAttempt, id);
            db.update(SQLDeleteQuiz, id);
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

    public String createSQLQuiz(String name, String password, String creator, Boolean review, String data, int duration, String eid) {

        //IF eid is not null then update the quiz
        if(eid != null) {
            String SQLUpdate = "UPDATE quiz SET name = ?, password = ?, creator = ?, data = ?, duration = ?, review = ? WHERE id = ?";
            try {
                db.update(SQLUpdate, name, password, creator, data, duration, review ? 1 : 0, eid);
                return "{\"status\": 200, \"message\": \"Quiz updated\"}";
            } catch (Exception e) {
                return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
            }
        }
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

    public String createSQLAttempt(String studentid, String quizid, int page, String data, long end) {
        String id = Student.generateID();

        //Keep generating the id until it is unique
        String SQLTest = "SELECT * FROM attempt";
        try {
            List<Attempt> attempts = db.query(SQLTest, new Object[]{}, new RowMapper<>() {
                @Override
                public Attempt mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Attempt attempt = new Attempt();
                    attempt.setStudentId(rs.getString("studentid"));
                    attempt.setQuizId(rs.getString("quizid"));
                    attempt.setPage(rs.getInt("page"));
                    Timestamp t = rs.getTimestamp("end");
                    attempt.setEnd((long) t.getTime());
                    attempt.setId(rs.getString("id"));
                    attempt.setCompleted(rs.getInt("completed") == 1 ? true : false);
                    return attempt;
                }
            });

            for(Attempt attempt : attempts) {
                if (attempt.getId().equals(id)) {
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

        //Convert end from int to DateTime
        String SQLInsert = "INSERT INTO attempt (id, studentid, quizid, page, data, end, completed) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Timestamp timestamp = new Timestamp(end);
        System.out.println("Timestamp: " + timestamp);
        System.out.println(end);
        try {
            db.update(SQLInsert, id, studentid, quizid, page, blob, timestamp, 0);
            return getSQLAttempt(id);
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String getSQLAttempt(String id) {
        String SQLTest = "SELECT * FROM attempt WHERE id = ?";
        try {
            List<Attempt> attempts = db.query(SQLTest, new Object[]{id}, new RowMapper<>() {
                @Override
                public Attempt mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Attempt attempt = new Attempt();
                    attempt.setStudentId(rs.getString("studentid"));
                    attempt.setQuizId(rs.getString("quizid"));
                    attempt.setPage(rs.getInt("page"));
                    Timestamp t = rs.getTimestamp("end");
                    attempt.setEnd((long) t.getTime());
                    attempt.setId(rs.getString("id"));
                    attempt.setCompleted(rs.getInt("completed") == 1 ? true : false);

                    Blob blob = rs.getBlob("data");
                    if (blob != null) {
                        byte[] blobBytes = blob.getBytes(1, (int) blob.length());
                        String blobText = new String(blobBytes);
                        attempt.setData(blobText);
                    }
                    return attempt;
                }
            });

            if (!attempts.isEmpty()) {
                return attempts.get(0).toJson();
            } else {
                return "{\"status\": 400, \"message\": \"Attempt not found\"}";
            }
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String updateSQLAttempt(String id, String data, int page) {
        String SQLUpdate = "UPDATE attempt SET data = ?, page = ? WHERE id = ?";
        try {
            Blob blob = null;
            if (data != null) {
                byte[] dataBytes = data.getBytes();
                blob = new javax.sql.rowset.serial.SerialBlob(dataBytes);
            }
            db.update(SQLUpdate, blob, page, id);
            return getSQLAttempt(id);
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String setSQLCompleted(String id) {
        String SQLUpdate = "UPDATE attempt SET completed = ? WHERE id = ?";
        try {
            db.update(SQLUpdate, 1, id);
            return "{\"status\": 200, \"message\": \"Attempt completed\"}";
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String getSQLQuizzes() {
        String SQLTest = "SELECT q.id, q.name, q.password, q.review, q.data, s.name, q.duration FROM quiz q JOIN student s ON q.creator = s.id";
        try {
            List<Quiz> quizzes = db.query(SQLTest, new Object[]{}, new RowMapper<>() {
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
                JSONObject quizzesJson = new JSONObject();
                quizzesJson.put("status", 200);
                quizzesJson.put("message", "Quizzes found");
                List<JSONObject> quizList = new ArrayList<>();
                for (Quiz quiz : quizzes) {
                    quizList.add(new JSONObject(quiz.toJson()));
                }
                quizzesJson.put("quizzes", quizList);
                return quizzesJson.toString();
            } else {
                return "{\"status\": 400, \"message\": \"Quizzes not found\"}";
            }
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String getUserSQLQuizzes(String id) {
        String SQLTest = "SELECT q.id, q.name, q.password, q.review, q.data, s.name, q.duration FROM quiz q JOIN student s ON q.creator = s.id WHERE q.creator = ?";
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
                JSONObject quizzesJson = new JSONObject();
                quizzesJson.put("status", 200);
                quizzesJson.put("message", "Quizzes found");
                List<JSONObject> quizList = new ArrayList<>();
                for (Quiz quiz : quizzes) {
                    quizList.add(new JSONObject(quiz.toJson()));
                }
                quizzesJson.put("quizzes", quizList);
                return quizzesJson.toString();
            } else {
                return "{\"status\": 400, \"message\": \"Quizzes not found\"}";
            }
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String deleteSQLQuiz(String id) {
        String SQLDeleteAttempt = "DELETE FROM attempt WHERE quizid = ?";
        String SQLDelete = "DELETE FROM quiz WHERE id = ?";
        try {
            db.update(SQLDeleteAttempt, id);
            db.update(SQLDelete, id);
            return "{\"status\": 200, \"message\": \"Quiz deleted\"}";
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String getSQLAttempt(String studentid, String quizid) {
        String SQLTest = "SELECT * FROM attempt WHERE studentid = ? AND quizid = ?";
        try {
            //Return all of the attempts
            List<Attempt> attempts = db.query(SQLTest, new Object[]{studentid, quizid}, new RowMapper<>() {
                @Override
                public Attempt mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Attempt attempt = new Attempt();
                    attempt.setStudentId(rs.getString("studentid"));
                    attempt.setQuizId(rs.getString("quizid"));
                    attempt.setPage(rs.getInt("page"));
                    Timestamp t = rs.getTimestamp("end");
                    attempt.setEnd((long) t.getTime());
                    attempt.setId(rs.getString("id"));
                    attempt.setCompleted(rs.getInt("completed") == 1 ? true : false);

                    Blob blob = rs.getBlob("data");
                    if (blob != null) {
                        byte[] blobBytes = blob.getBytes(1, (int) blob.length());
                        String blobText = new String(blobBytes);
                        attempt.setData(blobText);
                    }
                    return attempt;
                }
            });

            if (!attempts.isEmpty()) {
                JSONObject attemptsJson = new JSONObject();
                attemptsJson.put("status", 200);
                attemptsJson.put("message", "Attempts found");
                List<JSONObject> attemptList = new ArrayList<>();
                for (Attempt attempt : attempts) {
                    attemptList.add(new JSONObject(attempt.toJson()));
                }
                attemptsJson.put("attempts", attemptList);
                return attemptsJson.toString();
            } else {
                return "{\"status\": 400, \"message\": \"Attempts not found\"}";
            }
        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

    public String getSQLAnalytics(String userid) {
        String SQLTest = "SELECT * FROM attempt WHERE quizid = ?";
        try {
            //Get the total quizzez where userid = creator
            //Get the total attempts where userid = studentid
            //Get the COUNT() of Total Quizes in Database

            String SQLTotalQuizzes = "SELECT COUNT(*) FROM quiz WHERE creator = ?";
            int totalQuizzes = db.queryForObject(SQLTotalQuizzes, new Object[]{userid}, Integer.class);

            String SQLTotalAttempts = "SELECT COUNT(*) FROM attempt WHERE studentid = ?";
            int totalAttempts = db.queryForObject(SQLTotalAttempts, new Object[]{userid}, Integer.class);

            String SQLTotalQuizzesAll = "SELECT COUNT(*) FROM quiz";
            int totalQuizzesAll = db.queryForObject(SQLTotalQuizzesAll, Integer.class);

            //Retun the total quizzes, total attempts and total quizzes in the database
            JSONObject analytics = new JSONObject();
            analytics.put("status", 200);
            analytics.put("message", "Analytics found");
            analytics.put("totalQuizzes", totalQuizzes);
            analytics.put("totalAttempts", totalAttempts);
            analytics.put("totalQuizzesAll", totalQuizzesAll);
            return analytics.toString();

        } catch (Exception e) {
            return "{\"status\": 500, \"message\": \"Error: " + e.getMessage().replaceAll("\"", "\\\\\"") + "\"}";
        }
    }

}
