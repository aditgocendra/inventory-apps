package com.ark.inventory_apps.Model;

import java.util.List;

public class ModelProductVariant {

    private String priceVariant;
    private int stockVariant;
    private String urlImage;
    private List<ModelDetailProductVariant> listDetailProductVariant;
    private String keyProductVariant;

    public ModelProductVariant() {
    }

    public ModelProductVariant(String priceVariant, int stockVariant, String urlImage, List<ModelDetailProductVariant> listDetailProductVariant) {
        this.priceVariant = priceVariant;
        this.stockVariant = stockVariant;
        this.urlImage = urlImage;
        this.listDetailProductVariant = listDetailProductVariant;
    }

    public String getPriceVariant() {
        return priceVariant;
    }

    public void setPriceVariant(String priceVariant) {
        this.priceVariant = priceVariant;
    }

    public int getStockVariant() {
        return stockVariant;
    }

    public void setStockVariant(int stockVariant) {
        this.stockVariant = stockVariant;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public List<ModelDetailProductVariant> getListDetailProductVariant() {
        return listDetailProductVariant;
    }

    public void setListDetailProductVariant(List<ModelDetailProductVariant> listDetailProductVariant) {
        this.listDetailProductVariant = listDetailProductVariant;
    }

    public String getKeyProductVariant() {
        return keyProductVariant;
    }

    public void setKeyProductVariant(String keyProductVariant) {
        this.keyProductVariant = keyProductVariant;
    }
}
