package com.example.android.bakingapp;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Connects the remote adapter to be able to request remote views
 */

public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this);
    }
}
