package com.polyrob.robgps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
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

public class GPSService extends Service {
    private static final String TAG = "GPSService";
    private static final String REST_URL = "http://robbiescheidt.pythonanywhere.com/robgps";
    public static final int DELAY = 1000 * 60 * 10; // approx every 10 min

    private LocationManager locationManager;
    private RestCall restCall;

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

        Toast.makeText(getBaseContext(), "Starting GPSService.", Toast.LENGTH_LONG).show();

        Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                while (true) {
                    /* get latest location */
                    Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc == null)
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


                    ArrayList<NameValuePair> params = new ArrayList<>();

                    String time = convertTime(loc.getTime());
                    String provider = loc.getProvider();
                    double lat = loc.getLatitude();
                    double lng = loc.getLongitude();

                    params.add(new BasicNameValuePair("time", time));
                    params.add(new BasicNameValuePair("lat", Double.toString(lat)));
                    params.add(new BasicNameValuePair("lng", Double.toString(lng)));

                    StringBuilder sb = new StringBuilder("Loc data: ");
                    sb.append(time).append(", provider: ").append(provider).append(" - lat/lng: ").append(lat).append(", ").append(lng);
                    Log.i(TAG, sb.toString());

                    restCall = new RestCall(RestCall.RequestMethod.POST, REST_URL, params);
                    restCall.execute();

                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "An error has occurred, " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        };

        handler.post(r);

        return val;
    }


    private String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }

}
