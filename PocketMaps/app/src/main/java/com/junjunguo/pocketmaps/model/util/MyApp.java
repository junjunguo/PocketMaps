package com.junjunguo.pocketmaps.model.util;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 06, 2015.
 */

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Custom implementation of android.app.Application.&nbsp;The android:name attribute in the AndroidManifest.xml
 * application element should be the name of your class (".MyApp"). Android will always create an instance of the
 * application class and call onCreate before creating any other Activity, Service or BroadcastReceiver.
 */
public class MyApp extends Application {
    /**
     * The Analytics singleton. The field is set in onCreate method override when the application class is initially
     * created.
     */
    private static GoogleAnalytics analytics;

    /**
     * The default app tracker. The field is from onCreate callback when the application is initially created.
     */
    private static Tracker tracker;

    /**
     * Access to the global Analytics singleton. If this method returns null you forgot to either set
     * android:name="&lt;this.class.name&gt;" attribute on your application element in AndroidManifest.xml or you are
     * not setting this.analytics field in onCreate method override.
     */
    public static GoogleAnalytics analytics() {
        return analytics;
    }

    /**
     * The default app tracker. If this method returns null you forgot to either set
     * android:name="&lt;this.class.name&gt;" attribute on your application element in AndroidManifest.xml or you are
     * not setting this.tracker field in onCreate method override.
     */
    public static Tracker tracker() {
        return tracker;
    }

    @Override public void onCreate() {
        super.onCreate();
        analytics = GoogleAnalytics.getInstance(this);

        analytics.setLocalDispatchPeriod(3600);
//        tracker.setSampleRate(100.0d);
        tracker = analytics.newTracker("UA-64797294-1");

        // Provide unhandled exceptions reports. Do that first after creating the tracker
        tracker.enableExceptionReporting(true);

        // Enable Remarketing, Demographics & Interests reports
        // https://developers.google.com/analytics/devguides/collection/android/display-features
        tracker.enableAdvertisingIdCollection(true);

        // Enable automatic activity tracking for your app
        tracker.enableAutoActivityTracking(true);

        tracker.send(new HitBuilders.ScreenViewBuilder().setCustomDimension(1, null).build());
    }
}