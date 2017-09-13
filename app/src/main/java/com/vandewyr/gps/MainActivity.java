package com.vandewyr.gps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private String TAG = "GPS SERVICE";
    private int perms = 1;
    private Location location;
    private String latitude;
    private String longitude;
    private GoogleApiClient client;
    private LocationRequest mLocationRequest;
    DBHelper helper;
    private SQLiteDatabase db;
    private String sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new DBHelper(this);


        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if(requestPermissions() == 1){
                client.connect();
            }
        }else{
            client.connect();
        }

        ArrayList<HashMap<String, String>> airport_list;// = new ArrayList<HashMap<String, String>>();
        db = helper.getReadableDatabase();
        Log.i("DB MAIN", db.toString());
        //String sql ="SELECT * FROM airport_info";
        //airport_list = helper.getAirportList();


    }

    @Override
    public void onResume(){
        super.onResume();

        client.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
            client.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //Toast.makeText(this, "Location services connected", Toast.LENGTH_SHORT)
        //.show();
        try{
            location = LocationServices.FusedLocationApi.getLastLocation(client);
            if(location != null){
                handleLocation(location);
            }
        }catch(SecurityException e){
            Toast.makeText(this, "GPS Error", Toast.LENGTH_SHORT)
                    .show();
        }

        try {
            System.out.println("REQUESTING UPDATES");
            LocationServices.FusedLocationApi.requestLocationUpdates(client, mLocationRequest, this);
        }catch(SecurityException e){
            Log.d(TAG, "Minor");
        }
    }

    private void getNearestAirport(double latitude, double longitude){
        try {
            helper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        toast(Double.toString(latitude) + "  " + Double.toString(longitude));
        String airport = helper.openDataBase(latitude, longitude);

        toast("Closest airport is " + airport);
    }

    private void handleLocation(Location location){
        double roundLat = location.getLatitude();
        double roundLong = location.getLongitude();
        latitude = Double.toString(roundLat);
        longitude = Double.toString(roundLong);

        getNearestAirport(roundLat, roundLong);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Location services suspended", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onLocationChanged(Location location) {
        handleLocation(location);
    }

    public int requestPermissions(){
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    0);
        }else{
            return 1;
        }
        return 0;
    }

    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
