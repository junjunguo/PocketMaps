package com.junjunguo.pocketmaps.util;

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
    /*
     * sport category, which defines the MET value
     */
  
    /** Slow with bike 10mph or 16kmh **/
    public final static double BIKE_SLOW = 4.0;
    /** Normal with bike 13mph or 20kmh **/
    public final static double BIKE_MID = 8.0;
    /** Fast with bike 15mph or 24kmh **/
    public final static double BIKE_FAST = 10.0;
    /** Slow walk 3mph or 5kmh **/
    public final static double WALK_SLOW = 3.0;
    /** Slow running 5mph or 8kmh **/
    public final static double RUN_SLOW = 8.0;
    /** Normal running 8mph or 12kmh **/
    public final static double RUN_MID = 13.5;
    /** Fast running 10mph or 16kmh **/
    public final static double RUN_FAST = 16.0;
    /** General driving with car **/
    public final static double CAR_DRIVE = 2.0;
    
    public enum Type {Bike, Car, Run};
    
    /**
     * default body weight by kg if not defined by user
     */
    public final static double weightKg = 77.0;

    public static double getMET(double speedKmh, Type type)
    {
      if (type == Type.Run)
      {
        if (speedKmh<6.5) { return WALK_SLOW; }
        if (speedKmh<10) { return RUN_SLOW; }
        if (speedKmh<14) { return RUN_MID; }
        return RUN_FAST;
      }
      else if (type == Type.Bike)
      {
        if (speedKmh<18) { return BIKE_SLOW; }
        if (speedKmh<22) { return BIKE_MID; }
        return BIKE_FAST;
      }
      return CAR_DRIVE;
    }
    
    
    /**
     * weightKg = 77.0
     *
     * @param activity: bicycling, running, walking
     * @param timeHour: hours
     * @return calorie burned (activity * weightKg * timeHour)
     */
    public static double calorieBurned(double activity, double timeHour) {
        return calorieBurned(activity, weightKg, timeHour);
    }

    /**
     * @param activity: bicycling, running, walking
     * @param weightKg: in kg
     * @param timeHour: hours
     * @return calorie burned (activity * weightKg * timeHour)
     */
    public static double calorieBurned(double activity, double weightKg, double timeHour) {
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
    public static double calorieBurned(double activity, double weightKg, double timeHour, double heightCm, double age,
            boolean men) {
        if (men) {
            return (88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * age)) * activity * timeHour;
        }
        return (447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * age)) * activity * timeHour;
    }
}
