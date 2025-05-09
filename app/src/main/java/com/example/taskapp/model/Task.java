package com.example.taskapp.model;

import com.example.taskapp.enums.TaskStatus;

public class Task {
    private String id;                 // UUID
    private String name;
    private String requirement;
    private String categoryId;         // ID category

    private String member1Id;          // ID member
    private String member2Id;          //
    private String managerId;          // ID manager

    private String startTime;          // HH:mm dd/MM/yyyy
    private String endTime;            // HH:mm dd/MM/yyyy
    private String doneTime;           // HH:mm dd/MM/yyyy
    private String createTime;         // HH:mm dd/MM/yyyy
    private String reminderTime;       // HH:mm dd/MM/yyyy

    private String extendReason;       // Lý do gia hạn
    private TaskStatus status;         // Enum
    private int numOfReminder;

    public Task() {
    }

    public Task(String id, String name, String requirement, String categoryId,
                String member1Id, String member2Id, String managerId,
                String startTime, String endTime, String doneTime, String createTime,
                String reminderTime, String extendReason, TaskStatus status, int numOfReminder) {
        this.id = id;
        this.name = name;
        this.requirement = requirement;
        this.categoryId = categoryId;
        this.member1Id = member1Id;
        this.member2Id = member2Id;
        this.managerId = managerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.doneTime = doneTime;
        this.createTime = createTime;
        this.reminderTime = reminderTime;
        this.extendReason = extendReason;
        this.status = status;
        this.numOfReminder = numOfReminder;
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

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getMember1Id() {
        return member1Id;
    }

    public void setMember1Id(String member1Id) {
        this.member1Id = member1Id;
    }

    public String getMember2Id() {
        return member2Id;
    }

    public void setMember2Id(String member2Id) {
        this.member2Id = member2Id;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDoneTime() {
        return doneTime;
    }

    public void setDoneTime(String doneTime) {
        this.doneTime = doneTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getExtendReason() {
        return extendReason;
    }

    public void setExtendReason(String extendReason) {
        this.extendReason = extendReason;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getNumOfReminder() {
        return numOfReminder;
    }

    public void setNumOfReminder(int numOfReminder) {
        this.numOfReminder = numOfReminder;
    }
}
