package cunycodes.parkmatch;

public class RequestedSpot {
    private double longitude, latitude;
    private int hourParking, minParking;
    private String timeParking;

    public RequestedSpot () {

    }

    public RequestedSpot (double longitude, double latitude, int hourParking, int minParking) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.hourParking = hourParking;
        this.minParking = minParking;
        this.timeParking = Integer.toString(hourParking)+":"+Integer.toString(minParking);
    }

    public double getLongitude () {
        return this.longitude;
    }

    public double getLatitude () {
        return this.latitude;
    }

    public int getHourParking () { return this.hourParking; }

    public int getMinParking () { return minParking; }

    public String getTimeParking () { return timeParking; }

    public void setLongitude (double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude (double latitude) {
        this.latitude = latitude;
    }

    public void setHourParking (int hourParking) { this.hourParking = hourParking; }

    public void setMinParking (int minParking) { this.minParking = minParking; }

    public void setTimeParking (String timeParking) { this.timeParking = timeParking; }
}
