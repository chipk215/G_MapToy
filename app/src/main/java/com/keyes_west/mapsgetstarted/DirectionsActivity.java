package com.keyes_west.mapsgetstarted;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


//Directions API key:  AIzaSyD2K7D0bXauBddNa55_wuQB6qMGq68PECA


public class DirectionsActivity extends AppCompatActivity {

    private static final String TAG= "DirectionsActivity";
    private GoogleMap mMap;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directions_activity);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                Log.i(TAG, "onMapReady Invoked");
                mMap = map;
                new FetchDirectionsTask(null).execute();

            }
        });


    }


    private class FetchDirectionsTask extends AsyncTask<Void,Void,String>{

        private String mQuery;

        public FetchDirectionsTask(String query){
            mQuery = query;
        }


        @Override
        protected String doInBackground(Void... voids){
            return new DirectionsFetcher().getDirections(mQuery);
        }

        @Override
        protected void onPostExecute(String jsonResponse){

            try {
                JSONObject jsonBody = new JSONObject(jsonResponse);
                JSONArray routes = jsonBody.getJSONArray("routes");
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPoints = overviewPolyline.getString("points");
                Log.i(TAG, "Encoded points: " + encodedPoints);
                List<LatLng> decodedPath = PolyUtil.decode(encodedPoints);
                mMap.addPolyline(new PolylineOptions().addAll(decodedPath));

            }catch(JSONException je){
                Log.e(TAG, "Failed to parse JSON", je);
            }
        }
    }
}
