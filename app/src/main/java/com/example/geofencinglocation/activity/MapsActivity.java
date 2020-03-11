package com.example.geofencinglocation.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.geofencinglocation.ProgressDialog;
import com.example.geofencinglocation.R;
import com.example.geofencinglocation.baseclasses.AbstractProjectBaseActivity;
import com.example.geofencinglocation.retrofitsdk.APIClient;
import com.example.geofencinglocation.retrofitsdk.APIInterface;
import com.example.geofencinglocation.retrofitsdk.model.LocationListPojo;
import com.example.geofencinglocation.retrofitsdk.response.LocationListResponse;
import com.example.geofencinglocation.retrofitsdk.response.ShortestDistanceResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.RECORD_AUDIO;

public class MapsActivity extends AbstractProjectBaseActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int REQ_PERMISSION = 101;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private GoogleApiClient googleApiClient;
    private Marker locationMarker;
    private List<LocationListPojo> dataList;
    private String title;
    private double distAB, distBC, distAC, temp, shortestDistance;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        relativeLayout = findViewById(R.id.parentPanel);
        if (!checkPermission()) {
            requestPermission();
        } else {
            getAllGeoFenceLocation();
            Snackbar.make(relativeLayout, "Permission already granted.", Snackbar.LENGTH_LONG).show();
        }

        // initialize GoogleMaps
        initGMaps();
        // create GoogleApiClient
        createGoogleApi();
    }

    private void getAllGeoFenceLocation() {
        ProgressDialog.getInstance().show(MapsActivity.this);
        APIInterface service = new APIClient.Builder().build(MapsActivity.this).getAPIInterface();
        service.getGeoFencingResponse().enqueue(new Callback<LocationListResponse>() {
            @Override
            public void onResponse(Call<LocationListResponse> call, Response<LocationListResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getStatus() == 200) {
                        ProgressDialog.getInstance().dismiss();
                        dataList = new ArrayList<>();
                        dataList.addAll(response.body().getData());
                        Log.e(TAG, "List size: " + String.valueOf(dataList.size()));
                        Toast.makeText(MapsActivity.this, response.body().getResponse(), Toast.LENGTH_SHORT).show();

                        for (int i = 0; i < dataList.size(); i++) {
                            markerLocation(Double.parseDouble(dataList.get(i).getLatitude()), Double.parseDouble(dataList.get(i).getLongitude()), dataList.get(i));
                        }

                        /**calculate shortest distance between the three points */
                        distanceBetweenPointA_To_PointB(Double.parseDouble(dataList.get(0).getLatitude()), Double.parseDouble(dataList.get(0).getLongitude()), Double.parseDouble(dataList.get(1).getLatitude()), Double.parseDouble(dataList.get(1).getLongitude()));
                        distanceBetweenPointA_To_PointC(Double.parseDouble(dataList.get(0).getLatitude()), Double.parseDouble(dataList.get(0).getLongitude()), Double.parseDouble(dataList.get(2).getLatitude()), Double.parseDouble(dataList.get(2).getLongitude()));
                        distanceBetweenPointB_To_PointC(Double.parseDouble(dataList.get(1).getLatitude()), Double.parseDouble(dataList.get(1).getLongitude()), Double.parseDouble(dataList.get(2).getLatitude()), Double.parseDouble(dataList.get(2).getLongitude()));

                        /** find a shortest distance among three distance */
                        temp = distAB < distBC ? distAB : distBC;
                        shortestDistance = distAC < temp ? distAC : temp;
                        System.out.println("ShortestDistance is:" + shortestDistance);

                    } else {
                        Toast.makeText(MapsActivity.this, response.body().getResponse(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LocationListResponse> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Couldn't connect internet connection!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Initialize GoogleMaps
    private void initGMaps() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    // Create a Location Marker
    private void markerLocation(double latitude, double longitude, LocationListPojo locationListPojo) {
        if (locationListPojo.getGeofenceId() == 1) {
            title = "A";
        } else if (locationListPojo.getGeofenceId() == 2) {
            title = "B";
        } else if (locationListPojo.getGeofenceId() == 3) {
            title = "C";
        }
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(title);
        if (map != null) {
            locationMarker = map.addMarker(markerOptions);
            locationMarker.showInfoWindow();
            float zoom = 12.5f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom);
            map.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick(" + latLng + ")");

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition());
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    private double distanceBetweenPointA_To_PointB(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        distAB = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        distAB = Math.acos(distAB);
        distAB = rad2deg(distAB);
        distAB = distAB * 60 * 1.1515;
        Log.e(TAG, "Distance Between PointA To PointB--->" + distAB);
        return (distAB);
    }

    private double distanceBetweenPointA_To_PointC(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        distAC = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        distAC = Math.acos(distAC);
        distAC = rad2deg(distAC);
        distAC = distAC * 60 * 1.1515;
        Log.e(TAG, "Distance Between PointA To PointC--->" + distAC);
        return (distAC);
    }

    private double distanceBetweenPointB_To_PointC(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        distBC = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        distBC = Math.acos(distBC);
        distBC = rad2deg(distBC);
        distBC = distBC * 60 * 1.1515;
        Log.e(TAG, "Distance Between PointB To PointC--->" + distBC);
        return (distBC);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public void onClickGetDistance(View view) {
        ProgressDialog.getInstance().show(MapsActivity.this);
        APIInterface service = new APIClient.Builder().build(MapsActivity.this).getAPIInterface();
        service.getShortestDistanceResponse(shortestDistance, "2").enqueue(new Callback<ShortestDistanceResponse>() {
            @Override
            public void onResponse(Call<ShortestDistanceResponse> call, Response<ShortestDistanceResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getStatus() == 200) {
                        ProgressDialog.getInstance().dismiss();
                        Toast.makeText(MapsActivity.this, response.body().getMsg(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MapsActivity.this, response.body().getResponse(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ShortestDistanceResponse> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Couldn't connect internet connection!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {

        }
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQ_PERMISSION);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSION:
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        getAllGeoFenceLocation();
                        Snackbar.make(relativeLayout, "Permission Granted, Now you can access location.", Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(relativeLayout, "Permission Denied, You cannot access location.", Snackbar.LENGTH_LONG).show();

                        boolean showRationale = shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION);
                        if (!showRationale) {
                            openSettingsDialog();
                        }
                    }
                }

                break;
        }
    }

    private void openSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Required Permissions");
        builder.setMessage("This app require permission to use location feature. Grant them in app settings.");
        builder.setPositiveButton("Take Me To SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQ_PERMISSION);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
