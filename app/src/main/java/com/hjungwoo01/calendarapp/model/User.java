package com.hjungwoo01.calendarapp.model;

import java.util.Collections;
import java.util.List;

public class User {
    public static List<User> usersList = Collections.emptyList();
    private long id;
    private String name;
    private String face;

    public long getId() { return this.id; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public String getFace() { return this.face; }
    public void setFace(String face) { this.face = face; }

    public static void setUsersList(List<User> list) {
        usersList = list;
    }

    public static List<User> getUsersList() {
        return usersList;
    }

    public static String[] getUsersStringArray() {
        if(!usersList.isEmpty()) {
            String[] usersArray = new String[User.usersList.size()];
            for (int i = 0; i < usersArray.length; i++) {
                usersArray[i] = User.usersList.get(i).getName();
            }
            return usersArray;
        }
        return new String[] {};
    }
}
