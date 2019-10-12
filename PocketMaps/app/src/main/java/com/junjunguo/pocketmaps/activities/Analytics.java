package com.junjunguo.pocketmaps.activities;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
import com.junjunguo.pocketmaps.model.SportCategory;
import com.junjunguo.pocketmaps.model.listeners.TrackingListener;
import com.junjunguo.pocketmaps.map.Tracking;
import com.junjunguo.pocketmaps.util.Calorie;
import com.junjunguo.pocketmaps.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.util.UnitCalculator;
import com.junjunguo.pocketmaps.fragments.SpinnerAdapter;
import com.junjunguo.pocketmaps.util.Variable;

import java.util.ArrayList;
import java.util.Locale;
/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class Analytics extends AppCompatActivity implements TrackingListener {
    // status   -----------------
    public static boolean startTimer = true;
    /**
     * a sport category spinner: to choose with type of sport which also has MET value in its adapter
     */
    private Spinner spinner;
    private TextView durationTV, avgSpeedTV, maxSpeedTV, distanceTV, distanceUnitTV, caloriesTV, maxSpeedUnitTV, avgSpeedUnitTV;
    // duration
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
        initSpinner();
        initStatus();
        durationHandler = new Handler();
        calorieUpdateHandler = new Handler();
        // graph
        initGraph();
    }

    // ----------  status ---------------
    private void initSpinner() {
        spinner = (Spinner) findViewById(R.id.activity_analytics_spinner);

        ArrayList<SportCategory> spinnerList = new ArrayList<>();
        spinnerList.add(new SportCategory("walk/run", R.drawable.ic_directions_run_white_24dp, Calorie.Type.Run));
        spinnerList.add(new SportCategory("bike", R.drawable.ic_directions_bike_white_24dp, Calorie.Type.Bike));
        spinnerList.add(new SportCategory("car", R.drawable.ic_directions_car_white_24dp, Calorie.Type.Car));

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
        distanceTV = (TextView) findViewById(R.id.activity_analytics_distance);
        distanceUnitTV = (TextView) findViewById(R.id.activity_analytics_distance_unit);
        caloriesTV = (TextView) findViewById(R.id.activity_analytics_calories);
        maxSpeedTV = (TextView) findViewById(R.id.activity_analytics_max_speed);
        durationTV = (TextView) findViewById(R.id.activity_analytics_duration);
        avgSpeedTV = (TextView) findViewById(R.id.activity_analytics_avg_speed);
        maxSpeedUnitTV = (TextView) findViewById(R.id.activity_analytics_max_speed_unit);
        avgSpeedUnitTV = (TextView) findViewById(R.id.activity_analytics_avg_speed_unit);
        updateDistance(getTracking().getDistance());
        updateAvgSpeed(getTracking().getAvgSpeed());
        updateMaxSpeed(getTracking().getMaxSpeed());
        updateCalorieBurned();
    }
    
    Tracking getTracking()
    {
      return Tracking.getTracking(getApplicationContext());
    }

    /**
     * update avg speedGraphSeries
     *
     * @param avgSpeed in km/h
     */
    @Override
    public void updateAvgSpeed(Double avgSpeed) {
        avgSpeedTV.setText(UnitCalculator.getBigDistance(avgSpeed * 1000.0, 2));
    }

    /**
     * update max speedGraphSeries
     *
     * @param maxSpeed in km/h
     */
    @Override
    public void updateMaxSpeed(Double maxSpeed) {
        maxSpeedTV.setText(UnitCalculator.getBigDistance(maxSpeed * 1000.0, 2));
    }

    private void updateCalorieBurned() {
        long endTime = System.currentTimeMillis();
        if (!startTimer) { endTime = getTracking().getTimeEnd(); }
        double speedKmh = getTracking().getAvgSpeed();
        double met = Calorie.getMET(speedKmh, getSportCategory());
        double cals = Calorie.calorieBurned(met, getTracking().getDurationInHours(endTime));
        caloriesTV.setText(String.format(Locale.getDefault(), "%.2f", cals));
    }
    
    private void updateTimeSpent()
    {
      long endTime = System.currentTimeMillis();
      if (!startTimer) { endTime = getTracking().getTimeEnd(); }
      long updatedTime = endTime - getTracking().getTimeStart();
      int secs = (int) (updatedTime / 1000);
      int mins = secs / 60;
      secs = secs % 60;
      int hours = mins / 60;
      mins = mins % 60;
      durationTV.setText("" + String.format(Locale.getDefault(), "%02d", hours) + ":" + String.format(Locale.getDefault(), "%02d", mins) + ":" +
              String.format(Locale.getDefault(), "%02d", secs));
    }

    /**
     * get activity type (MET) from selected spinner (object)
     */
    private Calorie.Type getSportCategory() {
        return ((SportCategory) spinner.getSelectedItem()).getSportMET();
    }

    /**
     * update distanceGraphSeries
     *
     * @param distance in meter.
     */
    @Override
    public void updateDistance(Double distance) {
      
        if (distance < UnitCalculator.getMultValue()) {
            distanceTV.setText(UnitCalculator.getShortDistance(distance));
            distanceUnitTV.setText(UnitCalculator.getUnit(false));
        } else {
            distanceTV.setText(UnitCalculator.getBigDistance(distance, 2));
            distanceUnitTV.setText(UnitCalculator.getUnit(true));
        }
    }

    /**
     * new thread to update timer
     */
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            updateTimeSpent();
            
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
                getTracking().requestDistanceGraphSeries();
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
        if (Variable.getVariable().isImperalUnit())
        {
          speedGraphSeries.setTitle("Speed mi/h");
          distanceGraphSeries.setTitle("Distance mi");
        }
        else
        {
          speedGraphSeries.setTitle("Speed km/h");
          distanceGraphSeries.setTitle("Distance km");
        }
        speedGraphSeries.setColor(0xFF009688);
        distanceGraphSeries.setColor(0xFFFF5722);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    /**
     * auto setup max value for graph first y scale
     */
    public void resetGraphY1MaxValue() {
        double maxSpeed = getTracking().getMaxSpeed();
        maxSpeed = UnitCalculator.getBigDistanceValue(maxSpeed);
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
        double dis = getTracking().getDistanceKm();
        dis = UnitCalculator.getBigDistanceValue(dis);
        if (dis > maxY2axis * 0.9) {
            maxY2axis = getMaxValue(dis, maxY2axis);
        }
        //        log("max Y: " + maxY2axis);
        graph.getSecondScale().setMaxY(maxY2axis);
    }

    /**
     * @param dis
     * @param max
     * @return max * 2 until max > dis * 1.1
     */
    private double getMaxValue(double dis, double max) {
        if (max > dis * 1.1) {
            return max;
        }
        return getMaxValue(dis, max * 2);
    }

    public void resetGraphXMaxValue() {
        //        double max = 0.1;
        double time = getTracking().getDurationInHours();
        if (!startTimer)
        {
          long end = getTracking().getTimeEnd();
          time = getTracking().getDurationInHours(end);
        }
        if (time > maxXaxis * 0.9) {
            maxXaxis = getMaxValue(time, maxXaxis);
        }
//        log("max X: " + maxXaxis + "; time: " + time);
        //        graph.getViewport().setXAxisBoundsManual(true);
        //        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(maxXaxis);
    }

    @Override public void onResume() {
        super.onResume();
        if (startTimer)
        {
          durationHandler.postDelayed(updateTimerThread, 500);
          calorieUpdateHandler.postDelayed(updateCalorieThread, 60000);
        }
        distanceUnitTV.setText(UnitCalculator.getUnit(false));
        maxSpeedUnitTV.setText(UnitCalculator.getUnit(true) + "/h");
        avgSpeedUnitTV.setText(UnitCalculator.getUnit(true) + "/h");
        getTracking().addListener(this);
        //        graph
        getTracking().requestDistanceGraphSeries();
    }

    @Override public void onPause() {
        super.onPause();
        durationHandler.removeCallbacks(updateTimerThread);
        calorieUpdateHandler.removeCallbacks(updateCalorieThread);
        getTracking().removeListener(this);
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

    /**
     * updated when {@link Tracking#requestDistanceGraphSeries()} is called
     *
     * @param dataPoints
     */
    public void updateDistanceGraphSeries(final DataPoint[][] dataPoints) {
        resetGraphY1MaxValue();
        resetGraphY2MaxValue();
        //        resetGraphXMaxValue();
        try
        {
          speedGraphSeries.resetData(dataPoints[0]);
          distanceGraphSeries.resetData(dataPoints[1]);
        }
        catch (IllegalArgumentException e)
        {
          e.printStackTrace();
        }
        double maxV = speedGraphSeries.getHighestValueY();
        if (Variable.getVariable().isImperalUnit())
        { // From miles convert back to km!
          double factor = UnitCalculator.METERS_OF_MILE / 1000.0;
          maxV = maxV * factor;
        }
        getTracking().setMaxSpeed(maxV);
        updateMaxSpeed(maxV);
        if(!startTimer)
        {
          updateTimeSpent();
          updateCalorieBurned();
          updateAvgSpeed(getTracking().getAvgSpeed());
          resetGraphXMaxValue();
        }
    }
    
    public void setUpdateNewPoint()
    {
      hasNewPoint = true;
    }
}
