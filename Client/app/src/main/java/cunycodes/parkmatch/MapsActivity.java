package cunycodes.parkmatch;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private static MapsActivity inst;
    static int alarmHour;
    static int alarmMinute;
    protected static User user;
    Boolean dialogShownOnce = false;
    public static GoogleMap mMap;
    private static DatabaseReference mDatabase;
    public static int hourLeaving, minLeaving;
    public static double newLat, newLong;
    public static String lastKey="";
    private RetrieveAvailable retrieveAvailableParkingSpots;
    private AvailableSpot userSelectedSpot;
    private final int REQUEST_CODE_PLACEPICKER = 1;//Variable that confirms a location was picked by User.
    int ButtonSwitcher = 0;// so we can switch from gotoParking to displaySelectedPlaceFromPlacePicker  when calling onActivityResult
    public static Boolean leavingClicked = false, searchingClicked = false, moreTime = false;
    final int removeTime = 5; //remove spots that are this minute old

    public static MapsActivity instance() { return inst; }

    public void onStart() {super.onStart(); inst = this; }

    //Function that gets called when Map Activity begins
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();//initializing our static database reference
        moreTime = false;
        lastKey = "";

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        if (minute >= 0 && minute < removeTime) {
            hour--;
            minute = (minute - removeTime) + 60;
        } else
            minute = minute - removeTime;

        String time = Integer.toString(hour)+":"+Integer.toString(minute);
        //this.cleanDatabase(time);

        showLandingPage();
    }

    //Removes any available spots that have become available before the current time minus 5 minutes
    private void cleanDatabase (String time) {
        Query timeQuery = mDatabase.child("available_spots").orderByChild("timeLeaving").endAt(time);

        timeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    System.out.println(snapshot);
                    AvailableSpot s = snapshot.getValue(AvailableSpot.class);
                    String key = snapshot.getKey();
                    snapshot.getRef().removeValue();

                    //Remove from geofire_locations in addition to available_spots
                    Query geoQuery = mDatabase.child("geofire_locations").child("available").orderByKey().equalTo(key);

                    geoQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot geoDataSnapshot) {
                            for (DataSnapshot geoSnapshot: geoDataSnapshot.getChildren())
                                geoSnapshot.getRef().removeValue();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.err.println(databaseError);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println(databaseError);
            }
        });
    }

    /**
     *  Landing Page is a map displaying user's current location with two buttons at the bottom that
     *  user can use to indicate they are leaving a parking spot or that they want to find a parking
     *  spot.
     */
    private void showLandingPage() {
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        user = new User ();
        getCurrentUser();

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

        // sets up the alarm
        //alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);
        //alarmTextView = (TextView) findViewById(R.id.alarmText);
        //ToggleButton alarmToggle = (ToggleButton) findViewById(R.id.alarmToggle);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // sets up the alarm

        //alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);
        //alarmTextView = (TextView) findViewById(R.id.alarmText);
        //ToggleButton alarmToggle = (ToggleButton) findViewById(R.id.alarmToggle);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
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

    //Function that stores destination location (where the person wants to park) and asks for the time they need the spot
    public void gotoParking(Intent data) {
        Place placeSelected = PlacePicker.getPlace(this, data);

        //String address = placeSelected.getAddress().toString();

        //get latitude and longitude of the destination
        newLat = placeSelected.getLatLng().latitude;
        newLong = placeSelected.getLatLng().longitude;
        /*
        //Select Time parking is asking when do you need to park?
        Button displayTimePicker = (Button) findViewById(R.id.displayTimePicker);
        displayTimePicker.setText("Select Time Parking");
        displayTimePicker.setVisibility(View.VISIBLE);
        */

        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        writeToDatabase (newLong, newLat, hour, minute);
        GeoLocation requested = new GeoLocation(newLat, newLong);
        retrieveAvailableParkingSpots = new RetrieveAvailable (requested);
        retrieveAvailableParkingSpots.retrieveAvailableSpots(mDatabase);
    }


    //Opens up the GoogleMaps PlacePicker when Buttons are clicked
    public void startPlacePickerActivity() {
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

        if(placeSelected.isDataValid()) {
            Make_visible();//jws0405
        }

        //Changes text on the "Leaving" Button
       /* TextView enterCurrentLocation = (TextView) findViewById(R.id.Leaving);
        String buttonAddress = "Your car is at " + address;
        enterCurrentLocation.setText(buttonAddress);
        */
    }

    //JAME'S NEW ADDITION
    public  void Make_visible(){

        Button TimePickerBtn = (Button) findViewById(R.id.displayTimePicker);
        Button LeavingButton = (Button) findViewById(R.id.Leaving);
        Button SerchParkingButton = (Button) findViewById(R.id.SearchParking);

        if (TimePickerBtn.getVisibility() != View.VISIBLE) {
            TimePickerBtn.setVisibility(View.VISIBLE);

            LeavingButton.setVisibility(View.INVISIBLE);
            SerchParkingButton.setVisibility(View.INVISIBLE);

        } else if (TimePickerBtn.getVisibility() == View.VISIBLE){
            TimePickerBtn.setVisibility(View.INVISIBLE);

            LeavingButton.setVisibility(View.VISIBLE);
            SerchParkingButton.setVisibility(View.VISIBLE);
        }


    }

    //Called when button to select time leaving is clicked; What time is the user leaving?
    public void showTimePickerDialog(View v) {
        MapsActivity.TimePickerFragment newFragment = new MapsActivity.TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");

        //Sets 'choose time' button to invisible
        Make_visible();

    }

    //Time picker class
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
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

            //make alarmHour and alarmMinute equal to leaving time for the alarm
            alarmHour = hourLeaving;
            alarmMinute = minLeaving;

            ((MapsActivity)getActivity()).ActivateAlarm();

            if (moreTime)
                updateTimeInDatabase (hourLeaving, minLeaving);
            else
                writeToDatabase (newLong, newLat, hourLeaving, minLeaving);
        }
    }//End of Time Picker Class

    public static void updateTimeInDatabase(int hour, int min) {
        mDatabase.child("available_spots").child(lastKey).child("hourLeaving").setValue(hour);
        mDatabase.child("available_spots").child(lastKey).child("minLeaving").setValue(min);
        mDatabase.child("available_spots").child(lastKey).child("timeLeaving").setValue(Integer.toString(hour)+":"+Integer.toString(min));
    }

    //Function that displays a dialog box confirming selected location
    public void SelectLocationMessage(final double lat, final double lng)  {
        String address = getAddress (lat, lng);

        userSelectedSpot = retrieveAvailableParkingSpots.getSelected();

        String minuteFormat=String.format("%02d", userSelectedSpot.getMinLeaving());

        int hourFormat=userSelectedSpot.getHourLeaving();
        String AmPm="AM";
        if(userSelectedSpot.getHourLeaving()>12){
            hourFormat=hourFormat-12;
            AmPm="PM";}

        AlertDialog.Builder d = new AlertDialog.Builder(MapsActivity.instance());
        d.setTitle("Would you like to park at:")
                .setMessage(address+"?\n\nThe parking spot will be available at: "+hourFormat+":"+minuteFormat+" "+AmPm+"\nCar type previously parked at this location: "+userSelectedSpot.getCarType())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //NAVIGATION WOULD GO HERE
                        //Make all other markers disappear?
                        destinationReachedDialog (lat, lng);
                    }
                })
                .setNegativeButton("Next Spot", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        retrieveAvailableParkingSpots.setSelected(null);
                    }
                }).show();
    }

    //Function that writes to the database when either button is clicked
    public static void writeToDatabase (double longitude, double latitude, int hour, int min) {
        if (leavingClicked.equals(true)) {
            AvailableSpot newAvailableSpot = new AvailableSpot(longitude, latitude, hour, min);
            String key = mDatabase.child("available_spots").push().getKey();
            newAvailableSpot.writeGeofireLocationToDatabase(mDatabase, key);
            mDatabase.child("available_spots").child(key).setValue(newAvailableSpot);
            lastKey = key;
            if (!pointsManager("add")) System.out.println("ERROR IN POINTS MANAGER");
        }
        else if (searchingClicked.equals(true)) {
            RequestedSpot newRequestedSpot = new RequestedSpot (longitude, latitude);
            String key = mDatabase.child("requested_spots").push().getKey();
            newRequestedSpot.writeGeofireLocationToDatabase(mDatabase, key);
            mDatabase.child("requested_spots").child(key).setValue(newRequestedSpot);
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

    protected void ActivateAlarm() {

        Log.d("MapsActivity", "Alarm On");
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
        calendar.set(Calendar.MINUTE, alarmMinute);
        Intent myIntent = new Intent(MapsActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MapsActivity.this, 0, myIntent, 0);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        displayThankYou(0);
    }

    //JAMES ADDITION
    public void NeedMoreTimeMessage() {
        int hour=alarmHour;
        String AmPm="AM";
        String minute=String.format("%02d", alarmMinute);

        if(alarmHour>12){
            hour=hour-12;
            AmPm="PM";
        }
        if( dialogShownOnce == false) {
            dialogShownOnce = true;
            //final View v = findViewById(R.id.displayTimePicker);
            new AlertDialog.Builder(MapsActivity.this)
                    .setTitle("You are set to leave at " + hour + ":" + minute +" "+AmPm+" " )
                    .setMessage("Do you need more time?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            moreTime = true;
                            Make_visible();
                            dialogShownOnce = false;
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            moreTime = false;
                            dialogShownOnce = false;
                        }
                    }).show();
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
                // opens App setting
                Intent SettingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(SettingsIntent);
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
    private void destinationReachedDialog(final double lat, final double lng) {
        final AlertDialog.Builder d = new AlertDialog.Builder(MapsActivity.instance());
        d.setTitle("Destination reached! Did you find parking?")
                .setNeutralButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get current user
                        getCurrentUser();
                        //remove a point from user that used app to find spot
                        if(!pointsManager("remove")) System.out.println("ERROR IN POINTS MANAGER");
                        //give user who gave a spot a point
                        giveOtherUserPoints(userSelectedSpot.getUserId());

                        //Parking spot chosen is now occupied by the user -- remove from database
                        removeFromDatabase(lat, lng);
                        //Thank user for using app and go back to homepage
                        displayThankYou(1);
                        searchingClicked = false;
                        leavingClicked = false;

                        new CountDownTimer(2500, 1000) {

                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                startActivity(new Intent (getApplicationContext(), MapsActivity.class));
                            }
                        }.start();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User did not find parking where we had directed them to. Return to
                        // map to let user choose another available spot
                        // startPlacePickerActivity(); << this func returns nothing call gotoparking

                        //Parking spot chosen was unavailable -- remove from database
                        removeFromDatabase(lat, lng);
                        //Remove marker from chosen spot

                        //Ask if user wants another spot
                        d.setTitle("Would you like to search for another parking spot?")
                                .setNeutralButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Get new spot
                                        ButtonSwitcher = 2;
                                        searchingClicked = true;
                                        leavingClicked = false;
                                        startPlacePickerActivity();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Thank user for using app and go back to landing page
                                        displayThankYou(1);
                                        searchingClicked = false;
                                        leavingClicked = false;

                                        new CountDownTimer(2500, 1000) {

                                            @Override
                                            public void onTick(long millisUntilFinished) {
                                            }

                                            @Override
                                            public void onFinish() {
                                                startActivity(new Intent (getApplicationContext(), MapsActivity.class));
                                            }
                                        }.start();
                                    }
                                }).show();
                    }
                }).show();
    }

    public static void removeFromDatabase(double lat, double lng) {

        final double l = lng;
        Query availQuery = mDatabase.child("available_spots").orderByChild("latitude").equalTo(lat);

        availQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    System.out.println(snapshot);
                    AvailableSpot s = snapshot.getValue(AvailableSpot.class);
                    if (s.getLongitude() == l) {

                        String key = snapshot.getKey();
                        snapshot.getRef().removeValue();

                        //Remove from geofire_locations in addition to available_spots
                        Query geoQuery = mDatabase.child("geofire_locations").child("available").orderByKey().equalTo(key);

                        geoQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot geoDataSnapshot) {
                                for (DataSnapshot geoSnapshot: geoDataSnapshot.getChildren())
                                    geoSnapshot.getRef().removeValue();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                System.err.println(databaseError);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println(databaseError);
            }
        });
    }

    //given a set of Latitude and longitude, it returns a string containing an address
    private String getAddress(double lat, double lng){
        String full_address = "null";
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(MapsActivity.instance(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String zipcode  = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                System.out.println("address is not null");
                full_address = address + " " + state + " " + zipcode;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return full_address;
    }

    public void displayThankYou (int type) {
        AlertDialog.Builder dialog;

        //if type == 0, the user is thanked for entering information using the 'give a spot' button
        //otherwise, user is simply thanked for using ParkMatch
        if (type == 0)
             dialog = new AlertDialog.Builder(this)
                    .setTitle("Thank you for using ParkMatch")
                    .setMessage("Your information has been recorded!");
        else
            dialog = new AlertDialog.Builder(this)
                    .setTitle("Thank you for using ParkMatch")
                    .setMessage("We hope your parking experience has been a pleasant one!");

        final AlertDialog alert = dialog.create();
        alert.show();

        new CountDownTimer(2500, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                alert.dismiss();
            }
        }.start();
    }


    //sets local User variable to object returned from database
    private void currentUser(User u){
        this.user = u;
        System.out.println(user.getCarType() + " " + user.getEmail() +
                " " + user.getName() +" "+ user.getPoints() + " " +user.getId());

    }

    //get's the current users information from Database
    private void getCurrentUser(){
        if (user.setCurrentUserId()) {
            Query mRef = mDatabase.child("users").orderByKey().equalTo(user.getId());
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user1 = dataSnapshot.child(user.getId()).getValue(User.class); //return value as User object
                    MapsActivity x = new MapsActivity();
                    x.currentUser(user1);
                    String p = Integer.toString(user1.getPoints());
                    x.instance().setTitle("POINTS: " + p);

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("GETTING THE USER INFORMATION FAILED");

                }
            });
        }

    }

    //gets the other user's point information from the database
    private void giveOtherUserPoints(final String userID){
        Query mRef = mDatabase.child("users").orderByKey().equalTo(userID);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MapsActivity x = new MapsActivity();
                User user2 = dataSnapshot.child(userID).getValue(User.class); //return value as User object
                x.setOtherUserPoints(user2);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("UNABLE TO RETRIEVE OTHER USER INFORMATION");
            }
        });

    }

    private void setOtherUserPoints(User u){
        if (u != null) {
            u.addPoints();
            mDatabase.child("users").child(u.getId()).child("points").setValue(u.getPoints());
            System.out.println("USER IS NOT NULL");
        }
        else
            System.out.println("USER IS NULL");
    }

    //Adds or Removes points from the user. Returns false if the wrong string is passed in.
    private static boolean pointsManager(String manage) {
        //Gives user 1 point for giving a spot
        if (manage.equals("add")) {
            user.addPoints();
            String id = user.getId();
            mDatabase.child("users").child(id).child("points").setValue(user.getPoints());
            System.out.println("POINT ADDED! " + user.getName());
            return true;
        }else if(manage.equals("remove")){
            user.subPoints();
            String id = user.getId();
            mDatabase.child("users").child(id).child("points").setValue(user.getPoints());
            System.out.println("POINT REMOVED! " + user.getName());
            return true;
        }

        return false;
    }


}
