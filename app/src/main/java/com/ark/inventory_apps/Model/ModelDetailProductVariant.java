package com.ark.inventory_apps.Model;

public class ModelDetailProductVariant {
    private String typeVariant;
    private String keyVariant;

    public ModelDetailProductVariant() {
    }

    public ModelDetailProductVariant(String typeVariant) {
        this.typeVariant = typeVariant;
    }

    public String getTypeVariant() {
        return typeVariant;
    }

    public void setTypeVariant(String typeVariant) {
        this.typeVariant = typeVariant;
    }

    public String getKeyVariant() {
        return keyVariant;
    }

    public void setKeyVariant(String keyVariant) {
        this.keyVariant = keyVariant;
    }
}
