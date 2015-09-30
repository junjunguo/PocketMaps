package com.junjunguo.pocketmaps.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.model.util.Variable;
/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class SettingsActivity extends AppCompatActivity {
    private RadioGroup algoRG;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //         set status bar
        new SetStatusBarColor().setStatusBarColor(findViewById(R.id.statusBarBackgroundSettings),
                getResources().getColor(R.color.my_primary_dark), this);
        init();
    }


    /**
     * init and set
     */
    public void init() {
        algoRG = (RadioGroup) findViewById(R.id.activity_settings_routing_alg_rbtngroup);
        downloadBtn();
        alternateRoute();
        advancedSetting();
        directions();
    }

    /**
     * init and implement directions checkbox
     */
    private void directions() {
        CheckBox cb = (CheckBox) findViewById(R.id.activity_settings_directions_cb);
        cb.setChecked(Variable.getVariable().isDirectionsON());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Variable.getVariable().setDirectionsON(isChecked);
            }
        });
    }

    /**
     * set checkbox to enable or disable advanced settings
     * <p/>
     * init radio buttons
     */
    private void advancedSetting() {
        CheckBox cb = (CheckBox) findViewById(R.id.activity_settings_advanced_cb);
        cb.setChecked(Variable.getVariable().isAdvancedSetting());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Variable.getVariable().setAdvancedSetting(isChecked);
                for (int i = 0; i < algoRG.getChildCount(); i++) {
                    (algoRG.getChildAt(i)).setEnabled(isChecked);
                }
            }
        });
        //init set enable for radio buttons
        for (int i = 0; i < algoRG.getChildCount(); i++) {
            (algoRG.getChildAt(i)).setEnabled(Variable.getVariable().isAdvancedSetting());
        }
        algoRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.activity_settings_algorithm_bidijksjtra_rbtn:
                        Variable.getVariable().setRoutingAlgorithms("dijkstrabi");
                        break;
                    case R.id.activity_settings_algorithm_ontomdijksjtra_rbtn:
                        Variable.getVariable().setRoutingAlgorithms("dijkstraOneToMany");
                        break;
                    case R.id.activity_settings_algorithm_biastar_rbtn:
                        Variable.getVariable().setRoutingAlgorithms("astarbi");
                        break;
                    case R.id.activity_settings_algorithm_uniastar_rbtn:
                        Variable.getVariable().setRoutingAlgorithms("astar");
                        break;
                }
            }
        });
        //        init radio buttons:
        switch (Variable.getVariable().getRoutingAlgorithms()) {
            case "dijkstrabi":
                ((RadioButton) findViewById(R.id.activity_settings_algorithm_bidijksjtra_rbtn)).setChecked(true);
                break;
            case "dijkstraOneToMany":
                ((RadioButton) findViewById(R.id.activity_settings_algorithm_ontomdijksjtra_rbtn)).setChecked(true);
                break;
            case "astarbi":
                ((RadioButton) findViewById(R.id.activity_settings_algorithm_biastar_rbtn)).setChecked(true);
                break;
            case "astar":
                ((RadioButton) findViewById(R.id.activity_settings_algorithm_uniastar_rbtn)).setChecked(true);
                break;
        }
    }


    /**
     * init and set alternate route radio button option
     */
    private void alternateRoute() {
        RadioGroup rg = (RadioGroup) findViewById(R.id.activity_settings_weighting_rbtngroup);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.activity_settings_fastest_rbtn:
                        Variable.getVariable().setWeighting("fastest");
                        break;
                    case R.id.activity_settings_shortest_rbtn:
                        Variable.getVariable().setWeighting("shortest");
                        break;
                }
            }
        });
        RadioButton rbf, rbs;
        rbf = (RadioButton) findViewById(R.id.activity_settings_fastest_rbtn);
        rbs = (RadioButton) findViewById(R.id.activity_settings_shortest_rbtn);
        if (Variable.getVariable().getWeighting().equalsIgnoreCase("fastest")) {
            rbf.setChecked(true);
        } else {
            rbs.setChecked(true);
        }
    }


    /**
     * move view to download map
     */
    private void downloadBtn() {
        final ViewGroup cbtn = (ViewGroup) findViewById(R.id.activity_settings_download_map);
        cbtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cbtn.setBackgroundColor(getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        cbtn.setBackgroundColor(getResources().getColor(R.color.my_icons));
                        startDownloadActivity();
                        return true;
                }
                return false;
            }
        });
    }


    private void startDownloadActivity() {
        Intent intent = new Intent(this, DownloadMapActivity.class);
        startActivity(intent);
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

}
