package com.keyes_west.mapsgetstarted;



import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;


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
        implements GoogleMap.OnMarkerDragListener,
                    StreetViewPanorama.OnStreetViewPanoramaChangeListener,
                    StreetViewPanorama.OnStreetViewPanoramaCameraChangeListener {


    private static final int[] ICON_FILENAMES = {
            R.drawable.man_0,  R.drawable.man_1,  R.drawable.man_2,  R.drawable.man_3,
            R.drawable.man_4,  R.drawable.man_5,  R.drawable.man_6,  R.drawable.man_7,
            R.drawable.man_8,  R.drawable.man_9,  R.drawable.man_10,  R.drawable.man_11,
            R.drawable.man_12,  R.drawable.man_13,  R.drawable.man_14,  R.drawable.man_15
    };

    private static final String TAG= "MapsActivity";

    private static final String MARKER_POSITION_KEY = "MarkerPosition";

    private static final LatLng BOISE = new LatLng(43.615032, -116.202335);

    private StreetViewPanorama mStreetViewPanorama;

    private Marker mMarker;

    private Projection mProjection;

    private GoogleMap mMap;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.split_street_view_panorama_and_map_demo);


        final ViewGroup transitionContainer = (ViewGroup)findViewById(android.R.id.content)
                .findViewById(R.id.map_container);


        final LatLng markerPosition;
        if (savedInstanceState == null) {
            markerPosition = BOISE;
        } else {
            markerPosition = savedInstanceState.getParcelable(MARKER_POSITION_KEY);
        }

        final SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment)
                        getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);


        // this hides the fragment
        //streetViewPanoramaFragment.getView().setVisibility(View.GONE);

        // another way
        getSupportFragmentManager().beginTransaction().hide(streetViewPanoramaFragment).commit();

        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
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
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                Log.i(TAG, "onMapReady Invoked");
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

                        TransitionManager.beginDelayedTransition(transitionContainer,set);
                        if (zoom < 15){
                            // hide street view
                            getSupportFragmentManager().beginTransaction().hide(streetViewPanoramaFragment).commit();
                        }else  {
                            // show street view
                            getSupportFragmentManager().beginTransaction().show(streetViewPanoramaFragment).commit();
                        }

                    }
                });
                // Creates a draggable marker. Long press to drag.
                mMarker = map.addMarker(new MarkerOptions()
                        .position(markerPosition)
                        .icon(BitmapDescriptorFactory
                                .fromResource(ICON_FILENAMES[computeBearingIndex(map.getCameraPosition().bearing)]))
                        .anchor(.5f,.5f)
                        .draggable(true)
                        .flat(true)
                );



                map.getUiSettings().setCompassEnabled(true);
               // map.setPadding(30,30,30,30);

                mMap = map;
                mProjection = map.getProjection();
            }
        });


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.i(TAG, "xdpi: " + metrics.xdpi + " ydpi: " + metrics.ydpi );


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



    private void rotatePanoramaCamera(float angle, int milliSecondsDuration){


        StreetViewPanoramaCamera camera = new StreetViewPanoramaCamera.Builder()
                .zoom(mStreetViewPanorama.getPanoramaCamera().zoom)
                .tilt(mStreetViewPanorama.getPanoramaCamera().tilt)
                .bearing(mStreetViewPanorama.getPanoramaCamera().bearing - angle)
                .build();

        mStreetViewPanorama.animateTo(camera, milliSecondsDuration);
    }

    @Override
    public void onStreetViewPanoramaCameraChange(StreetViewPanoramaCamera streetViewPanoramaCamera) {
        Log.i(TAG, "SVPCameraChange - camera angle= " + streetViewPanoramaCamera.bearing);
        float svpCameraBearing = streetViewPanoramaCamera.bearing;

        mMarker.setIcon(BitmapDescriptorFactory
                .fromResource(ICON_FILENAMES[computeBearingIndex(svpCameraBearing)]));
    }


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
}
