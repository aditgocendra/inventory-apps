package com.ark.inventory_apps.Model;

public class ModelCategory {

    private String category;
    private String keyCategory;

    public ModelCategory() {
    }

    public ModelCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKeyCategory() {
        return keyCategory;
    }

    public void setKeyCategory(String keyCategory) {
        this.keyCategory = keyCategory;
    }
}
