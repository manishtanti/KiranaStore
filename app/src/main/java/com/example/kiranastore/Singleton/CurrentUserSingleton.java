package com.example.kiranastore.Singleton;

import com.example.kiranastore.Model.User;

public class CurrentUserSingleton {
    private static CurrentUserSingleton instance = null;
    private User user;

    private CurrentUserSingleton() {
        this.user = new User();
    }

    public static CurrentUserSingleton getInstance(){
        if(instance==null){
            instance = new CurrentUserSingleton();
        }
        return instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
