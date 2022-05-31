package com.nickstudio.notes;

public class Note {
    String date;
    String time;
    String text;
    String filePath;

    public Note(String date, String time, String text, String filePath) {
        this.date = date;
        this.time = time;
        this.text = text;
        this.filePath = filePath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
