package com.application.chattingapp;

public class ChatUser {

    private boolean seen;
    private long time;

    public ChatUser(){

    }

    public ChatUser(boolean seen, long time) {
        this.seen = seen;
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
