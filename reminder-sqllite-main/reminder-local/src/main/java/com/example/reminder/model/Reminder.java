package com.example.reminder.model;


public class Reminder {
    private int id;
    private String task;
    private String time;
    private String days;
    private String type;

    public Reminder(int id, String task, String time, String days, String type) {
        this.id = id;
        this.task = task;
        this.time = time;
        this.days = days;
        this.type = type;
    }

    public Reminder(String task, String time, String days, String type) {
        this(0, task, time, days, type);
    }

    // Getter & Setter
    public int getId() { return id; }
    public String getTask() { return task; }
    public String getTime() { return time; }
    public String getDays() { return days; }
    public String getType() { return type; }

    public void setId(int id) { this.id = id; }
    public void setTask(String task) { this.task = task; }
    public void setTime(String time) { this.time = time; }
    public void setDays(String days) { this.days = days; }
    public void setType(String type) { this.type = type; }
}

