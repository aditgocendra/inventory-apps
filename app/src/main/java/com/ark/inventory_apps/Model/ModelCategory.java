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

    @Override
    public String toString() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelCategory that = (ModelCategory) o;
        return keyCategory.equals(that.keyCategory);
    }
}
