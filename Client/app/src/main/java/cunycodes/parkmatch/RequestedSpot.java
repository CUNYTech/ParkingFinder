package cunycodes.parkmatch;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;

public class RequestedSpot {
    private double longitude, latitude; //coordinates of requested parking spot
    private int hourRequested, minRequested; //hour and minute of request, set to current time by default
    private String timeRequested;
    private GeoLocation emptySpot;

    public RequestedSpot () {
    }

    public RequestedSpot (double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;

        Calendar c = Calendar.getInstance();
        int hourReq = c.get(Calendar.HOUR_OF_DAY);
        int minReq = c.get(Calendar.MINUTE);

        this.hourRequested = hourReq;
        this.minRequested = minReq;
        this.timeRequested = Integer.toString(hourRequested)+":"+Integer.toString(minRequested);
        this.emptySpot = new GeoLocation(latitude, longitude);
    }

    public double getLongitude () {return this.longitude;}

    public double getLatitude () { return this.latitude; }

    public int getHourRequested () { return this.hourRequested; }

    public int getMinRequested () { return minRequested; }

    public String getTimeRequested () { return timeRequested; }

    public void setLongitude (double longitude) { this.longitude = longitude; }

    public void setLatitude (double latitude) { this.latitude = latitude; }

    public void setHourRequested (int hourRequested) { this.hourRequested = hourRequested; }

    public void setMinRequested (int minRequested) { this.minRequested = minRequested; }

    public void setTimeRequested (String timeRequested) { this.timeRequested = timeRequested; }

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


