package com.keyes_west.mapsgetstarted;


import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

public class MapsActivity extends AppCompatActivity
        implements GoogleMap.OnMarkerDragListener,
                    StreetViewPanorama.OnStreetViewPanoramaChangeListener,
                    StreetViewPanorama.OnStreetViewPanoramaCameraChangeListener {

    private static final String TAG= "MapsActivity";

    private static final String MARKER_POSITION_KEY = "MarkerPosition";

    private static final LatLng BOISE = new LatLng(43.615032, -116.202335);

    private static final float ICON_BEARING_OFFSET = 133.0f;

    private StreetViewPanorama mStreetViewPanorama;

    private Marker mMarker;

    private Projection mProjection;

    private GoogleMap mMap;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.split_street_view_panorama_and_map_demo);

        final LatLng markerPosition;
        if (savedInstanceState == null) {
            markerPosition = BOISE;
        } else {
            markerPosition = savedInstanceState.getParcelable(MARKER_POSITION_KEY);
        }

        SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment)
                        getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);

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
                // Creates a draggable marker. Long press to drag.
                mMarker = map.addMarker(new MarkerOptions()
                        .position(markerPosition)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman_ptr))
                        .draggable(true)
                        .flat(true)
                        .rotation(ICON_BEARING_OFFSET)   //aligns the pointer to North
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

        //    rotateCamera();

         //   rotateMapCamera();

            float svpCameraBearing = mStreetViewPanorama.getPanoramaCamera().bearing;
            Log.i(TAG, "Camera bearing: " + svpCameraBearing);
            mMarker.setRotation(addMarkerIconOffset(svpCameraBearing));

            //move pegman 1 inch to the right
            //get the screen coordinates of pegman
          //  Point screenPosition = mProjection.toScreenLocation(mMarker.getPosition());
          //  Log.i(TAG, "Marker Screen X= " + screenPosition.x +
          //          " Marker Screen Y= " + screenPosition.y);

           // screenPosition.x = screenPosition.x + 400;
           // LatLng newPos = mProjection.fromScreenLocation(screenPosition);
           // mMarker.setPosition(newPos);

           // mMarker.setRotation(90.0f);
        }
    }

    private void rotateMapCamera(){

        CameraPosition camPos = mMap.getCameraPosition();
        CameraPosition newPos = new CameraPosition.Builder()
                .target(camPos.target)
                .zoom(camPos.zoom)
                .bearing(camPos.bearing - 60.0f)
                .tilt(camPos.tilt)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newPos));


    }

    private void rotatePanoramaCamera(){
        long duration = 2000;

        StreetViewPanoramaCamera camera = new StreetViewPanoramaCamera.Builder()
                .zoom(mStreetViewPanorama.getPanoramaCamera().zoom)
                .tilt(mStreetViewPanorama.getPanoramaCamera().tilt)
                .bearing(mStreetViewPanorama.getPanoramaCamera().bearing - 180)
                .build();

        mStreetViewPanorama.animateTo(camera, duration);
    }

    @Override
    public void onStreetViewPanoramaCameraChange(StreetViewPanoramaCamera streetViewPanoramaCamera) {
        Log.i(TAG, "SVPCameraChange - camera angle= " + streetViewPanoramaCamera.bearing);
        float svpCameraBearing = streetViewPanoramaCamera.bearing;

        mMarker.setRotation(addMarkerIconOffset(svpCameraBearing) );
    }


    private float addMarkerIconOffset(float svpCameraBearing){
        return svpCameraBearing + ICON_BEARING_OFFSET;
    }
}
