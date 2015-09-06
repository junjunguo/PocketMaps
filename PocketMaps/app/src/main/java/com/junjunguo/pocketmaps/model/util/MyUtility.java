package com.junjunguo.pocketmaps.model.util;

import org.mapsforge.core.model.LatLong;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on July 09, 2015.
 */
public class MyUtility {
//    public static ArrayList<MyMap> insert(ArrayList<MyMap> list, MyMap myMap) {
    //
    //        for (int i = 0; i < list.size(); i++) {
    //            if(list.get(i).compareTo(myMap)>0){
    //                Collections.sort();
    //            }
    //        }
    //    }

    /**
     * @param s (latitude, longitude or latitude longitude; can be degree or digital coordinates)
     * @return null if there is an error
     */
    public static LatLong getLatLong(String s) {
        LatLong latlong = null;
        if (s.contains("N") || s.contains("S") || s.contains("n") || s.contains("s")) {
            return convertCoordingate(s);
        } else {
            String[] d = new String[2];
            try {
                if (s.contains(",")) {
                    d = s.split(",");
                } else {
                    d = s.split(" ");
                }
                return new LatLong(Double.parseDouble(d[0]), Double.parseDouble(d[1]));
            } catch (Exception e) {e.getStackTrace();}
        }

        return latlong;
    }

    /**
     * with or with out , in middle
     * <p>
     * <li>latitude, longitude</li> <li>latitude longitude</li>
     *
     * @param degrees
     * @return null if there is an error
     */
    public static LatLong convertCoordingate(String degrees) {
        String[] lalo = new String[2];
        if (degrees.contains(",")) {
            lalo = degrees.split(",");
        } else {
            int n = degrees.indexOf("N");
            int ns = degrees.indexOf("n");
            int s = degrees.indexOf("S");
            int ss = degrees.indexOf("s");
            if (n > 0) {
                lalo[0] = degrees.substring(0, n + 1);
                lalo[1] = degrees.substring(n + 1);
            } else if (ns > 0) {
                lalo[0] = degrees.substring(0, ns + 1);
                lalo[1] = degrees.substring(ns + 1);
            } else if (s > 0) {
                lalo[0] = degrees.substring(0, s + 1);
                lalo[1] = degrees.substring(s + 1);
            } else if (ss > 0) {
                lalo[0] = degrees.substring(0, ss + 1);
                lalo[1] = degrees.substring(ss + 1);
            }
        }
        double latitude = toDecimal(lalo[0]);
        double logitude = toDecimal(lalo[1]);
        System.out.println("La: " + latitude + "\nLo: " + logitude);
        if (String.valueOf(latitude).length() == 1 || String.valueOf(logitude).length() == 1) {
            return null;
        }
        return new LatLong(latitude, logitude);
    }

    /**
     * convert a degree value of coordinate to decimal format
     * <p>
     * input can be: <li>40° 26′ 46″ N</li> <li>40° 26.767′ N</li> <li>40.446° N</li>
     *
     * @param d
     * @return decimal value; 0 if there is an error
     */
    public static double toDecimal(String d) {
        double decimal = 0;
        try {
            int degree = d.indexOf("°");
            if (degree > 0) {
                decimal += Double.parseDouble(d.substring(0, degree));
            }
            int minute = d.indexOf("′");
            if (minute > 0) {
                decimal += Double.parseDouble(d.substring(degree + 1, minute)) / 60.0;
            }
            int second = d.indexOf("″");
            if (second > 0) {
                decimal += Double.parseDouble(d.substring(minute + 1, second)) / 3600.0;
            }
            if (d.contains("S") || d.contains("s") || d.contains("W") || d.contains("w")) {
                decimal = Math.abs(decimal) * -1;
            }
            decimal = (double) Math.round(decimal * 100000) / 100000;
        } catch (Exception e) {e.printStackTrace();}
        return decimal;
    }
}
