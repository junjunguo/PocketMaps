package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.util.Variable;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 01, 2015.
 */
public class AppSettings {
    private static AppSettings appSettings;
    private Activity activity;
    private RadioGroup algoRG;
    private ViewGroup appSettingsVP;

    public static AppSettings getAppSettings() {
        if (appSettings == null) {
            appSettings = new AppSettings();
        }
        return appSettings;
    }

    private AppSettings() {
    }

    /**
     * init and set
     *
     * @param activity
     * @param calledFromVP
     */
    public void set(Activity activity, final ViewGroup calledFromVP) {
        this.activity = activity;
        appSettingsVP = (ViewGroup) activity.findViewById(R.id.app_settings_layout);
        clearBtn(appSettingsVP, calledFromVP);
        algoRG = (RadioGroup) activity.findViewById(R.id.app_settings_routing_alg_rbtngroup);
        chooseBtn(appSettingsVP);
        alternateRoute();
        advancedSetting();
        appSettingsVP.setVisibility(View.VISIBLE);
        calledFromVP.setVisibility(View.INVISIBLE);
        directions();
    }

    public ViewGroup getAppSettingsVP() {
        return appSettingsVP;
    }

    /**
     * init and implement directions checkbox
     */
    private void directions() {
        CheckBox cb = (CheckBox) activity.findViewById(R.id.app_settings_directions_cb);
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
        CheckBox cb = (CheckBox) activity.findViewById(R.id.app_settings_advanced_cb);
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
                    case R.id.app_settings_algorithm_bidijksjtra_rbtn:
                        Variable.getVariable().setRoutingAlgorithms("dijkstrabi");
                        break;
                    case R.id.app_settings_algorithm_ontomdijksjtra_rbtn:
                        Variable.getVariable().setRoutingAlgorithms("dijkstraOneToMany");
                        break;
                    case R.id.app_settings_algorithm_biastar_rbtn:
                        Variable.getVariable().setRoutingAlgorithms("astarbi");
                        break;
                    case R.id.app_settings_algorithm_uniastar_rbtn:
                        Variable.getVariable().setRoutingAlgorithms("astar");
                        break;
                }
            }
        });
        //        init radio buttons:
        switch (Variable.getVariable().getRoutingAlgorithms()) {
            case "dijkstrabi":
                ((RadioButton) activity.findViewById(R.id.app_settings_algorithm_bidijksjtra_rbtn)).setChecked(true);
                break;
            case "dijkstraOneToMany":
                ((RadioButton) activity.findViewById(R.id.app_settings_algorithm_ontomdijksjtra_rbtn)).setChecked(true);
                break;
            case "astarbi":
                ((RadioButton) activity.findViewById(R.id.app_settings_algorithm_biastar_rbtn)).setChecked(true);
                break;
            case "astar":
                ((RadioButton) activity.findViewById(R.id.app_settings_algorithm_uniastar_rbtn)).setChecked(true);
                break;
        }
    }


    /**
     * init and set alternate route radio button option
     */
    private void alternateRoute() {
        RadioGroup rg = (RadioGroup) activity.findViewById(R.id.app_settings_weighting_rbtngroup);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.app_settings_fastest_rbtn:
                        Variable.getVariable().setWeighting("fastest");
                        break;
                    case R.id.app_settings_shortest_rbtn:
                        Variable.getVariable().setWeighting("shortest");
                        break;
                }
            }
        });
        RadioButton rbf, rbs;
        rbf = (RadioButton) activity.findViewById(R.id.app_settings_fastest_rbtn);
        rbs = (RadioButton) activity.findViewById(R.id.app_settings_shortest_rbtn);
        if (Variable.getVariable().getWeighting().equalsIgnoreCase("fastest")) {
            rbf.setChecked(true);
        } else {
            rbs.setChecked(true);
        }
    }

    /**
     * move to select and load map view
     *
     * @param appSettingsVP
     */
    private void chooseBtn(final ViewGroup appSettingsVP) {
        final ViewGroup cbtn = (ViewGroup) activity.findViewById(R.id.app_settings_select_map);
        cbtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cbtn.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        cbtn.setBackgroundColor(activity.getResources().getColor(R.color.my_icons));
                        //                        Variable.getVariable().setAutoLoad(false); // close auto load from
                        // main activity
                        startMainActivity();
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * init clear btn
     */
    private void clearBtn(final ViewGroup appSettingsVP, final ViewGroup calledFromVP) {
        ImageButton appsettingsClearBtn = (ImageButton) activity.findViewById(R.id.app_settings_clear_btn);
        appsettingsClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                appSettingsVP.setVisibility(View.INVISIBLE);
                calledFromVP.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * move to main activity
     */
    private void startMainActivity() {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("SELECTNEWMAP", true);
        activity.startActivity(intent);
        activity.finish();
    }
}
