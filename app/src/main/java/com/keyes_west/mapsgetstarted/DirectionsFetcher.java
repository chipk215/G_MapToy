package com.keyes_west.mapsgetstarted;


import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DirectionsFetcher {

    private static final String TAG = "DirectionsFetcher";

    private static final String API_KEY = " AIzaSyD2K7D0bXauBddNa55_wuQB6qMGq68PECA";

    private static final Uri DIRECTIONS_ENDPOINT = Uri
            .parse("https://maps.googleapis.com/maps/api/directions/json")
            .buildUpon()
            .appendQueryParameter("outputFormat", "json")
            .appendQueryParameter("key", API_KEY)
            .appendQueryParameter("origin","43.675429, -116.309703")
            .appendQueryParameter("destination","43.61389, -116.200717")
            .build();


    public String getDirections(String query)   {

        String url = DIRECTIONS_ENDPOINT.toString();

        String result = null;
        try {
            result = new String(getUrlBytes(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }



    public byte[] getUrlBytes(String urlSpec) throws IOException {

        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() +
                        ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally{
            connection.disconnect();
        }
    }
}
