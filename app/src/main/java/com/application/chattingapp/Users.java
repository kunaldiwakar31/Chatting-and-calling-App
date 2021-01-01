package com.application.chattingapp;

public class Users {
    public String name;
    public String image;
    public String status;
    public String thumb_image;
    public String search;
    public String phoneNumber;

    public Users(){

    }
    public Users(String name, String image, String status,String thumb_image,String online,String search,String phoneNumber) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image=thumb_image;
        this.search=search;
        this.phoneNumber=phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) { this.thumb_image = thumb_image; }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
