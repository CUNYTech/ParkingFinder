package cunycodes.parkmatch;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by emmakimlin on 3/12/17.
 */

public class LeaveSpaceRequest {
        /* Constructor takes the latitude and longitude of the user's car stored in a GeoLocation object. */
        public LeaveSpaceRequest(GeoLocation car_location, String time_leaving) {
            rootRef = FirebaseDatabase.getInstance().getReference();
            auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            user_id = user.getUid();
            geoFire = new GeoFire(rootRef);
            putEmptySpaceInDatabase(car_location, user_id, time_leaving);
        }

        private static final String GEO_FIRE_DB = "https://parkmatch-3a7b4.firebaseio.com/available_spots";
        private GeoFire geoFire;
        private FirebaseAuth auth;
        private DatabaseReference rootRef;
        private String user_id;

        /* Store this car location into database using username as key */
        private void putEmptySpaceInDatabase(GeoLocation car_location, String username, String time_leaving) {
            // Add Geolocation of empty space to database
             geoFire.setLocation(username, car_location, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        System.err.println("There was an error saving the location to GeoFire: " + error);
                    } else {
                        System.out.println("Location saved on server successfully!");
                    }
                }
            });
            // Add time when this spot will be empty (time_leaving)
            
        }
}