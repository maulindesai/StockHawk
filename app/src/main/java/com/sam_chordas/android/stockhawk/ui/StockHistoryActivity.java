package com.sam_chordas.android.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.HistoryBean;
import com.sam_chordas.android.stockhawk.service.StockHistoryIntentService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StockHistoryActivity extends AppCompatActivity implements View.OnClickListener,DatePickerFragment.OnDateChangeListener {

    private static final String TAG = "StockHistory";
    public static final String EXTRA_SYMBOL_NAME = "stock_quote_symbol";
    private Button mStartDate;
    private Button mEndDate;
    private LineChartView lineChartView;
    private BroadcastReceiver mBroadCastReceiver;
    private Button mButtonGo;
    private String quote_symbol;
   // private Tooltip mToolTip;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("Start-DATE",mStartDate.getText().toString());
        outState.putString("End-DATE",mEndDate.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        lineChartView= (LineChartView) findViewById(R.id.linechart);
        // mToolTip=new Tooltip(this,R.layout.stock_tool_tip,R.id.symbol_name);
        mStartDate = (Button) findViewById(R.id.start_date);
        mEndDate = (Button) findViewById(R.id.end_date);
        mButtonGo= (Button) findViewById(R.id.btn_go);

        quote_symbol= getIntent().getExtras().getString(EXTRA_SYMBOL_NAME);

        //set current date default
        Calendar calendar=Calendar.getInstance();
        Date currentDate=calendar.getTime();

        calendar.add(Calendar.DATE,-1);
        Date previousDate=calendar.getTime();

        if(savedInstanceState==null) {
            mStartDate.setText(parseDate(currentDate, "yyyy-MM-dd"));
            mEndDate.setText(parseDate(previousDate, "yyyy-MM-dd"));
        } else {
            mStartDate.setText(savedInstanceState.getString("Start-DATE"));
            mEndDate.setText(savedInstanceState.getString("End-DATE"));
        }

        //set click listener to open dialog fragment
        mStartDate.setOnClickListener(this);
        mEndDate.setOnClickListener(this);
        mButtonGo.setOnClickListener(this);

        StockHistoryIntentService.startActionFetchHistory(this,mStartDate.getText().toString()
                ,mEndDate.getText().toString(),quote_symbol);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter=new IntentFilter();
        filter.addAction(StockHistoryIntentService.HistoryIntentAction);
        mBroadCastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message=intent.getExtras().getString("message");
                List<HistoryBean> beans=parseMessage(message);
                if(beans!=null) {
                    LineSet lineSet = new LineSet();
                    for(HistoryBean historyBean:beans) {
                        lineSet.addPoint(new Point(historyBean.getDate(), (float) historyBean.getOpen()));
                        lineSet.setColor(Color.parseColor("#53c1bd"))
                                .setFill(Color.parseColor("#3d6c73"))
                                .setDashed(new float[]{10f,10f})
                                .setGradientFill(new int[]{Color.parseColor("#364d5a"), Color.parseColor("#3f7178")}, null);
                    }
                    lineChartView.addData(lineSet);

                    lineChartView.setStep(100);
                    lineChartView.setLabelsColor(Color.parseColor("#6a84c3"));

                    lineChartView.show();
                } else {
                    Snackbar.make(lineChartView, R.string.no_history_found,Snackbar.LENGTH_LONG).show();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadCastReceiver,filter);
    }

    /**
     * parse message
     * @param message json string
     * @return null if no record found OR invalid json
     */
    private List<HistoryBean> parseMessage(String message) {
        if(message==null) {
            return null;
        }
        ArrayList<HistoryBean> historyBeanArrayList=new ArrayList<>();
        try {
            JSONObject jsonObject=new JSONObject(message);
            JSONObject query=jsonObject.getJSONObject("query");
            int count=query.getInt("count");
            if(count==0) {
                return null;
            } else {
                JSONObject results=query.getJSONObject("results");
                JSONArray quotes=results.getJSONArray("quote");
                for(int i=0;i<quotes.length();i++) {
                    HistoryBean historyBean=new HistoryBean();
                    String symbol = quotes.getJSONObject(i).getString("Symbol");
                    String date = quotes.getJSONObject(i).getString("Date");
                    String open = quotes.getJSONObject(i).getString("Open");
                    String high = quotes.getJSONObject(i).getString("High");
                    String low = quotes.getJSONObject(i).getString("Low");
                    String close = quotes.getJSONObject(i).getString("Close");

                    historyBean.setSymbol(symbol);
                    historyBean.setOpen(Float.parseFloat(open));
                    historyBean.setClose(Float.parseFloat(close));
                    historyBean.setHigh(Float.parseFloat(high));
                    historyBean.setLow(Float.parseFloat(low));
                    historyBean.setDate(date);
                    historyBeanArrayList.add(historyBean);
                }
                return historyBeanArrayList;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadCastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_date:
                DatePickerFragment datePickerFragment=DatePickerFragment.newInstance(true);
                datePickerFragment.show(getSupportFragmentManager(),"datePicker");
                break;
            case R.id.end_date:
                datePickerFragment=DatePickerFragment.newInstance(false);
                datePickerFragment.show(getSupportFragmentManager(),"datePicker");
                break;
            case R.id.btn_go:
                StockHistoryIntentService.startActionFetchHistory(this,mStartDate.getText().toString()
                        ,mEndDate.getText().toString(),quote_symbol);
                break;
        }
    }

    //parse date with specific format
    String parseDate(Date date,String pattern) {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(pattern, Locale.US);
         return simpleDateFormat.format(date);
    }

    @Override
    public void onDateChangeListener(long date,boolean isStartDate) {
        if(isStartDate) {
            mStartDate.setText(parseDate(new Date(date),"yyyy-MM-dd"));
        } else {
            mEndDate.setText(parseDate(new Date(date),"yyyy-MM-dd"));
        }
    }
}
