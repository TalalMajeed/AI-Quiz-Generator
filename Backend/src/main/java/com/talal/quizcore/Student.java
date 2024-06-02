package com.talal.quizcore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.security.SecureRandom;

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    private String education;
    private String community;

    // Constructors, getters, and setters
    public Student() {
    }

    public Student(long id, String name, String email, String password, String education, String community) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.education = education;
        this.community = community;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getcommunity() {
        return community;
    }

    public void setcommunity() {
        this.community = community;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", education='" + education + '\'' +
                ", community='" + community + '\'';
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