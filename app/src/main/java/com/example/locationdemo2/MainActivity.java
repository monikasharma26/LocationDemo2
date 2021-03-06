package com.example.locationdemo2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1;

    //LOcation Demo With Fused Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    //ConstantVariable
    private static final int UPDATE_INTERVAL = 5000; //5 seconds
    private static final int FASTEST_INTERVAL = 3000; //3 seconds

    private List<String> permissionsRequest;
    private List<String> permissionsRejected;
    private List<String> permissions = new ArrayList<>();
    private List<String> getPermissionsRequest = new ArrayList<>();
    private TextView location_tv;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        location_tv = findViewById(R.id.location_tv);

        //instantiate the fusedLoacationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //add permissions

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsRequest = permissionsRequest(permissions);
        if (permissionsRequest.size() > 0) {
            requestPermissions(permissionsRequest.toArray(new String[permissionsRequest.size()]), REQUEST_CODE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private List<String> permissionsRequest(List<String> permissions) {
        ArrayList<String> results = new ArrayList<>();
        for (String perm : permissions) {
            if (!hasPermissions(perm))
                results.add(perm);
        }
        return results;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermissions(String perm) {
        return checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //this is a proper place to check google play services availablity
        int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = GoogleApiAvailability.getInstance().
                    getErrorDialog(this, errorCode, errorCode, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Toast.makeText(MainActivity.this, "NO", Toast.LENGTH_LONG).show();
                            //    Toast.makeText(MainActivity.this,"NoGoogle Api Service",Toast.LENGTH_LONG).show();
                        }
                    });
            errorDialog.show();
        }
    }

    private void findLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        location_tv.setText(String.format("Lat %s, lng %s", location.getLatitude(), location.getLongitude()));
                    }
                }
            });
        }
        startUpdateLocation();
    }

    private void startUpdateLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    location_tv.setText(String.format("Lat: %s , lng %s", location.getLatitude(), location.getLongitude()));
                }
            }
        };
        
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if(requestCode == REQUEST_CODE){
        for(String perm: permissions)
        {
            if(!hasPermissions(perm))
                permissionsRejected.add(perm);
        }
        if(permissionsRejected.size() > 0){
            if(shouldShowRequestPermissionRationale(permissionsRejected.get(0)))
            {
                new AlertDialog.Builder(MainActivity.this).
                        setMessage("Permission to location").setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), REQUEST_CODE);
                        }
                    }
                }).setNegativeButton("cancel",null).create()
                        .show();
            }
        }
    }
    }
}