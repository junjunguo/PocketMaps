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
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.dataType.SportCategory;
import com.junjunguo.pocketmaps.model.listeners.TrackingListener;
import com.junjunguo.pocketmaps.model.map.Tracking;
import com.junjunguo.pocketmaps.model.util.Calorie;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.model.util.SpinnerAdapter;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.util.ArrayList;
/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class Analytics extends AppCompatActivity implements TrackingListener {
    // status   -----------------
    /**
     * a sport category spinner: to choose with type of sport which also has MET value in its adapter
     */
    private Spinner spinner;
    private TextView durationTV, avgSpeedTV, maxSpeedTV, distanceTV, distanceUnitTV, caloriesTV;
    // duration
    private long durationStartTime;
    private Handler durationHandler;
    private Handler calorieUpdateHandler;

    // graph   -----------------
    /**
     * max value for it's axis (minimum 0)
     * <p/>
     * <li>X axis: time - hours</li> <li>Y1 (left) axis: speed - km/h</li> <li>Y2 (right) axis: distance - km</li>
     */
    private double maxXaxis, maxY1axis, maxY2axis;
    /**
     * has a new point needed to update to graph view
     */
    private boolean hasNewPoint;
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

        ArrayList<SportCategory> spinnerList = new ArrayList<>();
        spinnerList.add(new SportCategory("run", R.drawable.ic_directions_run_white_24dp, Calorie.running));
        spinnerList.add(new SportCategory("walk", R.drawable.ic_directions_walk_white_24dp, Calorie.walking));
        spinnerList.add(new SportCategory("bike", R.drawable.ic_directions_bike_white_24dp, Calorie.bicycling));

        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.analytics_activity_type, spinnerList);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(Variable.getVariable().getSportCategoryIndex());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parentView, View v, int position, long id) {
                updateCalorieBurned();
                Variable.getVariable().setSportCategoryIndex(position);
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
                Calorie.CalorieBurned(getSportCategory(), Tracking.getTracking().getDurationInHours())));
    }

    /**
     * get activity type (MET) from selected spinner (object)
     */
    private double getSportCategory() {
        return ((SportCategory) spinner.getSelectedItem()).getSportMET();
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
     * new thread to update calorie burned every 10 second
     */
    private Runnable updateCalorieThread = new Runnable() {
        public void run() {
            updateCalorieBurned();
            calorieUpdateHandler.postDelayed(this, 10000);
            // reload graph
            if (hasNewPoint) {
                Tracking.getTracking().requestDistanceGraphSeries();
                hasNewPoint = false;
            }
        }
    };

    // ----------  graph ---------------

    /**
     * init and setup Graph Contents
     */
    private void initGraph() {
        hasNewPoint = false;
        maxXaxis = 0.1;
        maxY1axis = 10;
        maxY2axis = 0.4;
        graph = (GraphView) findViewById(R.id.analytics_graph);

        speedGraphSeries = new LineGraphSeries<>();
        graph.addSeries(speedGraphSeries);

        graph.getGridLabelRenderer().setVerticalLabelsColor(0xFF009688);
        graph.getViewport().setYAxisBoundsManual(true);
        resetGraphY1MaxValue();
        distanceGraphSeries = new LineGraphSeries<>();
        //        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);

        graph.getViewport().setMinX(0);
        // set second scale
        graph.getSecondScale().addSeries(distanceGraphSeries);
        // the y bounds are always manual for second scale
        graph.getSecondScale().setMinY(0);
        resetGraphY2MaxValue();
        //        resetGraphXMaxValue();
        graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(0xFFFF5722);
        // legend
        speedGraphSeries.setTitle("Speed km/h");
        speedGraphSeries.setColor(0xFF009688);
        distanceGraphSeries.setTitle("Distance km");
        distanceGraphSeries.setColor(0xFFFF5722);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    /**
     * auto setup max value for graph first y scale
     */
    public void resetGraphY1MaxValue() {
        double maxSpeed = Tracking.getTracking().getMaxSpeed();
        if (maxSpeed > maxY1axis) {
            int i = ((int) (maxSpeed + 0.9999));
            maxY1axis = i + 4 - (i % 4);
        }
        //        log("resetGraphY1MaxValue max speed: " + maxSpeed + "\n speedGraphSeries" +
        //                ".getHighestValueY() " + maxY1axis + "\nTracking().getMaxSpeed() " +
        //                Tracking.getTracking().getMaxSpeed());
        graph.getViewport().setMaxY(maxY1axis);
    }

    /**
     * auto setup max value for graph second y scale
     */
    public void resetGraphY2MaxValue() {
        //        double max = 0.4;
        double dis = Tracking.getTracking().getDistanceKm();
        if (dis > maxY2axis * 0.9) {
            maxY2axis = getMaxValue(dis, maxY2axis);
        }
        //        log("max Y: " + maxY2axis);
        graph.getSecondScale().setMaxY(maxY2axis);
    }

    /**
     * @param dis
     * @param max
     * @return max * 2 until max >= dis * 1.2
     */
    private double getMaxValue(double dis, double max) {
        if (max > dis * 1.1) {
            return max;
        }
        return getMaxValue(dis, max * 2);
    }

    public void resetGraphXMaxValue() {
        //        double max = 0.1;
        double time = Tracking.getTracking().getDurationInHours();
        if (time > maxXaxis * 0.9) {
            maxXaxis = getMaxValue(time, maxXaxis);
        } else {
        }
//        log("max X: " + maxXaxis + "; time: " + time);
        //        graph.getViewport().setXAxisBoundsManual(true);
        //        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(maxXaxis);
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

    /**
     * updated when {@link Tracking#requestDistanceGraphSeries()} is called
     *
     * @param dataPoints
     */
    public void updateDistanceGraphSeries(final DataPoint[][] dataPoints) {
        resetGraphY1MaxValue();
        resetGraphY2MaxValue();
        //        resetGraphXMaxValue();
        speedGraphSeries.resetData(dataPoints[0]);
        distanceGraphSeries.resetData(dataPoints[1]);
        double maxV = speedGraphSeries.getHighestValueY();
        Tracking.getTracking().setMaxSpeed(maxV);
        updateMaxSpeed(maxV);
    }

    public void addDistanceGraphSeriesPoint(DataPoint speed, DataPoint distance) {
        hasNewPoint = true;
        //        int maxDataPoints = Tracking.getTracking().getTotalPoints() + 40;
        //        log("speed point: " + speed + "; dis point: " + distance);
        //        resetGraphY2MaxValue();
        //        resetGraphXMaxValue();
        //        speedGraphSeries.appendData(speed, false, maxDataPoints);
        /*
dataPoint - values the values must be in the correct order! x-value has to be ASC. First the lowest x value and at
least the highest x value.
scrollToEnd - true => graphview will scroll to the end (maxX)
maxDataPoints - if max data count is reached, the oldest data value will be lost to avoid memory leaks         */
        //        distanceGraphSeries.appendData(distance, false, maxDataPoints);
    }

    private void log(String s) {
        System.out.println(this.getClass().getSimpleName() + "-------------------" + s);
        logT(s);
    }

    private void logT(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
