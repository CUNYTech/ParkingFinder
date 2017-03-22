package cunycodes.parkmatch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    //Also add this to the first line in your MainActivity so you can receive results–
    private final int REQUEST_CODE_PLACEPICKER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //sets up button Leaving
        Button gotoButton = (Button) findViewById(R.id.Leaving);
        gotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
    public void SearchParking(View view) {

        mMap = G_googleMap;

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

            latitude = latitude + Nextblock;

            Nextblock = Nextblock + 0.0012;


            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            // Show the current location in Google Map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Zoom in the Google Map
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!"));
        }
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

        String key = mDatabase.child("available_spots").push().getKey();
        AvailableSpot newAvailableSpot = new AvailableSpot(longitude, latitude);
        
        mDatabase.child("available_spots").child(key).setValue(newAvailableSpot);
    }

    //And we also need to override a function so in the main activity, we will be able to receive results —
    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PLACEPICKER && resultCode == RESULT_OK) {
            displaySelectedPlaceFromPlacePicker(data);
        }
    }
}


