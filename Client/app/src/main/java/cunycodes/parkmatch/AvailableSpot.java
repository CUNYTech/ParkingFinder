package cunycodes.parkmatch;

import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static cunycodes.parkmatch.MapsActivity.mDatabase;


public class AvailableSpot {
    private double longitude, latitude;
    private int hourLeaving, minLeaving;
    private String timeLeaving;
    private GeoLocation emptySpot;
    private String User_Id;
    FirebaseUser cUser;

    //private DatabaseReference rootRef; //required to create geofire object

    public AvailableSpot () {

    }

    public AvailableSpot (double latitude, double longitude, int hourLeaving, int minLeaving) {
        //PASS AS LATITUDE AND LONGITUDE not vice versa
        emptySpot = new GeoLocation(latitude, longitude);
        setUserId();
        // Add Geolocation of empty space to database
        //rootRef = FirebaseDatabase.getInstance().getReference();
        String geoKey = mDatabase.child("GeoFire Locations").push().getKey();
        GeoFire geoFire = new GeoFire(mDatabase.child("GeoFire Locations").child(geoKey)); //creates a directory called "Geofire Locations" in available spots
        geoFire.setLocation(getUser_Id(), emptySpot);

       //this.longitude = longitude;
        //this.latitude = latitude;
        //this.hourLeaving = hourLeaving;
       // this.minLeaving = minLeaving;
        this.timeLeaving = Integer.toString(hourLeaving)+":"+Integer.toString(minLeaving);
        //String key = mDatabase.child("user").push().getKey(); //subdirectory in User directory
        User user = new User("name", cUser.getDisplayName(),cUser.getEmail(), cUser.getUid());
        user.setTimeLeaving(hourLeaving, minLeaving);
        mDatabase.child("Users").child(getUser_Id()).setValue(user);
    }

    public double getLongitude () {
        return this.longitude;
    }

    private void setUserId(){
        FirebaseAuth auth;
        auth = FirebaseAuth.getInstance();
        cUser = auth.getCurrentUser();
        User_Id = cUser.getUid(); //CHECK FOR NULL POINTER so app doesn't crash
    }

    public String getUser_Id(){return this.User_Id;}
    public double getLatitude () {
        return this.latitude;
    }

    public int getHourLeaving () { return this.hourLeaving; }

    public int getMinLeaving() { return minLeaving; }

    public String getTimeLeaving() { return timeLeaving; }

    public void setLongitude (double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude (double latitude) {
        this.latitude = latitude;
    }

    public void setHourLeaving (int hourLeaving) { this.hourLeaving = hourLeaving; }

    public void setMinLeaving(int minLeaving) { this.minLeaving = minLeaving; }

    public void setTimeLeaving(String timeLeaving) { this.timeLeaving = timeLeaving; }
}
