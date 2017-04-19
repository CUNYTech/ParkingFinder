package cunycodes.parkmatch;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

public class RequestedSpot {
    private double longitude, latitude; //coordinates of destination
    private int hourParking, minParking;
    private String timeParking; //Time When you need the spot
    private GeoLocation emptySpot;

    public RequestedSpot () {
    }

    public RequestedSpot (double longitude, double latitude, int hourParking, int minParking) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.hourParking = hourParking;
        this.minParking = minParking;
        this.timeParking = Integer.toString(hourParking)+":"+Integer.toString(minParking);
        this.emptySpot = new GeoLocation(latitude, longitude);
    }

    public double getLongitude () {return this.longitude;}

    public double getLatitude () { return this.latitude; }

    public int getHourParking () { return this.hourParking; }

    public int getMinParking () { return minParking; }

    public String getTimeParking () { return timeParking; }

    public void setLongitude (double longitude) { this.longitude = longitude; }

    public void setLatitude (double latitude) { this.latitude = latitude; }

    public void setHourParking (int hourParking) { this.hourParking = hourParking; }

    public void setMinParking (int minParking) { this.minParking = minParking; }

    public void setTimeParking (String timeParking) { this.timeParking = timeParking; }

    public void writeGeofireLocationToDatabase (DatabaseReference mDatabase, String key) {
        GeoFire geoFire = new GeoFire(mDatabase.child("geofire_locations").child("requested")); //creates a directory called "Geofire Locations" in available spots
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