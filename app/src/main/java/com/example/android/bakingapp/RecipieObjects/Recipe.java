package com.example.android.bakingapp.RecipieObjects;

import java.io.Serializable;
import java.util.List;

/**
 * Recipe object that includes the steps to make it and ingredients
 */

//implements serializable so it can be passed between activities
// a more efficient way would be to use parcable, but is more complicated
public class Recipe implements Serializable{
    private int mId;
    private String mName;
    private List<Ingredient> mIngredients;
    private List<Step> mSteps;

    public Recipe(int mId, String mName, List<Ingredient> ingredients, List<Step> steps) {
        this.mId = mId;
        this.mName = mName;
        this.mIngredients = ingredients;
        this.mSteps = steps;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public List<Ingredient> getIngredients() {
        return mIngredients;
    }

    public List<Step> getSteps() {
        return mSteps;
    }
}
