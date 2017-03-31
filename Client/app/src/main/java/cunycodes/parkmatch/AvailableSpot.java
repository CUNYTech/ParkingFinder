package cunycodes.parkmatch;

/**
 * Created by James on 3/15/2017.
 */

public class AvailableSpot {
    private double longitude, latitude;

    public AvailableSpot () {

    }

    public AvailableSpot (double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude () {
        return this.longitude;
    }

    public double getLatitude () {
        return this.latitude;
    }

    public void setLongitude (double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude (double latitude) {
        this.latitude = latitude;
    }
}
