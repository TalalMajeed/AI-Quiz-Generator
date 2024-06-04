package com.talal.quizcore;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.SecureRandom;

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String name;
    private String email;
    private String password;
    private String education;
    private String community;

    private String image;

    private String description;

    public Student() {
    }

    public Student(String  id, String name, String email, String password, String education, String community) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.education = education;
        this.community = community;
    }

    // Getters and setters

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String toJson(JwtUtil jwtUtil) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectNode studentJson = mapper.createObjectNode();
        studentJson.putPOJO("student", this);
        studentJson.put("status", 200);
        studentJson.put("message", "User found");

        String token = jwtUtil.generateToken(this.getId());
        studentJson.put("token", token);

        try {
            return mapper.writeValueAsString(studentJson);
        } catch (JsonProcessingException e) {
            return "{\"status\": 500, \"message\": \"JSON Serialization error: " + e.getMessage() + "\"}";
        }
    }

    public static String generateID() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        StringBuilder id = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 2; i++) {
            id.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        for (int i = 0; i < 3; i++) {
            id.append(numbers.charAt(random.nextInt(numbers.length())));
        }
        return id.toString();
    }
}