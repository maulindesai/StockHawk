package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

  public static final String StockIntentAction="stockintentAction";

  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra("tag").equals("add")){
      args.putString("symbol", intent.getStringExtra("symbol"));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    int result=stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    if(result==StockTaskService.NO_STOCK_SYMBOL_FOUND) {
      sendBroadCast(this,result);
    }
  }

  /**
   * send broadcast
   * @param context current context
   * @param message send message
   */
  void sendBroadCast(Context context, int message) {
    LocalBroadcastManager broadcastManager=LocalBroadcastManager.getInstance(context);
    Intent intent=new Intent(StockIntentAction);
    intent.putExtra(getString(R.string.message_tag),message);
    broadcastManager.sendBroadcast(intent);
  }
}
