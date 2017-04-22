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

/*
    public void retrieveAvailableSpots (DatabaseReference mDatabase) {
        System.out.println("IAM IN RETRIEVE AVAILABLE SPOTS");
        final GeoFire geoFire = new GeoFire(mDatabase.child("geofire_locations").child("available"));
        // creates a new query around [latitude, longitude] with a radius of 0.5 kilometers
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(this.dlatitude, this.dlongitude), 3.2);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            //The location of a key now matches the query criteria.
            public void onKeyEntered(final String key, GeoLocation location) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                keys.add(key);
                getSpotsInfo();
                //adds the requested marker
                Marker requested = (MapsActivity.mMap).addMarker(new MarkerOptions().position(new LatLng(getLatitude(), getLongitude())).title("Requested").icon(BitmapDescriptorFactory.fromResource(R.drawable.mark2)));
                //adds the available marker
                final Marker available = (MapsActivity.mMap).addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title("Available").icon(BitmapDescriptorFactory.fromResource(R.drawable.mark0)));
                requested.showInfoWindow();
                (MapsActivity.mMap).moveCamera(CameraUpdateFactory.newLatLng(requested.getPosition()));
                (MapsActivity.mMap).animateCamera(CameraUpdateFactory.zoomTo(13));// Zoom in the Google Map

                (MapsActivity.mMap).setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
                {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        if(marker.getTitle().equals("Available")) { // if marker source is clicked
                            System.out.println("Available marker clicked");
                            slatitude = marker.getPosition().latitude; //this doesn't actually assign Slat/slng
                            slongitude = marker.getPosition().longitude;
                            setPickedLatLng(slatitude,slongitude); //this sets our private variables
                            //gives the selected key to mapActivity
                            map.SelectLocationMessage(slatitude,slongitude); //calls the dialog from map activity

                            //iterate over spots to match with key
                            for(AvailableSpot spot : spotsReturned){
                                if(spot.getLatitude() == slatitude && spot.getLongitude() == slongitude)
                                {
                                    selectedSpot = spot;
                                    System.out.println("Selected spot time: " + selectedSpot.getTimeLeaving() + " size: " + spotsReturned.size());
                                }
                            }


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

    //given a set of Latitude and longitude, it returns a string containing an address
    public String getAddress(double lat, double lng){
        String full_address = "null";
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(MapsActivity.instance(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String zipcode  = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                System.out.println("address is not null");
                full_address = address + " " + state + " " + zipcode;
            }
      } catch (IOException e) {
            e.printStackTrace();
      }
        return full_address;
    }

    //delete
    private void printKeys(){
        if (keys.size() > 0){
            for(String k: keys){
                System.out.println(k);
            }
        }
    }

    //sets Local available spot object to the one returned from the database. This object contains information about the
    //selected spot
    public void getSpotsInfo (){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(keys.size() > 0) {
            for(String k: keys) {
                DatabaseReference mRef = database.getReference("Available Spots Attributes").child(k); //reference to Users/id
                mRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        AvailableSpot spot1 = dataSnapshot.getValue(AvailableSpot.class); //return value as User object
                        spotsReturned.add(spot1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("GETTING AVAILABLE SPOT INFORMATION FAILED");

                    }
                });
            }
        }

    }

}
>>>>>>> DesignExperiment */

