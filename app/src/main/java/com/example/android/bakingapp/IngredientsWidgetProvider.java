package com.example.android.bakingapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.android.bakingapp.RecipieObjects.Ingredient;
import com.example.android.bakingapp.RecipieObjects.Recipe;

import java.util.ArrayList;

/**
 * Implementation of App Widget functionality.
 * This is a broadcastReceiver
 */
public class IngredientsWidgetProvider extends AppWidgetProvider {

    private static final String BUNDLE_EXTRA = "bundle";
    private static final String INGREDIENTS_LIST_EXTRA = "ingredientsList";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Recipe receivedRecipe) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ingredients_widget);

        //open the app when the widget is clicked, this will work only if there is no data
        Intent intent = new Intent(context, MainActivity.class);
        //Use a pending intent so the app can be open from the widget
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_view, pendingIntent);

        //Add the list of ingredients to the intent
        Intent listIntent = new Intent(context, WidgetService.class);
        if (receivedRecipe != null){
            Log.d("Widget update: ", "updateAppWidget id:" + receivedRecipe.getId());

            //The list must be put into a bundle to be passed through multiple intents
            Bundle extrasBundle = new Bundle();
            extrasBundle.putSerializable(INGREDIENTS_LIST_EXTRA, receivedRecipe);
            listIntent.putExtra(BUNDLE_EXTRA, extrasBundle);
        }
        //Set the Widget service intent to act as the adapter for the listview
        views.setRemoteAdapter(R.id.widget_list_view, listIntent);

        //Set the detailActivity to be opened when an ingredient is clicked
        Intent appIntent = new Intent(context, RecipeDetailsActivity.class);
        PendingIntent detailPendingIntent = PendingIntent.getActivity(context, 0,
                appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //The template is used so a seperate pendingIntent does not need to be set on each view
        views.setPendingIntentTemplate(R.id.widget_list_view, detailPendingIntent);

        //Set the textview to show when there is no data
        views.setEmptyView(R.id.widget_list_view, R.id.empty_widget_text);
        //Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateRecipeWidgets(Context context, AppWidgetManager appWidgetManager,
                                           int[] widgetIds, Recipe selectedRecipe){
        for (int widgetId : widgetIds){
            updateAppWidget(context, appWidgetManager, widgetId, selectedRecipe);
        }
    }


    //CALLED WHEN NEW WIDGET IS CREATED OR WHEN THE UPDATE PERIOD EXPIRES
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("Widget update: ", "Provider onUpdate()");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null);
        }
    }

    //Called tge first tima an instance of the widget is added to the home screen
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    //called once the last instance of the widget is removed
    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

