package com.example.android.bakingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.bakingapp.RecipieObjects.Ingredient;
import com.example.android.bakingapp.RecipieObjects.Recipe;

import java.util.ArrayList;

/**
 * This class is used to get the data for the widget
 * works like an adapter
 * object RemoteView is used to inflate the layout
 */

public class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String INTENT_EXTRA_INGREDIENT_ITEM = "ingredient_item_position";
    private static final String INTENT_EXTRA_RECIPE_KEY = "recipe_object";
    private static final String BUNDLE_EXTRA = "bundle";
    private static final String INGREDIENTS_LIST_EXTRA = "ingredientsList";

    Context mContext;
    Recipe mRecipe;
    Intent mIntent;

    public ListRemoteViewsFactory(Context mContext, Intent mIntent) {
        this.mContext = mContext;
        this.mIntent = mIntent;
        Log.d("Widget update: ", "ListRemoteViewsFactory constructor");
        if (mIntent.hasExtra(BUNDLE_EXTRA)){
            Bundle bundle = mIntent.getBundleExtra(BUNDLE_EXTRA);
            if (bundle.containsKey(INGREDIENTS_LIST_EXTRA)){
                mRecipe = (Recipe)bundle.getSerializable(INGREDIENTS_LIST_EXTRA);
                Log.d("Widget update: ", "ListRemoteViewsFactory id:" + mRecipe.getId());
            }
        }
    }

    public void swapRecipe(Recipe recipe){
        mRecipe = recipe;
    }

    @Override
    public void onCreate() {
        Log.d("Widget update: ", "ListRemoteViewsFactory onCreate()");
    }

    //called on start and when notifyAppWidgetViewDataChanged is called
    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (mRecipe == null){
            return 0;
        }
        return mRecipe.getIngredients().size();
    }

    //Similar to onBindViewHolder in an Adapter
    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(),
                android.R.layout.simple_list_item_1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            remoteViews.setInt(android.R.id.text1, "setBackgroundColor",
                    mContext.getColor(R.color.colorPrimaryLight));
        } else {
            remoteViews.setInt(android.R.id.text1, "setBackgroundColor", ContextCompat.getColor(mContext,
                    R.color.colorPrimaryLight));
        }
        remoteViews.setTextViewText(android.R.id.text1, mRecipe.getIngredients().get(i).getName());

        //Fill in the Pending Intent onClick template, with the ingredient position in the list
        Bundle extras = new Bundle();
        extras.putSerializable(INTENT_EXTRA_RECIPE_KEY, mRecipe);
        extras.putInt(INTENT_EXTRA_INGREDIENT_ITEM, i);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        fillInIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        remoteViews.setOnClickFillInIntent(android.R.id.text1, fillInIntent);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    //Only one type of view
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
