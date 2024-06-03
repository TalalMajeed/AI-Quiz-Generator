package com.talal.quizcore;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String name;
    private String password;
    private int review;

    private int duration;
    private String data;

    private String creator;

    public Quiz(String id, String name, String password, int review, String data, String creator, int duration) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.review = review;
        this.data = data;
        this.creator = creator;
        this.duration = duration;
    }

public Quiz() {}

    // Getters and setters

    public String getCreator() {
        return creator;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getReview() {
        return review;
    }

    public void setReview(int review) {
        this.review = review;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectNode quizjson = mapper.createObjectNode();
        quizjson.putPOJO("quiz", this);
        quizjson.put("status", 200);
        quizjson.put("message", "Quiz found");
        try {
            return mapper.writeValueAsString(quizjson);
        } catch (JsonProcessingException e) {
            return "{\"status\": 500, \"message\": \"JSON Serialization error: " + e.getMessage() + "\"}";
        }

    }


}
