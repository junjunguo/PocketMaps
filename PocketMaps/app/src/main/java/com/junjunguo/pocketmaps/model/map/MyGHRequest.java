package com.junjunguo.pocketmaps.model.map;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on June 19, 2015.
 */

import com.graphhopper.GHRequest;
import com.graphhopper.routing.util.WeightingMap;
import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.GHPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * GraphHopper request wrapper to simplify requesting GraphHopper.
 * <p>
 *
 * @author Peter Karich
 * @author ratrun
 */
public class MyGHRequest extends GHRequest {
    private String algo = "";
    private final List<GHPoint> points;
    private final WeightingMap hints = new WeightingMap();
    private String vehicle = "";
    private boolean possibleToAdd = false;
    private Locale locale = Locale.US;

    // List of favored start (1st element) and arrival heading (all other).
    // Headings are north based azimuth (clockwise) in (0, 360) or NaN for equal preference
    private final List<Double> favoredHeadings;

    public MyGHRequest() {
        this(5);
    }

    public MyGHRequest(int size) {
        points = new ArrayList<GHPoint>(size);
        favoredHeadings = new ArrayList<Double>(size);
        possibleToAdd = true;
    }

    /**
     * Set routing request from specified startPlace (fromLat, fromLon) to endPlace (toLat, toLon) with a preferred
     * start and end heading. Headings are north based azimuth (clockwise) in (0, 360) or NaN for equal preference.
     */
    public MyGHRequest(double fromLat, double fromLon, double toLat, double toLon, double startHeading,
            double endHeading) {
        this(new GHPoint(fromLat, fromLon), new GHPoint(toLat, toLon), startHeading, endHeading);
    }

    /**
     * Set routing request from specified startPlace (fromLat, fromLon) to endPlace (toLat, toLon)
     */
    public MyGHRequest(double fromLat, double fromLon, double toLat, double toLon) {
        this(new GHPoint(fromLat, fromLon), new GHPoint(toLat, toLon));
    }


    /**
     * Set routing request from specified startPlace to endPlace with a preferred start and end heading. Headings are
     * north based azimuth (clockwise) in (0, 360) or NaN for equal preference
     */
    public MyGHRequest(GHPoint startPlace, GHPoint endPlace, double startHeading, double endHeading) {
        if (startPlace == null) throw new IllegalStateException("'from' cannot be null");

        if (endPlace == null) throw new IllegalStateException("'to' cannot be null");

        points = new ArrayList<GHPoint>(2);
        points.add(startPlace);
        points.add(endPlace);

        favoredHeadings = new ArrayList<Double>(2);
        validateAzimuthValue(startHeading);
        favoredHeadings.add(startHeading);
        validateAzimuthValue(endHeading);
        favoredHeadings.add(endHeading);
    }

    public MyGHRequest(GHPoint startPlace, GHPoint endPlace) {
        this(startPlace, endPlace, Double.NaN, Double.NaN);
    }


    /**
     * Set routing request
     *
     * @param points          List of stopover points in order: start, 1st stop, 2nd stop, ..., end
     * @param favoredHeadings List of favored headings for starting (start point) and arrival (via and end points)
     *                        Headings are north based azimuth (clockwise) in (0, 360) or NaN for equal preference
     */
    public MyGHRequest(List<GHPoint> points, List<Double> favoredHeadings) {
        if (points.size() != favoredHeadings.size())
            throw new IllegalArgumentException("Size of headings (" + favoredHeadings.size() +
                    ") must match size of points (" + points.size() + ")");

        for (Double heading : favoredHeadings) {
            validateAzimuthValue(heading);
        }
        this.points = points;
        this.favoredHeadings = favoredHeadings;
    }

    /**
     * Set routing request
     *
     * @param points List of stopover points in order: start, 1st stop, 2nd stop, ..., end
     */
    public MyGHRequest(List<GHPoint> points) {
        this(points, Collections.nCopies(points.size(), Double.NaN));
    }

    /**
     * Add stopover point to routing request.
     *
     * @param point          geographical position (see GHPoint)
     * @param favoredHeading north based azimuth (clockwise) in (0, 360) or NaN for equal preference
     */
    public MyGHRequest addPoint(GHPoint point, Double favoredHeading) {
        if (point == null) throw new IllegalArgumentException("point cannot be null");


        if (!possibleToAdd) throw new IllegalStateException(
                "Please call empty constructor if you intent to use " + "more than two places via addPlace method.");

        points.add(point);
        validateAzimuthValue(favoredHeading);
        favoredHeadings.add(favoredHeading);
        return this;
    }

    /**
     * Add stopover point to routing request.
     *
     * @param point geographical position (see GHPoint)
     */
    public MyGHRequest addPoint(GHPoint point) {
        addPoint(point, Double.NaN);
        return this;
    }

    /**
     * @return north based azimuth (clockwise) in (0, 360) or NaN for equal preference
     */
    public double getFavoredHeading(int i) {
        return favoredHeadings.get(i);
    }

    /**
     * @return if there exist a preferred heading for start/via/end point i
     */
    public boolean hasFavoredHeading(int i) {
        if (i >= favoredHeadings.size()) throw new IndexOutOfBoundsException(
                "Index: " + i + " too large for list of size " + favoredHeadings.size());

        return !Double.isNaN(favoredHeadings.get(i));
    }

    // validate Azimuth entry
    private void validateAzimuthValue(Double heading) {
        // heading must be in (0, 360) oder Nan
        if (!Double.isNaN(heading) && ((Double.compare(heading, 360) > 0) || (Double.compare(heading, 0) < 0))) {
            throw new IllegalArgumentException("Heading " + heading + " must be in range (0,360) or NaN");
        }
    }

    public List<GHPoint> getPoints() {
        return points;
    }

    /**
     * For possible values see AlgorithmOptions.*
     */
    public MyGHRequest setAlgorithm(String algo) {
        if (algo != null) this.algo = algo;
        return this;
    }

    public String getAlgorithm() {
        return algo;
    }

    public Locale getLocale() {
        return locale;
    }

    public MyGHRequest setLocale(Locale locale) {
        if (locale != null) this.locale = locale;
        return this;
    }

    public MyGHRequest setLocale(String localeStr) {
        return setLocale(Helper.getLocale(localeStr));
    }

    /**
     * By default it supports fastest and shortest. Or specify empty to use default.
     */
    public MyGHRequest setWeighting(String w) {
        hints.setWeighting(w);
        return this;
    }

    public String getWeighting() {
        return hints.getWeighting();
    }

    /**
     * Specifiy car, bike or foot. Or specify empty to use default.
     */
    public MyGHRequest setVehicle(String vehicle) {
        if (vehicle != null) this.vehicle = vehicle;
        return this;
    }

    public String getVehicle() {
        return vehicle;
    }

    @Override public String toString() {
        String res = "";
        for (GHPoint point : points) {
            if (res.isEmpty()) {
                res = point.toString();
            } else {
                res += "; " + point.toString();
            }
        }
        return res + "(" + algo + ")";
    }

    public WeightingMap getHints() {
        return hints;
    }
}