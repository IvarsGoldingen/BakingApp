package com.example.android.bakingapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bakingapp.RecipieObjects.Step;
import com.example.android.bakingapp.Utilities.VolleyUtility;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ivars on 3/10/18.
 */

public class RecipeDetailsFragment extends Fragment implements
StepAdapter.StepListItemClickListener{

    @BindView (R.id.recipe_details_rv)
    RecyclerView stepRv;

    //tag to cancel thumbnail volley request
    private static final String THUMBNAIL_IMAGE_REQUEST_TAG = "get_thumbnail_tag";
    private static final String INTENT_EXTRA_STEP_LIST_KEY = "step_list";


    private ArrayList<Step> mSteps;
    private StepAdapter mStepAdapter;
    //Inteface that triggers a callback in the host activity
    OnItemClickListener mCallback;
    //calls a method in the host activity
    public interface OnItemClickListener {
        void onIngreedientsClicked();
        void onStepClicked(int stepNumber);
    }

    //empty constructor, necessary to implement the fragment
    public RecipeDetailsFragment() {
    }

    //similar to onCreate for activity
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate
                (R.layout.fragment_recipe_detail_view, container, false);

        ButterKnife.bind(this, rootView);
        mSteps = (ArrayList)getArguments().getSerializable(INTENT_EXTRA_STEP_LIST_KEY);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        stepRv.setLayoutManager(layoutManager);
        mStepAdapter = new StepAdapter(getContext(), mSteps, this);
        stepRv.setAdapter(mStepAdapter);

        return rootView;
    }

    @Override
    public void onStepListItemClick(int clickedStep) {
        mCallback.onStepClicked(clickedStep);
    }

    @Override
    public void onIngredientsClick() {
        mCallback.onIngreedientsClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //make sure the host activity implements the callback
        try {
            mCallback = (OnItemClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + " must implement OnItemClickListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelThumbnailDownloads();
    }

    //cancel thumbnail download if the recipe details fragment is destroyed
    private void cancelThumbnailDownloads(){
        VolleyUtility.getInstance(getContext()).cancelPendingRequests(THUMBNAIL_IMAGE_REQUEST_TAG);
    }
}
