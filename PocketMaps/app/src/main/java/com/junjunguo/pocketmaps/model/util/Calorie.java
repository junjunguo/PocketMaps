package com.junjunguo.pocketmaps.model.util;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on September 01, 2015.
 * <p>
 * <p>
 * The Harris–Benedict equations revised by Roza and Shizgal in 1984.
 * <p>
 * Men	    BMR = 88.362 + (13.397 x weight in kg) + (4.799 x height in cm) - (5.677 x age in years)
 * <p>
 * Women	BMR = 447.593 + (9.247 x weight in kg) + (3.098 x height in cm) - (4.330 x age in years)
 * <p>
 * ####       calories burned     =     weight in kg x MET x time in hours
 * <p>
 * ####       MET: http://prevention.sph.sc.edu/tools/docs/documents_compendium.pdf
 * <p>
 * ####       bicycling general           8.0
 * <p>
 * ####       running jogging, general    7.0
 * <p>
 * ####       walking walking the dog     3.0
 * <p>
 * ####
 * <p>
 * ####
 */
public class Calorie {
    /**
     * sport category, which defines the MET value
     */
    public final static double bicycling = 8.0, running = 7.0, walking = 3.0;
    /**
     * default body weight by kg if not defined by user
     */
    public final static double weightKg = 77.0;

    /**
     * weightKg = 77.0
     *
     * @param activity: bicycling, running, walking
     * @param timeHour: hours
     * @return calorie burned (activity * weightKg * timeHour)
     */
    public static double CalorieBurned(double activity, double timeHour) {
        return CalorieBurned(activity, weightKg, timeHour);
    }

    /**
     * @param activity: bicycling, running, walking
     * @param weightKg: in kg
     * @param timeHour: hours
     * @return calorie burned (activity * weightKg * timeHour)
     */
    public static double CalorieBurned(double activity, double weightKg, double timeHour) {
        return activity * weightKg * timeHour;
    }

    /**
     * use The Harris–Benedict equations revised by Roza and Shizgal in 1984. BMR
     *
     * @param activity: bicycling, running, walking
     * @param weightKg: in kg
     * @param timeHour: hours
     * @param heightCm: height in cm
     * @param age:      age in years
     * @param men:      true -> men ; false -> women
     * @return calorie burned (BMR * activity * timeHour)
     */
    public static double CalorieBurned(double activity, double weightKg, double timeHour, double heightCm, double age,
            boolean men) {
        if (men) {
            return (88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * age)) * activity * timeHour;
        }
        return (447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * age)) * activity * timeHour;
    }
}
