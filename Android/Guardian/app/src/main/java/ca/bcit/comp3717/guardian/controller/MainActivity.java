package ca.bcit.comp3717.guardian.controller;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.api.HttpHandler;
import ca.bcit.comp3717.guardian.model.EmergencyBuilding;
import ca.bcit.comp3717.guardian.model.User;

public class MainActivity extends Activity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private FusedLocationProviderClient mFusedLocationClient;
    private String TAG = MapsActivity.class.getSimpleName();

    ArrayList<EmergencyBuilding> locationList;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationList = new ArrayList<>();
        new GetLocations().execute();

        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/Guardians.ttf");

        Button tx = (Button)findViewById(R.id.mapBtn);
        tx.setTypeface(custom_font);
        tx = (Button)findViewById(R.id.linkAccBtn);
        tx.setTypeface(custom_font);
        tx = (Button)findViewById(R.id.userAccBtn);
        tx.setTypeface(custom_font);
        tx = (Button)findViewById(R.id.logoutBtn);
        tx.setTypeface(custom_font);

        Intent i = getIntent();
        user = new User();
        user.setId(i.getIntExtra("userId", -1));
        user.setUserName(i.getStringExtra("userName"));
        user.setEmail(i.getStringExtra("email"));
        user.setPassword(i.getStringExtra("password"));
    }

    public void alert (View view) {
        final long numbers[] = new long[4];
        HttpHandler sh = new HttpHandler();

        new GetLocations().execute();

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
                            for (int i = 0; i < locationList.size(); i++) {
                                EmergencyBuilding item = locationList.get(i);
                                if (numbers[0] == 0 && item.getCategory() == 1) {
                                    numbers[0] = item.getPhone();
                                }
                                if (numbers[1] == 0 && item.getCategory() == 2) {
                                    numbers[1] = item.getPhone();
                                }
                                if (numbers[2] == 0 && item.getCategory() == 3) {
                                    numbers[2] = item.getPhone();
                                }
                            }
                        }
                    }
                });
        /*for (int i = 0; i < locationList.size(); i++) {
            EmergencyBuilding item = locationList.get(i);
            if (numbers[0] == 0 && item.getCategory() == 1) {
                numbers[0] = item.getPhone();
            }
            if (numbers[1] == 0 && item.getCategory() == 2) {
                numbers[1] = item.getPhone();
            }
            if (numbers[2] == 0 && item.getCategory() == 3) {
                numbers[2] = item.getPhone();
            }
        }*/

        // Create custom dialog object
        final Dialog dialog = new Dialog(MainActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.alert_layout);
        // Set dialog title
        dialog.setTitle("Alert");
            Button fire = (Button) dialog.findViewById(R.id.fireBtn);
            fire.setText("" + numbers[0]);
            Button hospital = (Button) dialog.findViewById(R.id.hospitalBtn);
            hospital.setText("" + numbers[1]);
            Button police = (Button) dialog.findViewById(R.id.policeBtn);
            police.setText("" + numbers[2]);

        dialog.show();
    }

    public void map (View view) {
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    public void linkAcc (View view) {
        Intent i = new Intent(this, LinkedAccountActivity.class);
        startActivity(i);
    }

    public void userAcc (View view) {
        Intent i = new Intent(this, UserAccountActivity.class);
        startActivity(i);
    }

    public void logout (View view) {
        new LogoutUserTask().execute();
    }

    public void goToLandingActivity() {
        Intent i = new Intent(this, LandingActivity.class);
        Toast.makeText(this.getBaseContext(), user.getUserName() + " Logged out", Toast.LENGTH_SHORT).show();
        startActivity(i);
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
        }
    }
    private class LogoutUserTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voidArgs) {
            HttpHandler.UserController.logoutByEmail(user.getEmail(), user.getPassword());
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            super.onPostExecute(args);
            Log.d("API Response", user.toString());
            goToLandingActivity();
        }
    }
}

