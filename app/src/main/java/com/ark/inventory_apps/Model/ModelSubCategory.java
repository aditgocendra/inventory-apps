package com.ark.inventory_apps.Model;

public class ModelSubCategory {
    private String subCategory;
    private String keySubCategory;

    public ModelSubCategory() {
    }

    public ModelSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getKeySubCategory() {
        return keySubCategory;
    }

    public void setKeySubCategory(String keySubCategory) {
        this.keySubCategory = keySubCategory;
    }

    @Override
    public String toString() {
        return subCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelSubCategory that = (ModelSubCategory) o;
        return keySubCategory.equals(that.keySubCategory);
    }
}
