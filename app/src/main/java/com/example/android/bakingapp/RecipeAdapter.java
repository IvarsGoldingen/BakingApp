package com.example.android.bakingapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.bakingapp.RecipieObjects.Recipe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A recipe adapter to use with the recycler view
 */

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeAdapterViewHolder> {

    private Context mContext;
    private ArrayList<Recipe> mRecipeList;

    final private ListItemClickListener mOnClickListener;

    /*
    * An interface for on item click handling is used, so the clicks can be handled in the main
    * activity
    * */
    public interface ListItemClickListener{
        void onListItemClick(int clickedItemId);
    }

    //Constructor that gets the context
    public RecipeAdapter(Context mContext, ListItemClickListener listener) {
        this.mContext = mContext;
        mOnClickListener = listener;
    }

    /*
    * gets called when each viewholder is created
    * happens when the recycler view is first laid out
    * ViewGroup
    * */
    @Override
    public RecipeAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recipe_card_item,parent,false);
        return  new RecipeAdapterViewHolder(view);
    }

    //Called to display the data of a specific position
    //The holder is the view which will be used for data displaying
    //position in used to know which item should be displayed in this view
    @Override
    public void onBindViewHolder(RecipeAdapterViewHolder holder, int position) {
        Recipe currentRecipe = mRecipeList.get(position);
        holder.itemTextView.setText(currentRecipe.getName());
    }

    public void swapList(ArrayList<Recipe> list){
        mRecipeList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mRecipeList != null){
            return mRecipeList.size();
        }
        return 0;
    }

    //ViewHolder removes the need to call the findviewbyid every time a new item need to be created
    class RecipeAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.recipe_item_iv)
        ImageView itemImage;
        @BindView(R.id.recipe_item_tv)
        TextView itemTextView;

        public RecipeAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        /*
        * We override the on click method so we can passed the clicked items position to the
        * main activity
        * */
        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}
