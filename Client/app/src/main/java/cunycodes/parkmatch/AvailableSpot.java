package cunycodes.parkmatch;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;


public class AvailableSpot {
    private double longitude, latitude;
    private int hourLeaving, minLeaving;
    private String timeLeaving;
    private GeoLocation emptySpot;
    private String userId;
    public String carType = "unavailable";

    public AvailableSpot () {
    }

    public AvailableSpot (double longitude, double latitude, int hourLeaving, int minLeaving) {
        //PASS AS LATITUDE AND LONGITUDE not vice versa
        this.emptySpot = new GeoLocation(latitude, longitude);
        setUserId();
        setCarType();
        // Add Geolocation of empty space to database
        //rootRef = FirebaseDatabase.getInstance().getReference();
        //String geoKey = mDatabase.child("GeoFire Locations").push().getKey();

        this.longitude = longitude;
        this.latitude = latitude;
        this.hourLeaving = hourLeaving;
        this.minLeaving = minLeaving;
        this.timeLeaving = Integer.toString(hourLeaving)+":"+Integer.toString(minLeaving);

    }

    public double getLongitude () { return this.longitude; }

    public String getUserId() { return this.userId; }

    public String getCarType() {return carType;}

    public void setCarType() {
        carType = MapsActivity.user.getCarType();
    }

    public double getLatitude () { return this.latitude; }

    public int getHourLeaving () { return this.hourLeaving; }

    public int getMinLeaving() { return this.minLeaving; }

    public String getTimeLeaving() { return this.timeLeaving; }

    public void setLongitude (double longitude) { this.longitude = longitude; }

    public void setLatitude (double latitude) { this.latitude = latitude; }

    public void setHourLeaving (int hourLeaving) { this.hourLeaving = hourLeaving; }

    public void setMinLeaving(int minLeaving) { this.minLeaving = minLeaving; }

    public void setTimeLeaving(String timeLeaving) { this.timeLeaving = timeLeaving; }

    private void setUserId() {
        FirebaseUser cUser;
        FirebaseAuth auth;
        auth = FirebaseAuth.getInstance();
        cUser = auth.getCurrentUser();
        userId = cUser.getUid(); //CHECK FOR NULL POINTER so app doesn't crash
    }

    public void writeGeofireLocationToDatabase (DatabaseReference mDatabase, String key) {
        GeoFire geoFire = new GeoFire(mDatabase.child("geofire_locations").child("available")); //creates a directory called "Geofire Locations" in available spots
        geoFire.setLocation(key, emptySpot, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });

    }
}
