package cunycodes.parkmatch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

import com.firebase.geofire.GeoLocation;
import java.io.File;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    //a variable in order to receive results for pacePicker
    private final int REQUEST_CODE_PLACEPICKER = 1;

    // so we can switch from gotoParking to displaySelectedPlaceFromPlacePicker  when calling onActivityResult
    int ButtonSwitcher=0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        //return true;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_account:
                Toast.makeText(MapsActivity.this, "Account Settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.nav_settings:
                Toast.makeText(MapsActivity.this, "Settings", Toast.LENGTH_LONG).show();
                return true;
            case R.id.nav_logout:

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

                Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static boolean deleteDir(File dir)
        {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //////////////////////////////////////////////////////////////////////////////////////////

        //sets up button for PlacePicker Leaving
        Button LeavingButton = (Button) findViewById(R.id.Leaving);
        LeavingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonSwitcher=1;
                startPlacePickerActivity();
            }
        });

        //sets up button for PlacePicker Leaving
        Button gotoParkingButton = (Button) findViewById(R.id.SearchParking);
        gotoParkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonSwitcher=2;
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


    GoogleMap G_googleMap;
    @Override
    public void onMapReady(GoogleMap googleMap) {

        //makes maps a global variable for later use
        G_googleMap=googleMap;;
        mMap = googleMap;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

        if(myLocation !=null) {
            // Get latitude of the current location
            double latitude = myLocation.getLatitude();

            // Get longitude of the current location
            double longitude = myLocation.getLongitude();

            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            // Show the current location in Google Map
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Zoom in the Google Map
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!"));
        }
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///A search bar, in case its usefull later
   /* public void SearchPlace(View view){
        EditText Loacation_tf = (EditText)findViewById(R.id.address);
        String location = Loacation_tf.getText().toString();

        List<android.location.Address> addressList =null;
        if(location != null || !location.equals("")){
            Geocoder geocoder=new Geocoder(this);
            try {
               addressList= geocoder.getFromLocationName(location,1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            android.location.Address address= addressList.get(0);

            LatLng BoB =new LatLng(address.getLatitude(),address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(BoB).title("Marker BoB"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(BoB));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }

    }*/
    // place a marker in a loction close to the users current position

    double Nextblock= 0.0012;
    public void gotoParking(Intent data) {

        Place placeSelected =  PlacePicker.getPlace(this, data);

        String name = placeSelected.getName().toString();
        String address = placeSelected.getAddress().toString();

        //ger longitude in as an string; maybe useful later
        // String longitude= placeSelected.getLatLng().toString();

        // Get latitude of the currentcar location
        double latitude = placeSelected.getLatLng().latitude;

        // Get longitude of the current car location
        double longitude = placeSelected.getLatLng().longitude;

        /////////////////////////////////////////////

        this.LookingForSpotsNear (latitude, longitude);


        ///////////////////////////////////////////////

        TextView enterCurrentLocation = (TextView) findViewById(R.id.SearchParking);
        enterCurrentLocation.setText("Found one near "+ address );
            latitude = latitude + Nextblock;

            Nextblock = Nextblock + 0.0012;


            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            mMap = G_googleMap;
            // Show the current location in Google Map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Zoom in the Google Map
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Open Parking Spot Here"));
        }



    //Now we need to implement startPlacePickerActivity() and a couple other functions–
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

    private void displaySelectedPlaceFromPlacePicker(Intent data) {
        Place placeSelected =  PlacePicker.getPlace(this, data);

        String name = placeSelected.getName().toString();
        String address = placeSelected.getAddress().toString();

        //ger longitude in as an string; maybe useful later
       // String longitude= placeSelected.getLatLng().toString();

        // Get latitude of the currentcar location
        double latitude = placeSelected.getLatLng().latitude;

        // Get longitude of the current car location
        double longitude = placeSelected.getLatLng().longitude;

        /////////////////////////////////////////////

        this.writeToDatabase (latitude, longitude);


        ///////////////////////////////////////////////

        TextView enterCurrentLocation = (TextView) findViewById(R.id.Leaving);
        enterCurrentLocation.setText("Your Car is at "+name + ", " + address + ", coordinates= " +latitude +", " +longitude);
    }

    private void writeToDatabase(double longitude, double latitude) {
        String time_leaving = "10:15"; //CHANGE THIS TO GET TIME WHEN USER WANTS TO LEAVE
        LeaveSpaceRequest leave_space = new LeaveSpaceRequest(new GeoLocation(latitude, longitude), String time_leaving);
    }



    private void LookingForSpotsNear(double longitude, double latitude) {

        AvailableSpot newAvailableSpot = new AvailableSpot(longitude, latitude);
        mDatabase.child("Requested_Spot_Area").setValue(newAvailableSpot);
    }

    //And we also need to override a function so in the main activity, we will be able to receive results —
    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PLACEPICKER && resultCode == RESULT_OK) {
            if(ButtonSwitcher==1){
            displaySelectedPlaceFromPlacePicker(data);}

            if(ButtonSwitcher==2){
                gotoParking(data);}
            }

        }
    }



