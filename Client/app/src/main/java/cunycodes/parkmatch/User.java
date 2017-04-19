package cunycodes.parkmatch;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class User {
    private String name, email, id, carType;
    private int points =0;
    private final int pointsToAdd = 1;
    private final int pointsToSub = 1;

    //Initializes variables to current user information
    public User () {

    }
    //when user first registers
    public User (String name, String carType, String email, String id) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.carType = carType;
        points = 2;
    }

    //gets the current User's Id from database. if successful returns true, otherwise returns false
    public boolean setCurrentUserId(){

        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        if (cUser != null){//CHECK FOR NULL POINTER so app doesn't crash}
            id = cUser.getUid();
            return true;
        }
        return false;
    }

    public String getName () {
        return this.name;
    }

    public String getCarType(){return this.carType;}

    public String getEmail () {
        return this.email;
    }

    public String getId () { return this.id; }

    public int getPoints () { return this.points; }

    public void setPoints(int p) {points = p;}

    public void addPoints(){

        points = points + pointsToAdd;}

    public void subPoints(){if(points > 0) points = points - pointsToSub;}

    public void setCarType(String carType){this.carType = carType;}

    public void setName (String name) {
        this.name = name;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public void setId (String id) { this.id = id; }
}
