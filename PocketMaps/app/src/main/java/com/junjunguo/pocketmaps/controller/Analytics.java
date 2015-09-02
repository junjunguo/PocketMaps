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

public class Analytics extends AppCompatActivity implements TrackingListener {
    // status   -----------------
    private Spinner spinner;
    private TextView durationTV, avgSpeedTV, maxSpeedTV, distanceTV, distanceUnitTV, caloriesTV;
    // duration
    private long durationStartTime;
    private Handler durationHandler;
    private Handler calorieUpdateHandler;


    // graph   -----------------
    private GraphView graph;
    private LineGraphSeries<DataPoint> speedGraphSeries;
    private LineGraphSeries<DataPoint> distanceGraphSeries;

    @Override protected void onCreate(Bundle savedInstanceState) {
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
        durationHandler = new Handler();
        calorieUpdateHandler = new Handler();
        // graph
        initGraph();
    }

    // ----------  status ---------------
    private void intSpinner() {
        spinner = (Spinner) findViewById(R.id.activity_analytics_spinner);

        ArrayList<AnalyticsActivityType> spinnerList = new ArrayList<>();
        spinnerList.add(new AnalyticsActivityType("run", R.drawable.ic_directions_run_white_24dp, Calorie.running));
        spinnerList.add(new AnalyticsActivityType("walk", R.drawable.ic_directions_walk_white_24dp, Calorie.walking));
        spinnerList.add(new AnalyticsActivityType("bike", R.drawable.ic_directions_bike_white_24dp, Calorie.bicycling));

        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.analytics_activity_type, spinnerList);
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
     * update avg speedGraphSeries
     *
     * @param avgSpeed
     */
    public void updateAvgSp(double avgSpeed) {
        avgSpeedTV.setText(String.format("%.2f", avgSpeed));
    }

    private void updateMaxSp(double maxSpeed) {
        maxSpeedTV.setText(String.format("%.2f", maxSpeed));
    }

    private void updateCalorieBurned() {
        caloriesTV.setText(String.format("%.2f",
                Calorie.CalorieBurned(getSportActivity(), Tracking.getTracking().getDurationInHours())));
    }

    /**
     * get activity type (MET) from spinner (object)
     */
    private double getSportActivity() {
        return ((AnalyticsActivityType) spinner.getSelectedItem()).getActivityMET();
    }

    /**
     * update distanceGraphSeries
     *
     * @param distance
     */
    public void updateDis(double distance) {
        if (distance < 1000) {
            distanceTV.setText(String.valueOf(Math.round(distance)));
            distanceUnitTV.setText(R.string.meter);
        } else {
            distanceTV.setText(String.format("%.2f", distance / 1000));
            distanceUnitTV.setText(R.string.km);
        }
    }

    /**
     * new thread to update timer
     */
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

    /**
     * new thread to update calorie burned every minutes
     */
    private Runnable updateCalorieThread = new Runnable() {
        public void run() {
            updateCalorieBurned();
            calorieUpdateHandler.postDelayed(this, 60000);
        }
    };

    // ----------  graph ---------------

    /**
     * init and setup Graph Contents
     */
    private void initGraph() {
        graph = (GraphView) findViewById(R.id.analytics_graph);

        speedGraphSeries = new LineGraphSeries<>();
        graph.addSeries(speedGraphSeries);
        speedGraphSeries.setColor(0xFF009688);
        graph.getGridLabelRenderer().setVerticalLabelsColor(0xFF009688);
        distanceGraphSeries = new LineGraphSeries<>();

        // set second scale
        graph.getSecondScale().addSeries(distanceGraphSeries);
        // the y bounds are always manual for second scale
        graph.getSecondScale().setMinY(0);
        resetGraphY2MaxValue();
        distanceGraphSeries.setColor(0xFFFF5722);
        graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(0xFFFF5722);
        // legend
        speedGraphSeries.setTitle("Speed km/h");
        distanceGraphSeries.setTitle("Distance km");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    /**
     * auto setup max value for graph second y scale
     */
    public void resetGraphY2MaxValue() {
        double max = 0.4;
        double dis = Tracking.getTracking().getDistanceKm();
        if (dis < 0.35) {
            max = 0.4;
        } else {
            max = getMaxValue(dis, max);
        }
        graph.getSecondScale().setMaxY(max);
    }

    /**
     * @param dis
     * @param max
     * @return max * 2 until max >= dis * 1.2
     */
    private double getMaxValue(double dis, double max) {
        if (max < dis * 1.2) {
            getMaxValue(dis, max * 2);
        }
        return max;
    }

    @Override public void onResume() {
        super.onResume();
        durationHandler.postDelayed(updateTimerThread, 500);
        calorieUpdateHandler.postDelayed(updateCalorieThread, 60000);
        Tracking.getTracking().addListener(this);
        //        graph
        Tracking.getTracking().requestDistanceGraphSeries();
    }

    @Override public void onPause() {
        super.onPause();
        durationHandler.removeCallbacks(updateTimerThread);
        calorieUpdateHandler.removeCallbacks(updateCalorieThread);
        Tracking.getTracking().removeListener(this);
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

    public void updateDistance(Double distance) {
        updateDis(distance);
    }

    public void updateAvgSpeed(Double avgSpeed) {
        updateAvgSp(avgSpeed);
    }

    public void updateMaxSpeed(Double maxSpeed) {
        updateMaxSp(maxSpeed);
    }

    public void updateDistanceGraphSeries(final DataPoint[][] dataPoints) {
        speedGraphSeries.resetData(dataPoints[0]);
        distanceGraphSeries.resetData(dataPoints[1]);
    }

    public void addDistanceGraphSeriesPoint(DataPoint speed, DataPoint distance) {
        speedGraphSeries.appendData(speed, true, 1);
        resetGraphY2MaxValue();
        distanceGraphSeries.appendData(distance, true, 1);
    }

    private void log(String s) {
        System.out.println(this.getClass().getSimpleName() + "-------------------" + s);
    }

}
