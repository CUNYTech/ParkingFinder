package cunycodes.parkmatch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.CountDownTimer;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RetrieveAvailable {
    private GeoLocation requested;
    private HashMap<String, GeoLocation> availableSpots;
    private int totalAvailable;
    AvailableSpot selected;

    public RetrieveAvailable (GeoLocation requested) {
        this.requested = requested;
        this.selected = new AvailableSpot();
        this.availableSpots = new HashMap<String, GeoLocation>();
        this.totalAvailable = 0;
    }

    public void setSelected (AvailableSpot selected) {
        this.selected = selected;
    }

    public AvailableSpot getSelected() {
        return this.selected;
    }

    public void retrieveAvailableSpots (DatabaseReference mDatabase) {
        final GeoFire geoFire = new GeoFire(mDatabase.child("geofire_locations").child("available"));
        // creates a new query around [latitude, longitude] with a radius of 0.5 kilometers
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(requested.latitude, requested.longitude), 0.5);
        MapsActivity.mMap.clear();
        displayRequestedSpot();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            //The location of a key now matches the query criteria
            public void onKeyEntered(String key, GeoLocation location) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                availableSpots.put(key, location);
                totalAvailable++;
                displayAvailableSpot(key, location);
            }

            //The location of a key no longer matches the query criteria
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
                            availableSpots.remove(key);
                            totalAvailable--;

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

        //If no available parking spots have been found after ten seconds, a popup appears to ask the user to either wait or choose a new location to park at
        new CountDownTimer(10000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (totalAvailable == 0) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.instance())
                            .setTitle("No Parking Spots Found")
                            .setMessage("Please continue to wait or modify the location of your requested parking spot!")
                            .setPositiveButton("Wait", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setNegativeButton("New Spot", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    MapsActivity.searchingClicked = true;
                                    MapsActivity.leavingClicked = false;
                                    MapsActivity.instance().startPlacePickerActivity();
                                }
                            });

                    final AlertDialog alert = dialog.create();
                    alert.show();
                }
            }
        }.start();
    }

    public void displayRequestedSpot () {
        //adds the requested marker
        Marker requestedSpot = (MapsActivity.mMap).addMarker(new MarkerOptions().position(new LatLng(requested.latitude, requested.longitude)).title("Requested").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        requestedSpot.showInfoWindow();
        (MapsActivity.mMap).moveCamera(CameraUpdateFactory.newLatLng(requestedSpot.getPosition()));
        // Zoom in the Google Map
        (MapsActivity.mMap).animateCamera(CameraUpdateFactory.zoomTo(13));
    }

    public void displayAvailableSpot (String key, GeoLocation selected) {
        final String availableKey = key;
        //adds the available marker
        Marker availableSpot = (MapsActivity.mMap).addMarker(new MarkerOptions().position(new LatLng(selected.latitude, selected.longitude)).title("Available").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        (MapsActivity.mMap).animateCamera(CameraUpdateFactory.zoomTo(13));

        (MapsActivity.mMap).setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTitle().equals("Available")) { // if marker source is clicked
                    System.out.println("Available marker clicked");
                    //Set selected member variable to appropriate available spot location and display dialog once data is properly set
                    setSelectedByMarkerClick(availableKey);
                }
                return true;
            }
        });
    }

    //Sets the member variable 'selected' equal to the instance of AvailableSpot that corresponds to the available spot indicated by the selected marker
    //before displaying appropriate dialog
    private void setSelectedByMarkerClick (final String key) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query availQuery = mDatabase.child("available_spots").orderByKey().equalTo(key);

        availQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AvailableSpot s = dataSnapshot.child(key).getValue(AvailableSpot.class);
                setSelected(s);
                //Display dialog only once data is appropriately set
                displayConfirmationDialog();
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println(databaseError);
                }
            });
    }

    //Call method in MapsActivity to confirm that user would like to park in the location of the clicked available spot marker
    private void displayConfirmationDialog() {
        MapsActivity.instance().SelectLocationMessage(getSelected().getLatitude(), getSelected().getLongitude());
        return;
    }
}
