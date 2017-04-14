package cunycodes.parkmatch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.Calendar;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    private static DatabaseReference mDatabase;
    public static int hourLeaving, minLeaving;
    public static double newLat, newLong;

    //Variable that confirms a location was picked by User.
    private final int REQUEST_CODE_PLACEPICKER = 1;

    // so we can switch from gotoParking to displaySelectedPlaceFromPlacePicker  when calling onActivityResult
    int ButtonSwitcher = 0;
    public static Boolean leavingClicked = false, searchingClicked = false;


    //Function that gets called when Map Activity begins
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference(); //initializing our static database reference
        showLandingPage();
    }

    /**
     *  Landing Page is a map dispaying user's current location with two buttons at the bottom that
     *  user can use to indicate they are leaving a parking spot or that they want to find a parking
     *  spot.
    */
    private void showLandingPage() {
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Gives clickable functionality to "LEAVING" Button
        Button LeavingButton = (Button) findViewById(R.id.Leaving);
        LeavingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonSwitcher = 1;
                leavingClicked = true;
                searchingClicked = false;
                startPlacePickerActivity();
            }
        });
        // Gives clickable functionality to "I NEED A SPOT" Button aka gotoParkingButton
        Button gotoParkingButton = (Button) findViewById(R.id.SearchParking);
        gotoParkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonSwitcher = 2;
                searchingClicked = true;
                leavingClicked = false;
                startPlacePickerActivity();
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    //Function that checks if app has permission to get current location and Zooms in on current location
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; //initialize our reference with what's passed in

        //CHECKING IF WE HAVE PERMISSION TO ACCESS USER LOCATION
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // Enable MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);
        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);

        //DISPLAYING CURRENT LOCATION
        if (myLocation != null) {
            // Get latitude of the current location
            double current_latitude = myLocation.getLatitude();
            // Get longitude of the current location
            double current_longitude = myLocation.getLongitude();
            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(current_latitude, current_longitude);
            // Show the current location in Google Map
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            // Zoom in the Google Map
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            googleMap.addMarker(new MarkerOptions().position(new LatLng(current_latitude, current_longitude)).title("You are here!"));
        }
    }

    // place a marker in a location close to the users current position
    double Nextblock = 0.0012; //why do we need this

    //Function that stores destination location (where the person wants to park) and asks for the time they need the spot
    public void gotoParking(Intent data) {
        Place placeSelected = PlacePicker.getPlace(this, data);

        String address = placeSelected.getAddress().toString();
        //get latitude and longitude of the destination
        newLat = placeSelected.getLatLng().latitude;
        newLong = placeSelected.getLatLng().longitude;

        //Select Time parking is asking when do you need to park?
        Button displayTimePicker = (Button) findViewById(R.id.displayTimePicker);
        displayTimePicker.setText("Select Time Parking");
        displayTimePicker.setVisibility(View.VISIBLE);

       ///////////////////////////////////////////////

        /*TextView enterCurrentLocation = (TextView) findViewById(R.id.SearchParking);
        enterCurrentLocation.setText("Found one near " + address);
        newLat = newLat + Nextblock;

        Nextblock = Nextblock + 0.0012;


        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(newLat, newLong);
        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Open Parking Spot Here"));*/
    }

    //Opens up the GoogleMaps PlacePicker when Buttons are clicked
    private void startPlacePickerActivity() {
        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
        // this would only work if you have your Google Places API working
        try {
            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent, REQUEST_CODE_PLACEPICKER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Function that gives functionality to Leaving/I have a spot portion. Gets coordinates of parked car and asks what time the user will
    //be leaving that spot
    private void displaySelectedPlaceFromPlacePicker(Intent data) {
        Place placeSelected = PlacePicker.getPlace(this, data);

        String address = placeSelected.getAddress().toString();

        // Get latitude of the current car location
        double latitude = placeSelected.getLatLng().latitude;

        // Get longitude of the current car location
        double longitude = placeSelected.getLatLng().longitude;

        newLat = latitude;
        newLong = longitude;

        //Time picker display button made visible
        Button displayTimePickerBtn = (Button) findViewById(R.id.displayTimePicker);
        if(placeSelected.isDataValid()) {
            displayTimePickerBtn.setText("Select Time Leaving");
            displayTimePickerBtn.setVisibility(View.VISIBLE);
        }

        //Changes text on the "Leaving" Button
        TextView enterCurrentLocation = (TextView) findViewById(R.id.Leaving);
        String buttonAddress = "Your car is at " + address;
        enterCurrentLocation.setText(buttonAddress);
    }

    //Called when button to select time leaving is clicked; What time is the user leaving?
    public void showTimePickerDialog(View v) {
        MapsActivity.TimePickerFragment newFragment = new MapsActivity.TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");

        //Sets 'choose time' button to invisible
        Button displayTimePickerBtn = (Button) findViewById(R.id.displayTimePicker);
        displayTimePickerBtn.setVisibility(View.INVISIBLE);
    }

    //Time picker class
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), R.style.DialogTheme, this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hourLeaving = hourOfDay;
            minLeaving = minute;
            String timeLeaving = Integer.toString(hourOfDay) + ":" + Integer.toString(minute);
            MapsActivity wdata = new MapsActivity();
            wdata.writeToDatabase (newLong, newLat, hourLeaving, minLeaving);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Your information has been recorded").setTitle("Thank you!");
            builder.show();
        }
    }//End of Time Picker Class

    //Function that writes to the database when either button is clicked
    public void writeToDatabase(double longitude, double latitude, int hour, int min) {

        if (leavingClicked.equals(true)) {
            AvailableSpot newAvailableSpot = new AvailableSpot(longitude, latitude, hour, min);
            String key = mDatabase.child("available_spots").push().getKey();
            newAvailableSpot.writeGeofireLocationToDatabase(mDatabase, key);
            //mDatabase.child("available_spots").child(key).setValue(newAvailableSpot);
        }
        else if (searchingClicked.equals(true)) {
            RequestedSpot newRequestedSpot = new RequestedSpot (longitude, latitude, hour, min);
            String key = mDatabase.child("requested_spots").push().getKey();
            newRequestedSpot.writeGeofireLocationToDatabase(mDatabase, key);
            // Navigation logic (...)
            // Once they have reached destination:
            destinationReachedDialog();
            // mDatabase.child("requested_spots").child(key).setValue(newRequestedSpot);
        }
    }

    //If the user picked a location, call the functions that handles leaving/Wanting a spot
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PLACEPICKER && resultCode == RESULT_OK) {
            if (ButtonSwitcher == 1) {
                displaySelectedPlaceFromPlacePicker(data); //LEAVING BUTTON
            }
            if (ButtonSwitcher == 2) {
                gotoParking(data); //I NEED A SPOT BUTTON
            }
        }
    }

    //Implementation of menu bar
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Implementation of menu options
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_account:
                Toast.makeText(MapsActivity.this, "Account Settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.nav_settings:
                Toast.makeText(MapsActivity.this, "Settings", Toast.LENGTH_LONG).show();
                return true;
            case R.id.nav_logout:   //when user clicks "Log out" we delete all cached app data.
                File cache = getCacheDir();
                File appDir = new File(cache.getParent());
                if (appDir.exists()) {
                    String[] children = appDir.list();
                    for (String s : children) {
                        if (!s.equals("lib")) {
                            deleteDir(new File(appDir, s));
                            Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                        }
                    }
                }
                Intent intent = new Intent(MapsActivity.this, LoginActivity.class); //Login Activity is started after all data is removed
                startActivity(intent);
                finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Function that recursively deletes app data for Logging out; all saved files get deleted
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    /**
     * This method is intended to be called after a user is navigated to their parking space of choice.
     * A pop-up message will ask user if they were able to find parking. If they did, they will be
     * rerouted to the landing page. If they were not, we will return them to the map to choose another
     * available space.
     */
    private void destinationReachedDialog() {
        new AlertDialog.Builder(MapsActivity.this)
                .setTitle("Destination reached! Did you find parking?")
                .setNeutralButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User has parked their car. Reroute them to the homepage.
                        showLandingPage();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User did not find parking where we had directed them to. Return to
                        // map to let user choose another available spot
                        startPlacePickerActivity();
                    }
                }).show();
    }
}
