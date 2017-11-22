package ca.bcit.comp3717.guardian.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.bcit.comp3717.guardian.HttpHandler;
import ca.bcit.comp3717.guardian.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private GoogleMap mMap;
    private String TAG = MapsActivity.class.getSimpleName();
    ArrayList<EmergencyBuilding> locationList;
    private FusedLocationProviderClient mFusedLocationClient;
    SupportMapFragment mapFragment;
    boolean fireFilter;
    boolean hospitalFilter;
    boolean policeFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationList = new ArrayList<>();
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/Guardians.ttf");

        Button tx = (Button)findViewById(R.id.back);
        tx.setTypeface(custom_font);

        fireFilter = true;
        hospitalFilter = true;
        policeFilter = true;
        new GetLocations().execute();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void loadMap(){

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    Toast.makeText(this, "Can not show your location.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    Toast.makeText(this, "Can not show your location.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void back(View view) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        LatLng newWest = new LatLng(49.2057, -122.9110);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newWest));
        mMap.setMinZoomPreference(12);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(latLng).title("Your location"));
                        }
                    }
                });
        for (int i = 0; i < locationList.size(); i++) {
            EmergencyBuilding item = locationList.get(i);
            if ((fireFilter && item.getCategory() == 1) || (hospitalFilter && item.getCategory() == 2) || (policeFilter && item.getCategory() == 3)) {
                LatLng latLng = new LatLng(Float.parseFloat(item.getLatitutde()), Float.parseFloat(item.getLongitude()));
                switch(item.getCategory()) {
                    case 1:
                        mMap.addMarker(new MarkerOptions().position(latLng).title(item.getBldgName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker)));
                        break;
                    case 2:
                        mMap.addMarker(new MarkerOptions().position(latLng).title(item.getBldgName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.darkgreen_marker)));
                        break;
                    case 3:
                        mMap.addMarker(new MarkerOptions().position(latLng).title(item.getBldgName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_marker)));
                        break;
                }
            }
        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.fireCheckBox:
                if (checked) {
                    fireFilter = true;
                }
            else {
                    fireFilter = false;
                }
                break;
            case R.id.hospitalCheckBox:
                if (checked) {
                    hospitalFilter = true;
                }
            else {
                    hospitalFilter = false;
                }
                break;
            case R.id.policeCheckBox:
                if (checked) {
                    policeFilter = true;
                }
                else {
                    policeFilter = false;
                }
                break;
        }
        loadMap();
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetLocations extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String SERVICE_URL = "http://guardiannewwestapi.azurewebsites.net/emergencybldg/get/all/";
            String jsonStr = sh.makeServiceCall(SERVICE_URL);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Getting JSON Array node
                    JSONObject emergBldgJSONArray = new JSONObject(jsonStr);
                    // looping through All Contacts
                    String building = emergBldgJSONArray.getString("buildings");
                    JSONArray buildingJSONArray = new JSONArray(building);

                    // looping through All Contacts
                    for (int i = 0; i < buildingJSONArray.length(); i++) {
                        JSONObject c = buildingJSONArray.getJSONObject(i);
                        String BldgName = c.getString("BldgName");
                        String Lat = c.getString("Lat");
                        String Lng = c.getString("Lng");
                        String category = c.getString("Category");
                        Long phone = c.getLong("Phone");
                        // tmp hash map for single contact
                        final EmergencyBuilding emergencyBldg = new EmergencyBuilding();

                        // adding each child node to HashMap key => value
                        emergencyBldg.setBldgName(BldgName);
                        emergencyBldg.setLatitutde(Lat);
                        emergencyBldg.setLongitude(Lng);
                        emergencyBldg.setCategory(Integer.parseInt(category));
                        emergencyBldg.setPhone(phone);
                        locationList.add(emergencyBldg);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            loadMap();
        }
    }
}
