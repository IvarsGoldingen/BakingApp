package com.example.android.bakingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViewsService;

import com.example.android.bakingapp.RecipieObjects.Recipe;

/**
 * Connects the remote adapter to be able to request remote views
 */

public class WidgetService extends RemoteViewsService {

    private static final String INTENT_EXTRA_INGREDIENT_ITEM = "ingredient_item_position";
    private static final String INTENT_EXTRA_RECIPE_KEY = "recipe_object";
    private static final String BUNDLE_EXTRA = "bundle";
    private static final String INGREDIENTS_LIST_EXTRA = "ingredientsList";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("Widget update: ", "WidgetService");
        if (intent.hasExtra(BUNDLE_EXTRA)){
            Bundle bundle = intent.getBundleExtra(BUNDLE_EXTRA);
            if (bundle.containsKey(INGREDIENTS_LIST_EXTRA)){
                Recipe mRecipe = (Recipe)bundle.getSerializable(INGREDIENTS_LIST_EXTRA);
                Log.d("Widget update: ", "WidgetService id:" + mRecipe.getId());
            }
        }

        return new ListRemoteViewsFactory(this, intent);
    }
}
