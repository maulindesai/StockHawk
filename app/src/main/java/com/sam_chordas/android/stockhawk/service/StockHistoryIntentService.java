package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * fetching history of stock price
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class StockHistoryIntentService extends IntentService {

    private static final String ACTION_FETCH_HISTORY = "co.sam_chordas.android.stockhawk.service.action.fetchHistory";

    private static final String EXTRA_STARTDATE = "com.sam_chordas.android.stockhawk.service.extra.STARTDATE";
    private static final String EXTRA_ENDDATE = "com.sam_chordas.android.stockhawk.service.extra.ENDDATE";
    private static final String EXTRA_STOCK_SYMBOLE = "com.sam_chordas.android.stockhawk.service.extra.stock_symbol";
    private static final String TAG = "stockhisory";
    private OkHttpClient client;
    public static String HistoryIntentAction="historyIntentAction";

    public StockHistoryIntentService() {
        super("StockHistoryIntentService");
    }

    /**
     * Starts this service to perform action Fetch history with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchHistory(Context context, String startDate, String endDate, String stockSymbol) {
        Intent intent = new Intent(context, StockHistoryIntentService.class);
        intent.setAction(ACTION_FETCH_HISTORY);
        intent.putExtra(EXTRA_STARTDATE, startDate);
        intent.putExtra(EXTRA_ENDDATE, endDate);
        intent.putExtra(EXTRA_STOCK_SYMBOLE,stockSymbol);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        client=new OkHttpClient();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_HISTORY.equals(action)) {
                final String stockSymbol = intent.getStringExtra(EXTRA_STOCK_SYMBOLE);
                final String endDate = intent.getStringExtra(EXTRA_ENDDATE);
                final String startDate= intent.getStringExtra(EXTRA_STARTDATE);
                try {
                    handleActionFetchHistory(stockSymbol, startDate ,endDate);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private void handleActionFetchHistory(String stockSymbol, String startDate, String endDate) throws UnsupportedEncodingException {
        Log.d(TAG,startDate+" end date "+endDate);
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
        stringBuilder.append("select * from yahoo.finance.historicaldata where symbol in (")
                .append("\""+stockSymbol+"\"")
                .append(") and startDate = \"")
                .append(startDate+"\"").append(" and endDate = \"")
                .append(endDate+"\"");
        // finalize the URL for the API query.
        stringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");
        try {
            Log.d(TAG,stringBuilder.toString());
           String output=fetchData(stringBuilder.toString());
            Log.d(TAG,output);
            //send local broadcast to history activity
            sendBroadCast(StockHistoryIntentService.this,output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * send broadcast
     * @param context current context
     * @param message send message
     */
    void sendBroadCast(Context context,String message) {
        LocalBroadcastManager broadcastManager=LocalBroadcastManager.getInstance(context);
        Intent intent=new Intent(HistoryIntentAction);
        intent.putExtra("message",message);
        broadcastManager.sendBroadcast(intent);
    }
}
