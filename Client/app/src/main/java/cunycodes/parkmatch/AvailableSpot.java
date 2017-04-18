package cunycodes.parkmatch;

import android.app.AlertDialog;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class AvailableSpot {
    private double longitude, latitude;
    private int hourLeaving, minLeaving;
    private int dayOfYearLeaving; // The calendar year begins with day 1
    private int yearLeaving;
    private GeoLocation emptySpot;
    private String userId;

    public AvailableSpot (double longitude, double latitude, int hourLeaving, int minLeaving, int thedayOfYearLeaving,
                          int theYearLeaving) {
        //PASS AS LATITUDE AND LONGITUDE not vice versa
        this.emptySpot = new GeoLocation(latitude, longitude);
        setUserId();
        // Add Geolocation of empty space to database
        //rootRef = FirebaseDatabase.getInstance().getReference();
        //String geoKey = mDatabase.child("GeoFire Locations").push().getKey();

        this.longitude = longitude;
        this.latitude = latitude;
        this.hourLeaving = hourLeaving;
        this.minLeaving = minLeaving;
        this.dayOfYearLeaving = thedayOfYearLeaving;
        this.yearLeaving = thedayOfYearLeaving;
    }

    public double getLongitude () { return this.longitude; }

    public String getUserId() { return this.userId; }

    public double getLatitude () { return this.latitude; }

    public int getHourLeaving () { return this.hourLeaving; }

    public int getMinLeaving() { return this.minLeaving; }

    public int getDayOfYearLeaving() { return this.dayOfYearLeaving; }

    public int getYearLeaving() { return this.yearLeaving; }

    public void setLongitude (double longitude) { this.longitude = longitude; }

    public void setLatitude (double latitude) { this.latitude = latitude; }

    public void setHourLeaving (int hourLeaving) { this.hourLeaving = hourLeaving; }

    public void setMinLeaving(int minLeaving) { this.minLeaving = minLeaving; }

    public void setDayOfYearLeaving(int theDayOfLeaving) {this.dayOfYearLeaving = theDayOfLeaving; }

    public void setYearLeaving(int theYearLeaving) {this.yearLeaving = theYearLeaving; }


    private void setUserId() {
        FirebaseUser cUser;
        FirebaseAuth auth;
        auth = FirebaseAuth.getInstance();
        cUser = auth.getCurrentUser();
        userId = cUser.getUid(); //CHECK FOR NULL POINTER so app doesn't crash
    }

    public void writeGeofireLocationToDatabase (DatabaseReference mDatabase, String key) {
        //creates a directory called "Geofire Locations" in available spots
        GeoFire geoFire = new GeoFire(mDatabase.child("geofire_locations").child("available"));
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
