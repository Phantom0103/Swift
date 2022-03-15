package com.wenxia.facade.model;

/**
 * @author zhouw
 * @date 2022-03-15
 */
public class User {

    private String userId;
    private String username;
    private int type;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
