package com.example.kiranastore.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Order {
    String customerId,orderId;
    Integer itemCount,price;
    boolean delivered;

    public Order() {
    }

    public Order(String customerId, String orderId, Integer itemCount, Integer price, boolean delivered) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.itemCount = itemCount;
        this.price = price;
        this.delivered = delivered;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }


}
