package cunycodes.parkmatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

public class RequestedSpot {
    private double dlongitude, dlatitude; //coordinates of destination
    private int hourParking, minParking;
    private String timeParking; //Time When you need the spot
    private GeoLocation emptySpot;
    private double slongitude, slatitude; //coordinates of picked available spot
    private LatLng pickedSpot; //object that hold both longitude and latitude

    public RequestedSpot () {
    }

    public RequestedSpot (double longitude, double latitude, int hourParking, int minParking) {
        this.dlongitude = longitude;
        this.dlatitude = latitude;
        this.hourParking = hourParking;
        this.minParking = minParking;
        this.timeParking = Integer.toString(hourParking)+":"+Integer.toString(minParking);
        this.emptySpot = new GeoLocation(latitude, longitude);
    }

    public double getLongitude () {return this.dlongitude;}

    public double getLatitude () {
        return this.dlatitude;
    }

    public double getPickedLat () {return this.slatitude;}

    public double getPickedLon () {return this.slongitude;}

    public LatLng getPickedLocation () {pickedSpot = new LatLng(slatitude,slongitude); return this.pickedSpot;}

    public int getHourParking () { return this.hourParking; }

    public int getMinParking () { return minParking; }

    public String getTimeParking () { return timeParking; }

    public void setLongitude (double longitude) {
        this.dlongitude = longitude;
    }

    public void setLatitude (double latitude) {
        this.dlatitude = latitude;
    }

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
        this.retrieveAvailableSpots(mDatabase);
    }

    public void retrieveAvailableSpots (DatabaseReference mDatabase) {
        System.out.println("IAM IN RETRIEVE AVAILABLE SPOTS");
        final GeoFire geoFire = new GeoFire(mDatabase.child("geofire_locations").child("available"));
        // creates a new query around [latitude, longitude] with a radius of 0.5 kilometers
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(this.dlatitude, this.dlongitude), 3.2);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            //The location of a key now matches the query criteria.
            public void onKeyEntered(String key, GeoLocation location) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                //adds the requested marker
                Marker requested = (MapsActivity.mMap).addMarker(new MarkerOptions().position(new LatLng(getLatitude(), getLongitude())).title("Requested").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                //adds the available marker
                Marker available = (MapsActivity.mMap).addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title("Available"));
                requested.showInfoWindow();
                //(MapsActivity.mMap).animateCamera(CameraUpdateFactory.newLatLng(requested.getPosition()), 250, null);
                (MapsActivity.mMap).moveCamera(CameraUpdateFactory.newLatLng(requested.getPosition()));
                // Zoom in the Google Map
                (MapsActivity.mMap).animateCamera(CameraUpdateFactory.zoomTo(13));

                (MapsActivity.mMap).setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
                {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if(marker.getTitle().equals("Available")) { // if marker source is clicked
                            System.out.println("Available marker clicked");
                            //ADD CODE FOR A POPUP DIALOG ASKING IF USER WOULD LIKE TO PARK IN LOCATION OF MARKER
                            slatitude = marker.getPosition().latitude;
                            slongitude = marker.getPosition().longitude;
                        }
                        return true;
                    }

                });
            }

            //The location of a key no longer matches the query criteria.
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
                geoFire.getLocation(key, new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        if (location != null) {
                            System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                            //remove marker for available spot if it no longer meets query requirements
                            Marker unavailableSpot = (MapsActivity.mMap).addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
                            unavailableSpot.remove();
                        } else {
                            System.out.println(String.format("There is no location for key %s in GeoFire", key));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("There was an error getting the GeoFire location: " + databaseError);
                    }
                });
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });
    }
}
