package com.example.android.bakingapp;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.bakingapp.RecipieObjects.Ingredient;
import com.example.android.bakingapp.RecipieObjects.Recipe;
import com.example.android.bakingapp.Utilities.JsonUtility;
import com.example.android.bakingapp.Utilities.VolleyUtility;

import org.json.JSONArray;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.ListItemClickListener{

    //JSON url
    private static final String REQUEST_URL =
            "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";
    //Adapter for the recycler view
    private RecipeAdapter mRecipeAdapter;
    ArrayList<Recipe> recipeList = null;

    @BindView(R.id.recipe_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.error_getting_data_tv)
    TextView mErrorTv;

    private static final String INTENT_EXTRA_RECIPE_KEY = "recipe_object";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getJsonResponse();

        //A linear layout manager manages how all the viewholders are displayed in the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //set the linear layout manager to the recycler viwe
        mRecyclerView.setLayoutManager(layoutManager);
        mRecipeAdapter = new RecipeAdapter(this, this);
        //Set the recipeAdapter on the recyclerView
        mRecyclerView.setAdapter(mRecipeAdapter);
    }

    private void setWidgetData(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int widgetRecipeId = sharedPreferences.getInt(getString(R.string.preference_widget_recipe_id), 0);
        if (widgetRecipeId > recipeList.size()){
            //check if the saved preference is not out of bounds of the array
            widgetRecipeId = 0;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int [] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, IngredientsWidgetProvider.class));
        IngredientsWidgetProvider.updateRecipeWidgets(this, appWidgetManager,
                appWidgetIds,recipeList.get(widgetRecipeId));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
    }

    //Method that uses Volley library to get a JSON response
    private void getJsonResponse(){
        //show the loading indicator on start of network connection
        mLoadingIndicator.setVisibility(View.VISIBLE);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                REQUEST_URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                        recipeList = JsonUtility.readJsonArray(response);
                        mRecipeAdapter.swapList(recipeList);
                        //update the widget each time we receive data
                        setWidgetData();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                        mErrorTv.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        VolleyUtility.getInstance(this).addToRequestQueue(request);
    }

    @Override
    public void onListItemClick(int clickedItemId) {
        Intent intent = new Intent(this, RecipeDetailsActivity.class);
        intent.putExtra(INTENT_EXTRA_RECIPE_KEY, recipeList.get(clickedItemId));
        startActivity(intent);
    }
}
