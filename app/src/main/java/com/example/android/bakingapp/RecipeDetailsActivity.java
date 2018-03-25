package com.example.android.bakingapp;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.bakingapp.RecipieObjects.Ingredient;
import com.example.android.bakingapp.RecipieObjects.Recipe;
import com.example.android.bakingapp.RecipieObjects.Step;

import java.util.ArrayList;

/**
 * Details activity displays details of one recipe
 */

public class RecipeDetailsActivity extends AppCompatActivity
implements RecipeDetailsFragment.OnItemClickListener{

    private static final int NO_INGREDIENT_POSITION_SPECIFIED = -1;
    private static final String INTENT_EXTRA_INGREDIENT_ITEM = "ingredient_item_position";
    private static final String INTENT_EXTRA_RECIPE_KEY = "recipe_object";
    private static final String INTENT_EXTRA_STEP_LIST_KEY = "step_list";
    private static final String INTENT_EXTRA_INGREDIENT_LIST_KEY = "ingredient_list";
    private static final String INTENT_EXTRA_STEPS_KEY = "steps";
    private static final String INTENT_EXTRA_STEP_SELECTED_KEY = "step_clicked";
    //save instance keys
    private static final String SAVE_INSTANCE_RECEIVED_RECIPE = "received_recipe";

    private Recipe receivedRecipe;
    private Toast mToast;

    //This gets called when the widget is clicked while the activity is  at the top of the activity stack
    //in that case onCreate is not called, so we need to set the UI to the correct Recipe
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        receivedRecipe = (Recipe)intent.getSerializableExtra(INTENT_EXTRA_RECIPE_KEY);
        setTitle(receivedRecipe.getName());
        //When comming from the widget, the back button should not show previous fragments, so
        //remove them
        removeFragmentsFromBackstack();
        if (intent.hasExtra(INTENT_EXTRA_INGREDIENT_ITEM)){
            startIngredientsFragment(intent.getIntExtra(INTENT_EXTRA_INGREDIENT_ITEM,
                    NO_INGREDIENT_POSITION_SPECIFIED));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail_view);
        //Create new fragments if there is no previously saved state
        if(savedInstanceState == null){
            Intent starterIntent = getIntent();
            receivedRecipe = (Recipe)starterIntent.getSerializableExtra(INTENT_EXTRA_RECIPE_KEY);
            setTitle(receivedRecipe.getName());

            if (starterIntent.hasExtra(INTENT_EXTRA_INGREDIENT_ITEM)){
                startIngredientsFragment(starterIntent.getIntExtra(INTENT_EXTRA_INGREDIENT_ITEM,
                        NO_INGREDIENT_POSITION_SPECIFIED));
            } else {
                startDetailsFragment();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recipe_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_set_on_widget:
                setCurrentRecipeOnWidget();
                showToast(receivedRecipe.getName() + " ingredients successfully set in widget");
                break;
            default:
                showToast(getString(R.string.widget_add_error));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setCurrentRecipeOnWidget(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();
        preferenceEditor.putInt(getString(R.string.preference_widget_recipe_id), receivedRecipe.getId());
        preferenceEditor.apply();

        Log.d("Widget update: ", "setCurrentRecipeOnWidget id:" + receivedRecipe.getId());

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int [] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, IngredientsWidgetProvider.class));
        IngredientsWidgetProvider.updateRecipeWidgets(this, appWidgetManager,
                appWidgetIds,receivedRecipe);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_INSTANCE_RECEIVED_RECIPE, receivedRecipe);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        receivedRecipe = (Recipe)savedInstanceState.getSerializable(SAVE_INSTANCE_RECEIVED_RECIPE);
    }

    @Override
    public void onBackPressed() {
        //If there is a fragment in the back stack then return to that, if not then return to prev
        //activity.
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0){
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /*
    * Called when the ingredient item is clicked in the recipe details fragment
    * */
    @Override
    public void onIngreedientsClicked() {
        startIngredientsFragment(NO_INGREDIENT_POSITION_SPECIFIED);
    }

    private void startIngredientsFragment(int recipeItemPosition){
        Bundle args = new Bundle();
        ArrayList<Ingredient> ingredients = (ArrayList)receivedRecipe.getIngredients();
        args.putSerializable(INTENT_EXTRA_INGREDIENT_LIST_KEY, ingredients);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (recipeItemPosition != -1){
            args.putInt(INTENT_EXTRA_INGREDIENT_ITEM, recipeItemPosition);
            //getSupportFragmentManager().popBackStack();
        } else {
            //Allow returning back to previous fragment only if we came from detailsFragment
            fragmentTransaction.addToBackStack(null);
        }
        IngredientsFragment ingredientsFragment = new IngredientsFragment();
        ingredientsFragment.setArguments(args);

        fragmentTransaction.replace(R.id.recipe_detail_container, ingredientsFragment).commit();
    }

    private void startDetailsFragment(){
        ArrayList<Step> steps = (ArrayList)receivedRecipe.getSteps();
        RecipeDetailsFragment detailsFragment = new RecipeDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(INTENT_EXTRA_STEP_LIST_KEY, steps);
        detailsFragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.recipe_detail_container, detailsFragment)
                .commit();
    }

    /*
    * Called when a step item is clicked in the recipe details fragment
    * */
    @Override
    public void onStepClicked(int stepNumber) {
        Bundle args = new Bundle();
        ArrayList<Step> steps = (ArrayList)receivedRecipe.getSteps();
        args.putSerializable(INTENT_EXTRA_STEPS_KEY, steps);
        args.putInt(INTENT_EXTRA_STEP_SELECTED_KEY, stepNumber);
        StepDetailsFragment stepDetailsFragment = new StepDetailsFragment();
        stepDetailsFragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.recipe_detail_container, stepDetailsFragment).commit();
    }

    private void removeFragmentsFromBackstack(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i){
            fragmentManager.popBackStack();
        }
    }

    private void showToast(String message){
        if (mToast != null){
            mToast.cancel();
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
