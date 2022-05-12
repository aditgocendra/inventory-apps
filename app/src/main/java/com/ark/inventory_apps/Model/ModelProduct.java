package com.ark.inventory_apps.Model;

public class ModelProduct {

    private String keyCategory;
    private String keySubCategory;
    private String nameProduct;
    private String priceDefault;
    private String unit;
    private int stock;
    private String dateInput;
    private String urlImage;
    private String keyProduct;

    public ModelProduct() {
    }

    public ModelProduct(String keyCategory, String keySubCategory, String nameProduct, String priceDefault, String unit, int stock, String dateInput, String urlImage) {
        this.keyCategory = keyCategory;
        this.keySubCategory = keySubCategory;
        this.nameProduct = nameProduct;
        this.priceDefault = priceDefault;
        this.unit = unit;
        this.stock = stock;
        this.dateInput = dateInput;
        this.urlImage = urlImage;
    }

    public String getKeyCategory() {
        return keyCategory;
    }

    public void setKeyCategory(String keyCategory) {
        this.keyCategory = keyCategory;
    }

    public String getKeySubCategory() {
        return keySubCategory;
    }

    public void setKeySubCategory(String keySubCategory) {
        this.keySubCategory = keySubCategory;
    }

    public String getNameProduct() {
        return nameProduct;
    }

    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
    }

    public String getPriceDefault() {
        return priceDefault;
    }

    public void setPriceDefault(String priceDefault) {
        this.priceDefault = priceDefault;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDateInput() {
        return dateInput;
    }

    public void setDateInput(String dateInput) {
        this.dateInput = dateInput;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getKeyProduct() {
        return keyProduct;
    }

    public void setKeyProduct(String keyProduct) {
        this.keyProduct = keyProduct;
    }
}
