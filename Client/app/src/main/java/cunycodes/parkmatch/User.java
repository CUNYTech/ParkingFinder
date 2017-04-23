package cunycodes.parkmatch;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class User {
    private String name, email, id, carType;
    private int points = 0;
    private final int pointsToAdd = 1;
    private final int pointsToSub = 1;

    public User () {

    }

    //when user first registers
    public User (String name, String email, String id,  String carType) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.carType = carType;
        this.points = 2;
    }

    //Retrieve current user's user ID from database - returns true if successful, otherwise returns false
    public boolean setCurrentUserId(){

        FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
        if (cUser != null){ //CHECK FOR NULL POINTER so app doesn't crash}
            id = cUser.getUid();
            return true;
        }
        return false;
    }

    public String getName () { return this.name; }

    public String getCarType() { return this.carType;}

    public String getEmail () { return this.email; }

    public String getId () { return this.id; }

    public int getPoints () { return this.points; }

    public void setPoints (int points) { this.points = points; }

    public void addPoints(){ this.points = this.points + this.pointsToAdd; }

    public void subPoints(){ if (this.points > 0) this.points = this.points - this.pointsToSub;}

    public void setCarType (String carType) { this.carType = carType; }

    public void setName (String name) { this.name = name; }

    public void setEmail (String email) {
        this.email = email;
    }

    public void setId (String id) { this.id = id; }
}