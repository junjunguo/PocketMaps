package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.graphhopper.GraphHopper;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.map.MapHandler;

import org.mapsforge.map.layer.overlay.Marker;

import java.io.File;

public class MapViewActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
//    private MyMapView mapView;
//    private MapView mapView;
    private CustomView mapView;
    private GraphHopper hopper;
    private volatile boolean prepareInProgress = false;
    private Location mCurrentLocation;
    private Marker mPositionMarker;
    private String currentArea;
    private File mapsFolder;
    private Location mLastLocation;
    private MapHandler mapHandler;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view);

         mapView = (CustomView) findViewById(R.id.map_view);
//        mapView.getLayerManager();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map_view, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onConnected(Bundle bundle) {

    }

    @Override public void onConnectionSuspended(int i) {

    }

    @Override public void onLocationChanged(Location location) {

    }

    @Override public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
