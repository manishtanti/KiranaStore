package com.example.kiranastore.Model;

public class User {
    private boolean shopkeeper;
    private String email,id,imageUrl,fullName,phoneNo;

    public User() {
    }

    public User(boolean shopkeeper, String email, String id, String imageUrl, String fullName, String phoneNo) {
        this.shopkeeper = shopkeeper;
        this.email = email;
        this.id = id;
        this.imageUrl = imageUrl;
        this.fullName = fullName;
        this.phoneNo = phoneNo;
    }

    public boolean isShopkeeper() {
        return shopkeeper;
    }

    public void setShopkeeper(boolean shopkeeper) {
        this.shopkeeper = shopkeeper;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}
