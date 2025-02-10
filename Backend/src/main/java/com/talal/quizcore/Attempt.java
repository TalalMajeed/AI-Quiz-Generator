package com.talal.quizcore;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Attempt {
    private String id;
    private String quizId;
    private String studentId;
    private String data;
    private int page;
    private String quizName;

    private long end;

    private int duration;

    boolean completed;

    public Attempt(String id, String quizId, String studentId, String data, String quizName, int end) {
        this.id = id;
        this.quizId = quizId;
        this.studentId = studentId;
        this.data = data;
        this.page = page;
        this.quizName = quizName;
        this.end = end;
    }

    public Attempt() {
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getEnd() {
        return end;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getQuizName() {
        return quizName;
    }

    public void setQuizName(String quizName) {
        this.quizName = quizName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectNode attemptJson = mapper.createObjectNode();
        attemptJson.putPOJO("attempt", this);
        attemptJson.put("status", 200);
        attemptJson.put("message", "Attempt found");
        try {
            return mapper.writeValueAsString(attemptJson);
        } catch (JsonProcessingException e) {
            return "{\"status\": 500, \"message\": \"JSON Serialization error: " + e.getMessage() + "\"}";
        }
    }

}
