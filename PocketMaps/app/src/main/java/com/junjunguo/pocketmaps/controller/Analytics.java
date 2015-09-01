package com.junjunguo.pocketmaps.controller;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.dataType.AnalyticsActivityType;
import com.junjunguo.pocketmaps.model.listeners.TrackingListener;
import com.junjunguo.pocketmaps.model.map.Tracking;
import com.junjunguo.pocketmaps.model.util.Calorie;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.model.util.SpinnerAdapter;

import java.util.ArrayList;
import java.util.Random;

public class Analytics extends AppCompatActivity implements TrackingListener {
    // status   -----------------
    private Spinner spinner;
    private TextView durationTV, avgSpeedTV, maxSpeedTV, distanceTV, distanceUnitTV, caloriesTV;
    // duration
    private long durationStartTime;
    private Handler durationHandler;
    private Handler calorieUpdateHandler;


    // graph   -----------------
    private final Handler mHandler = new Handler();
    private GraphView graph;
    private Runnable mTimer1;
    private Runnable mTimer2;
    private LineGraphSeries<DataPoint> speed;
    private LineGraphSeries<DataPoint> distance;
    private double graph2LastXValue = 5d;

    @Override protected void onCreate(Bundle savedInstanceState) {
        //        log("on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //         set status bar
        new SetStatusBarColor().setStatusBarColor(findViewById(R.id.statusBarBackgroundDownload),
                getResources().getColor(R.color.my_primary), this);
        // status
        durationStartTime = 0L;
        intSpinner();
        initStatus();
        // graph
        initGraph();
        durationHandler = new Handler();
        calorieUpdateHandler = new Handler();
    }

    // ----------  status ---------------
    private void intSpinner() {
        spinner = (Spinner) findViewById(R.id.activity_analytics_spinner);

        ArrayList<AnalyticsActivityType> spinnerlist = new ArrayList<>();
        spinnerlist.add(new AnalyticsActivityType("run", R.drawable.ic_directions_run_white_24dp, Calorie.running));
        spinnerlist.add(new AnalyticsActivityType("walk", R.drawable.ic_directions_walk_white_24dp, Calorie.walking));
        spinnerlist.add(new AnalyticsActivityType("bike", R.drawable.ic_directions_bike_white_24dp, Calorie.bicycling));

        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.analytics_activity_type, spinnerlist);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parentView, View v, int position, long id) {
                updateCalorieBurned();
            }

            @Override public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    /**
     * init status text views
     */
    private void initStatus() {
        durationStartTime = Tracking.getTracking().getTimeStart();

        distanceTV = (TextView) findViewById(R.id.activity_analytics_distance);
        distanceUnitTV = (TextView) findViewById(R.id.activity_analytics_distance_unit);
        caloriesTV = (TextView) findViewById(R.id.activity_analytics_calories);
        maxSpeedTV = (TextView) findViewById(R.id.activity_analytics_max_speed);
        durationTV = (TextView) findViewById(R.id.activity_analytics_duration);
        avgSpeedTV = (TextView) findViewById(R.id.activity_analytics_avg_speed);

        updateDis(Tracking.getTracking().getDistance());
        updateAvgSp(Tracking.getTracking().getAvgSpeed());
        updateMaxSp(Tracking.getTracking().getMaxSpeed());
        updateCalorieBurned();
    }

    /**
     * update avg speed
     *
     * @param avgSpeed
     */
    public void updateAvgSp(float avgSpeed) {
        avgSpeedTV.setText(String.format("%.2f", avgSpeed));
    }

    private void updateMaxSp(float maxSpeed) {
        maxSpeedTV.setText(String.format("%.2f", maxSpeed));
    }

    private void updateCalorieBurned() {
        caloriesTV.setText(String.valueOf(
                (int) Calorie.CalorieBurned(getSportActivity(), Tracking.getTracking().getDurationInHours())));
    }

    /**
     * get activity type (MET) from spinner (object)
     */
    private double getSportActivity() {
        return ((AnalyticsActivityType) spinner.getSelectedItem()).getActivityMET();
    }

    /**
     * update distance
     *
     * @param distance
     */
    public void updateDis(float distance) {
        if (distance < 1000) {
            distanceTV.setText(String.valueOf(Math.round(distance)));
            distanceUnitTV.setText(R.string.meter);
        } else {
            distanceTV.setText(String.format("%.2f", distance / 1000));
            distanceUnitTV.setText(R.string.km);
        }
    }


    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            long updatedTime = System.currentTimeMillis() - durationStartTime;
            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int hours = mins / 60;
            mins = mins % 60;
            durationTV.setText("" + String.format("%02d", hours) + ":" + String.format("%02d", mins) + ":" +
                    String.format("%02d", secs));
            durationHandler.postDelayed(this, 500);
        }

    };

    private Runnable updateCalorieThread = new Runnable() {
        public void run() {
            updateCalorieBurned();
            calorieUpdateHandler.postDelayed(this, 60000);
        }
    };

    // ----------  graph ---------------
    private void initGraph() {
        graph = (GraphView) findViewById(R.id.analytics_graph);
        speed = new LineGraphSeries<>(generateData());
        graph.addSeries(speed);
        speed.setColor(0xFF009688);
        graph.getGridLabelRenderer().setVerticalLabelsColor(0xFF009688);
        graph.getGridLabelRenderer().setVerticalLabelsColor(0xFF009688);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMinX(0);

        distance = new LineGraphSeries<>();
        //        graph.getViewport().setXAxisBoundsManual(true);

        // set second scale
        graph.getSecondScale().addSeries(distance);
        // the y bounds are always manual for second scale
        graph.getSecondScale().setMinY(0);
        //        graph.getSecondScale().setMaxY(100);
        resetScale(10, 10, 60);

        distance.setColor(0xFFFF5722);
        graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(0xFFFF5722);
        // legend
        speed.setTitle("Speed km/h");
        distance.setTitle("Distance km");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    private void resetScale(int speedScale, int distanceScale, int maxXScale) {
        graph.getViewport().setMaxY(speedScale);
        graph.getSecondScale().setMaxY(distanceScale);
        graph.getViewport().setMaxX(maxXScale);
    }


    private DataPoint[] getSpeed() {
        //    TODO: implement get speed datapoint & distance datapoint
        return null;
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
        durationHandler.postDelayed(updateTimerThread, 500);
        calorieUpdateHandler.postDelayed(updateCalorieThread, 60000);
        Tracking.getTracking().addListener(this);
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
        durationHandler.removeCallbacks(updateTimerThread);
        calorieUpdateHandler.removeCallbacks(updateCalorieThread);
        Tracking.getTracking().removeListener(this);
        mHandler.removeCallbacks(mTimer1);
        mHandler.removeCallbacks(mTimer2);
        super.onPause();
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


    public void updateDistance(Float distance) {
        updateDis(distance);
    }

    public void updateAvgSpeed(Float avgSpeed) {
        updateAvgSp(avgSpeed);
    }

    public void updateMaxSpeed(Float maxSpeed) {
        updateMaxSp(maxSpeed);
    }

    private void log(String s) {
        System.out.println(this.getClass().getSimpleName() + "-------------------" + s);
    }
}
