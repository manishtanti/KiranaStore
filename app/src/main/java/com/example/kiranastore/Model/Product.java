package com.example.kiranastore.Model;


import android.os.Parcelable;

import java.io.Serializable;

public class Product implements Serializable {
    private String name,imageUrl,id,description,category;
    private Integer quantity,price;

    public Product() {
    }

    public Product(String name, String imageUrl, String id, String description, String category, Integer quantity, Integer price) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.id = id;
        this.description = description;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
