package com.example.android.bakingapp.RecipieObjects;

import java.io.Serializable;

/**
 * Ingredient object used for recipies
 */

public class Ingredient implements Serializable {
    private double mQuantity;
    private String mMeasure;
    private String mName;

    public Ingredient(double quantity, String measure, String name) {
        this.mQuantity = quantity;
        this.mMeasure = measure;
        this.mName = name;
    }

    public double getQuantity() {
        return mQuantity;
    }

    public String getMeasure() {
        return mMeasure;
    }

    public String getName() {
        return mName;
    }
}
