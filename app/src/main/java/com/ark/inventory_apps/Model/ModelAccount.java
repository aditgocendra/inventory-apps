package com.ark.inventory_apps.Model;

public class ModelAccount {
    private String username;
    private String email;
    private boolean statusAccount;
    private String keyUser;

    public ModelAccount() {
    }

    public ModelAccount(String username, String email, boolean statusAccount) {
        this.username = username;
        this.email = email;
        this.statusAccount = statusAccount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isStatusAccount() {
        return statusAccount;
    }

    public void setStatusAccount(boolean statusAccount) {
        this.statusAccount = statusAccount;
    }

    public String getKeyUser() {
        return keyUser;
    }

    public void setKeyUser(String keyUser) {
        this.keyUser = keyUser;
    }
}
