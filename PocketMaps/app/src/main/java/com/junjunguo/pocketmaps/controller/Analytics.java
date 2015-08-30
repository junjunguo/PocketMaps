package com.junjunguo.pocketmaps.controller;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;

import java.util.Random;

public class Analytics extends AppCompatActivity {
    private final Handler mHandler = new Handler();
    private GraphView graph = (GraphView) findViewById(R.id.analytics_graph);
    private Runnable mTimer1;
    private Runnable mTimer2;
    private LineGraphSeries<DataPoint> speed;
    private LineGraphSeries<DataPoint> distance;
    private double graph2LastXValue = 5d;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //         set status bar
        new SetStatusBarColor().setStatusBarColor(findViewById(R.id.statusBarBackgroundDownload),
                getResources().getColor(R.color.my_primary_dark), this);

        initGraph();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initGraph() {
        graph = (GraphView) findViewById(R.id.analytics_graph);
        speed = new LineGraphSeries<>(generateData());
        graph.addSeries(speed);

        distance = new LineGraphSeries<>();
        //        graph.getViewport().setXAxisBoundsManual(true);


        graph.getGridLabelRenderer()
                .setLabelFormatter(new StaticLabelsFormatter(graph, null, new String[]{"low", "middle", "high"}));

        // set second scale
        graph.getSecondScale().addSeries(distance);
        // the y bounds are always manual for second scale
        graph.getSecondScale().setMinY(0);
        graph.getSecondScale().setMaxY(100);
        distance.setColor(Color.RED);
        graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.RED);

        // legend
        speed.setTitle("Speed");
        distance.setTitle("Distance");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph.getViewport().setBackgroundColor(Color.GRAY);
    }

    private DataPoint[] generateData() {
        int count = 30;
        DataPoint[] values = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            double x = i;
            double f = mRand.nextDouble() * 0.15 + 0.3;
            double y = Math.sin(i * f + 2) + mRand.nextDouble() * 0.3;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }

    double mLastRandom = 2;
    Random mRand = new Random();

    private double getRandom() {
        return mLastRandom += mRand.nextDouble() * 0.5 - 0.25;
    }

    @Override public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override public void run() {
                speed.resetData(generateData());
                mHandler.postDelayed(this, 300);
            }
        };
        mHandler.postDelayed(mTimer1, 300);

        mTimer2 = new Runnable() {
            @Override public void run() {
                graph2LastXValue += 1d;
                distance.appendData(new DataPoint(graph2LastXValue, getRandom()), true, 40);
                mHandler.postDelayed(this, 200);
            }
        };
        mHandler.postDelayed(mTimer2, 1000);
    }

    @Override public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        mHandler.removeCallbacks(mTimer2);
        super.onPause();
    }

    private void log(String s) {
        System.out.println(this.getClass().getSimpleName() + "-------------------" + s);
    }
}
