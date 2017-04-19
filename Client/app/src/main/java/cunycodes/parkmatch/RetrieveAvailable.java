package cunycodes.parkmatch;

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

import java.util.ArrayList;

public class RetrieveAvailable {
    private GeoLocation requested, chosen;
    private ArrayList<GeoLocation> selected;
    private int totalAvailable;

    public RetrieveAvailable (GeoLocation requested) {
        this.requested = requested;
        this.chosen = null;
        this.selected = new ArrayList<GeoLocation>();
        this.totalAvailable = 0;
    }

    public GeoLocation getChosen () {
        return chosen;
    }


    public void retrieveAvailableSpots (DatabaseReference mDatabase) {
        final GeoFire geoFire = new GeoFire(mDatabase.child("geofire_locations").child("available"));
        // creates a new query around [latitude, longitude] with a radius of 0.5 kilometers
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(requested.latitude, requested.longitude), 0.5);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            //The location of a key now matches the query criteria.
            public void onKeyEntered(String key, GeoLocation location) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                selected.add(location);
                totalAvailable++;
                displayAvailableSpot(key, location);
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
                geoQuery.removeAllListeners();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });
    }

    public void displayAvailableSpot (String key, GeoLocation selected) {
        final String availableKey = key;
        //adds the requested marker
        Marker requestedSpot = (MapsActivity.mMap).addMarker(new MarkerOptions().position(new LatLng(requested.latitude, requested.longitude)).title("Requested").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        //adds the available marker
        Marker availableSpot = (MapsActivity.mMap).addMarker(new MarkerOptions().position(new LatLng(selected.latitude, selected.longitude)).title("Available"));
        requestedSpot.showInfoWindow();
        //(MapsActivity.mMap).animateCamera(CameraUpdateFactory.newLatLng(requested.getPosition()), 250, null);
        (MapsActivity.mMap).moveCamera(CameraUpdateFactory.newLatLng(requestedSpot.getPosition()));
        // Zoom in the Google Map
        (MapsActivity.mMap).animateCamera(CameraUpdateFactory.zoomTo(13));

        (MapsActivity.mMap).setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getTitle().equals("Available")) { // if marker source is clicked
                    System.out.println("Available marker clicked");
                    MapsActivity.instance().SelectLocationMessage(marker.getPosition().latitude, marker.getPosition().longitude, availableKey); //calls the dialog from map activity
                    chosen = new GeoLocation(marker.getPosition().latitude, marker.getPosition().longitude);
                }
                return true;
            }
        });
    }
}
