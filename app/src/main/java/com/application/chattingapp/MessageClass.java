package com.application.chattingapp;

public class MessageClass {

    private String message;
    private String type;
    private String from;
    private String to;
    private boolean seen;
    private long time;

    public MessageClass(){

    }
    public MessageClass(String message, long time, String type, boolean seen,String from,String to) {
        this.message = message;
        this.time = time;
        this.type = type;
        this.seen = seen;
        this.from = from;
        this.to=to;
    }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public long getTime() { return time; }

    public void setTime(long time) { this.time = time; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getFrom() { return from; }

    public void setFrom(String from) { this.from = from; }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }


}
