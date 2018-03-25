package com.example.android.bakingapp.Utilities;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * A singleton class for Volley library functionality. Singleton means that there is only one object of this class
 * that will last the lifetime of the app.
 */

public class VolleyUtility {
    private static Context mContext;
    //Used to detect if the object has already been initialized
    private static VolleyUtility mVolleyUtility;
    //The Request que manages worker threads for running the network operations
    private RequestQueue mRequestQue;

    //private constructor so no more that 1 object can be created
    private VolleyUtility(Context context){
        mContext = context;
        mRequestQue = getRequestQue();
    }

    //To allow only one Volley Utility object
    public static synchronized VolleyUtility getInstance (Context context){
        if(mVolleyUtility == null){
            mVolleyUtility = new VolleyUtility(context);
        }
        return mVolleyUtility;
    }

    //The Request que manages worker threads for running the network operations
    public RequestQueue getRequestQue(){
        if (mRequestQue == null){
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQue;
    }

    //All requests in Volley are placed in a queue first and then processed
    //Adds a request to the que
    public <T> void addToRequestQueue(Request<T> req){
        getRequestQue().add(req);
    }

    public void cancelPendingRequests (Object tag){
        if (mRequestQue != null){
            mRequestQue.cancelAll(tag);
        }
    }
}
