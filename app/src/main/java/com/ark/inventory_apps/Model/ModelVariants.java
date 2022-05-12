package com.ark.inventory_apps.Model;

public class ModelVariants {

    private String nameVariant;
    private String typeVariant;
    private String keyVariant;

    public ModelVariants() {
    }

    public ModelVariants(String nameVariant) {
        this.nameVariant = nameVariant;
    }

    public String getNameVariant() {
        return nameVariant;
    }

    public void setNameVariant(String nameVariant) {
        this.nameVariant = nameVariant;
    }

    public String getKeyVariant() {
        return keyVariant;
    }

    public void setKeyVariant(String keyVariant) {
        this.keyVariant = keyVariant;
    }
}
