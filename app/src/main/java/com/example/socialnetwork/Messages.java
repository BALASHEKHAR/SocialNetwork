package com.example.socialnetwork;

class Messages {
    String date,time,type,from,messageText;

    public Messages(String date, String time, String type, String from, String messageText) {
        this.date = date;
        this.time = time;
        this.type = type;
        this.from = from;
        this.messageText = messageText;
    }

    public String getDate() {
        return date;
    }

    public Messages() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
