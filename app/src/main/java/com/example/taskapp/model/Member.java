package com.example.taskapp.model;

public class Member {
    private String id;          // UUID
    private String name;
    private String knoxId;      // username
    private String password;
    private String email;       // email
    private String phoneNumber; // 10 number
    private boolean isFirstLogin;

    public Member() {
        this.isFirstLogin = true;
    }

    public Member(String id, String name, String knoxId, String password, String email, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.knoxId = knoxId;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.isFirstLogin = true;
    }

    // Getter and Setter...

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

    public String getKnoxId() {
        return knoxId;
    }

    public void setKnoxId(String knoxId) {
        this.knoxId = knoxId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        isFirstLogin = firstLogin;
    }
}
