package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by maulin on 19/6/16.
 */

public class QuoteWidgetProvider1 extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d("recive ","receive"+action);
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        for (int appWidgetId : appWidgetIds) {
            RemoteViews layout = buildLayout(context, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, layout);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    RemoteViews buildLayout(Context context, int appWidgetId) {
        RemoteViews rv;
        final Intent intent = new Intent(context, QuoteWidgetRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        rv = new RemoteViews(context.getPackageName(), R.layout.widget_collection);
        rv.setRemoteAdapter(appWidgetId, R.id.widget_list, intent);
         return rv;
    }
}
