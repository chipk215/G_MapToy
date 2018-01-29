package com.keyes_west.mapsgetstarted;



import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;



public class MapsActivity extends AppCompatActivity
        implements  OnMapReadyCallback,
                    GoogleMap.OnMarkerDragListener,
                    StreetViewPanorama.OnStreetViewPanoramaChangeListener,
                    StreetViewPanorama.OnStreetViewPanoramaCameraChangeListener,
                    GoogleMap.OnMyLocationButtonClickListener,
                    GoogleMap.OnMyLocationClickListener,
                    ActivityCompat.OnRequestPermissionsResultCallback{


    // Array holds references to the direction pointing icon files.
    // Each file covers 22.5 degrees
    private static final int[] ICON_FILENAMES = {
            R.drawable.man_0,  R.drawable.man_1,  R.drawable.man_2,  R.drawable.man_3,
            R.drawable.man_4,  R.drawable.man_5,  R.drawable.man_6,  R.drawable.man_7,
            R.drawable.man_8,  R.drawable.man_9,  R.drawable.man_10,  R.drawable.man_11,
            R.drawable.man_12,  R.drawable.man_13,  R.drawable.man_14,  R.drawable.man_15
    };

    private static final String TAG= "MapsActivity";

    private static final String MARKER_POSITION_KEY = "MarkerPosition";

    private static final LatLng BOISE = new LatLng(43.615032, -116.202335);

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private StreetViewPanorama mStreetViewPanorama;

    private Marker mMarker;

    private Projection mProjection;

    private GoogleMap mMap;

    private ViewGroup mTransitionContainer;

    private SupportStreetViewPanoramaFragment mStreetViewPanoramaFragment;

    private LatLng mPegmanPosition;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.split_street_view_panorama_and_map_demo);


        mTransitionContainer= (ViewGroup)findViewById(android.R.id.content)
                .findViewById(R.id.map_container);


        if (savedInstanceState == null) {
            mPegmanPosition = BOISE;
        } else {
            mPegmanPosition = savedInstanceState.getParcelable(MARKER_POSITION_KEY);
        }

        mStreetViewPanoramaFragment = (SupportStreetViewPanoramaFragment)
                getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);


        // another way
        getSupportFragmentManager().beginTransaction().hide(mStreetViewPanoramaFragment).commit();

        mStreetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        mStreetViewPanorama = panorama;
                        mStreetViewPanorama.setOnStreetViewPanoramaChangeListener(
                                MapsActivity.this);
                        mStreetViewPanorama.setOnStreetViewPanoramaCameraChangeListener(MapsActivity.this);
                        // Only need to set the position once as the streetview fragment will maintain
                        // its state.
                        if (savedInstanceState == null) {
                            mStreetViewPanorama.setPosition(BOISE);
                        }
                    }
                });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


        mapFragment.getMapAsync(this);


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.i(TAG, "xdpi: " + metrics.xdpi + " ydpi: " + metrics.ydpi );


    }


    @Override
    public void onMapReady(GoogleMap map) {
        Log.i(TAG, "onMapReady Invoked");

        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();


        mProjection = map.getProjection();

        map.setOnMarkerDragListener(MapsActivity.this);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i(TAG,"Marker clicked");
                if (marker.equals(mMarker)) {
                    float currentRotation = mMarker.getRotation();
                    Log.i(TAG,"Marker rotation= " + currentRotation);
                    // mMarker.setRotation(currentRotation + 15.0f);
                }
                return true;
            }
        });


        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener(){
            @Override
            public void onCameraIdle(){
                float zoom = mMap.getCameraPosition().zoom;
                Log.i(TAG,"Min Zoom= " + mMap.getMinZoomLevel()  + "  Max Zoom= " + mMap.getMaxZoomLevel());
                Log.i(TAG, "Camera Zoom level: " + zoom);

                TransitionSet set = new TransitionSet()
                        .addTransition(new Slide(Gravity.LEFT));

                TransitionManager.beginDelayedTransition(mTransitionContainer,set);
                if (zoom < 15){
                    // hide street view
                    getSupportFragmentManager().beginTransaction().hide(mStreetViewPanoramaFragment).commit();
                }else  {
                    // show street view
                    getSupportFragmentManager().beginTransaction().show(mStreetViewPanoramaFragment).commit();
                }

            }
        });
        // Creates a draggable marker. Long press to drag.
        mMarker = map.addMarker(new MarkerOptions()
                .position(mPegmanPosition)
                .icon(BitmapDescriptorFactory
                        .fromResource(ICON_FILENAMES[computeBearingIndex(map.getCameraPosition().bearing)]))
                .anchor(.5f,.5f)
                .draggable(true)
                .flat(true)
        );


        map.getUiSettings().setCompassEnabled(true);
        // map.setPadding(30,30,30,30);


    }


    /**
     * Invoked between onPause() and onStop() - provides opportunity for activity to save activity
     * state information not contained in a view
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MARKER_POSITION_KEY, mMarker.getPosition());

        // invoke the super class last with the updated Bundle
        super.onSaveInstanceState(outState);
    }

    /**
     * Called when a pegman starts being dragged
     * @param marker
     */
    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.i(TAG, "Pegman started");
    }


    /**
     * Called repeatedly while pegman is being dragged
     * @param marker
     */
    @Override
    public void onMarkerDrag(Marker marker) {

    }

    /**
     * Called when pegman has finished being dragged
     * @param marker
     */
    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.i(TAG, "Pegman stopped");
        // update the streetview panorama to correlate with pegman
        mStreetViewPanorama.setPosition(marker.getPosition(), 150);

    }

    @Override
    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation location) {
        Log.i(TAG, "SVP Changed");
        if (location != null) {
            mMarker.setPosition(location.position);


            float svpCameraBearing = mStreetViewPanorama.getPanoramaCamera().bearing;
            Log.i(TAG, "Camera bearing: " + svpCameraBearing);
            //mMarker.setRotation(addMarkerIconOffset(svpCameraBearing));
            mMarker.setIcon(BitmapDescriptorFactory
                    .fromResource(ICON_FILENAMES[computeBearingIndex(svpCameraBearing)]));


        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }



    @Override
    public void onStreetViewPanoramaCameraChange(StreetViewPanoramaCamera streetViewPanoramaCamera) {
        Log.i(TAG, "SVPCameraChange - camera angle= " + streetViewPanoramaCamera.bearing);
        float svpCameraBearing = streetViewPanoramaCamera.bearing;

        mMarker.setIcon(BitmapDescriptorFactory
                .fromResource(ICON_FILENAMES[computeBearingIndex(svpCameraBearing)]));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }


    /**
     * Map the camera bearing angle to a pegman image file so that the
     * pointer in the pegman file correlates to the camera angle.
     * @param theta
     * @return
     */
    public static int computeBearingIndex(float theta){

        // 0<= theta <= 360
        if (theta < 0){
            theta += 360.0;
        }

        //subtract 11.25 degree offset
        theta = theta - 11.25f;
        if (theta < 0){
            theta = 0;
        }

        int index = (int)Math.ceil(theta/22.5d) % 16;

        return index;
    }


    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }


    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    private void rotatePanoramaCamera(float angle, int milliSecondsDuration){


        StreetViewPanoramaCamera camera = new StreetViewPanoramaCamera.Builder()
                .zoom(mStreetViewPanorama.getPanoramaCamera().zoom)
                .tilt(mStreetViewPanorama.getPanoramaCamera().tilt)
                .bearing(mStreetViewPanorama.getPanoramaCamera().bearing - angle)
                .build();

        mStreetViewPanorama.animateTo(camera, milliSecondsDuration);
    }
}
