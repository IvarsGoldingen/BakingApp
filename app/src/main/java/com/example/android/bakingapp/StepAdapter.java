package com.example.android.bakingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.bakingapp.RecipieObjects.Step;
import com.example.android.bakingapp.Utilities.VolleyUtility;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ivars on 3/13/18.
 */

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepAdapterViewHolder> {

    private static final String THUMBNAIL_IMAGE_REQUEST_TAG = "get_thumbnail_tag";

    private Context mContext;
    private ArrayList<Step> mStepList;
    final private StepListItemClickListener mOnClickListener;

    public interface StepListItemClickListener{
        void onStepListItemClick(int clickedStep);
        void onIngredientsClick();
    }

    public StepAdapter(Context context, ArrayList<Step> list, StepListItemClickListener listener) {
        mContext = context;
        mStepList = list;
        mOnClickListener = listener;
    }

    @Override
    public StepAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.step_list_item, parent, false);
        return new StepAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StepAdapterViewHolder holder, int position) {
        //Add the ingredients as a list item in the 0 position in the list
        if (position == 0){
            holder.stepItemTv.setText("Ingredients");
            holder.stepThumbnailIv.setImageResource(R.drawable.ic_ingredients_black_48dp);
        } else {
            Step currentStep = mStepList.get(position - 1);
            holder.stepItemTv.setText(currentStep.getShortDescription());
            String stepThumbNailUrlString = currentStep.getThumbnailUrl();
            //Check if the step has a thumbnail
            if (stepThumbNailUrlString != null && !stepThumbNailUrlString.isEmpty()) {
                //download the thumbnail if it is available
                //use volley to download the image
                ImageRequest imageRequest = new ImageRequest(
                        stepThumbNailUrlString,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                holder.stepThumbnailIv.setImageBitmap(response);
                            }
                        },
                        0, 0,
                        ImageView.ScaleType.FIT_XY,
                        Bitmap.Config.ARGB_8888,
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                holder.stepThumbnailIv.setImageResource(R.drawable.ic_assignment_black_48dp);
                            }
                        }
                );
                imageRequest.setTag(THUMBNAIL_IMAGE_REQUEST_TAG);
                VolleyUtility.getInstance(mContext).addToRequestQueue(imageRequest);
            } else {
                holder.stepThumbnailIv.setImageResource(R.drawable.ic_assignment_black_48dp);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mStepList != null) {
            //Because first item is ingredients
            return mStepList.size() +1;
        }
        return 0;
    }

    class StepAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.step_thumbnail_iv)
        ImageView stepThumbnailIv;
        @BindView(R.id.step_list_item_tv)
        TextView stepItemTv;

        public StepAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int stepItemClicked = getAdapterPosition()-1;
            if (stepItemClicked < 0){
                mOnClickListener.onIngredientsClick();
            } else {
                mOnClickListener.onStepListItemClick(stepItemClicked);
            }
        }
    }
}
