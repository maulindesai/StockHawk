package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by maulin on 19/6/16.
 */
public class QuoteWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuoteWidgetFactory(this.getApplicationContext(),intent);
    }

    public class QuoteWidgetFactory implements RemoteViewsFactory {

        private final Context mContext;
        private int mAppWidgetId;
        private Cursor mCursor;

        public QuoteWidgetFactory(Context context, Intent intent) {
            mContext=context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            // Refresh the cursor
            if (mCursor != null) {
                mCursor.close();
            }
            mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,  new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP}, QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"}, null);
        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
            }
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            // Get the data for this position from the content provider
            String symbolName = getString(R.string.unknown_symbol);
            String Change = "";
            if (mCursor.moveToPosition(position)) {
                final int symbolIndex = mCursor.getColumnIndex(QuoteColumns.SYMBOL);
                final int changeIndex = mCursor.getColumnIndex(
                        QuoteColumns.CHANGE);
                symbolName = mCursor.getString(symbolIndex);
                Change = mCursor.getString(changeIndex);
            }

            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_collection_item);
            rv.setTextViewText(R.id.stock_symbol, symbolName);
            rv.setTextViewText(R.id.change,Change);
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
