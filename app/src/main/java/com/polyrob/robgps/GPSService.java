package com.polyrob.robgps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GPSService extends Service implements LocationListener {
    private static final String TAG = "GPSService";
    private static final String REST_URL = "http://robbiescheidt.pythonanywhere.com/robgps";
    public static final int DELAY = 1000 * 60 * 10; // approx every 10 min

    private LocationManager locationManager;
    private RestCall restCall;

    private long lastLocationTime;

    public GPSService() {
        Log.i(TAG, "GPSService constructor...");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TAG", "Service created.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.i(TAG, "onBind()");
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int val = super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Doing onStartCommand");

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        /* use LocationListener interface to request location updates periodically */
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DELAY, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DELAY, 0, this);

//        Toast.makeText(getBaseContext(), "Starting GPSService.", Toast.LENGTH_LONG).show();

//        Handler handler = new Handler();

//        final Runnable r = new Runnable() {
//            public void run() {
//                while (true) {
//
//                }
//            }
//        };

//        handler.post(r);

        return val;
    }

    private void handleLocationEvent(Location location) {
        /* if this is a new location, push */
        if (location.getTime() != lastLocationTime) {
            Log.i(TAG, "New location found. Pushing.");
            lastLocationTime = location.getTime();

            ArrayList<NameValuePair> params = new ArrayList<>();
            String time = convertTime(location.getTime());
            String provider = location.getProvider();
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            params.add(new BasicNameValuePair("time", time));
            params.add(new BasicNameValuePair("lat", Double.toString(lat)));
            params.add(new BasicNameValuePair("lng", Double.toString(lng)));

            StringBuilder sb = new StringBuilder("Loc data: ");
            sb.append(time).append(", provider: ").append(provider).append(" - lat/lng: ").append(lat).append(", ").append(lng);
            Log.i(TAG, sb.toString());

            restCall = new RestCall(RestCall.RequestMethod.POST, REST_URL, params);
            restCall.execute();
        } else {
            Log.i(TAG, "is same locaiton. Not making REST call.");
        }

                    /* wait a while before checking again */
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            Log.i(TAG, "An error has occurred, " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged event received!!! ");
        handleLocationEvent(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
