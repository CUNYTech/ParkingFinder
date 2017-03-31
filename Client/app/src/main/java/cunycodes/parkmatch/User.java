package cunycodes.parkmatch;

public class User {
    private String name, userName, email, id;
    public String time_leaving;

    public User () {

    }
    public User(String id){
        this.id = id;
    }
    public User (String name, String userName, String email, String id) {
        this.name = name;
        this.userName = userName;
        this.email = email;
        this.id = id;
    }

    public String getName () {
        return this.name;
            }

    public String getUserName () {
        return this.userName;
    }

    public String getEmail () {
        return this.email;
    }

    public String getId () { return this.id; }

    public void setName (String name) {
        this.name = name;
    }

    public void setUserName (String userName) {
        this.userName = userName;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public void setId (String id) { this.id = id; }

    public void setTimeLeaving(int h, int m) {
       time_leaving = Integer.toString(h)+":"+Integer.toString(m);
        }

    public String getTime_leaving(){return time_leaving;}
}
