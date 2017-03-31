package cunycodes.parkmatch;

public class AvailableSpot {
    private double longitude, latitude;
    private int hourLeaving, minLeaving;
    private String timeLeaving;

    public AvailableSpot () {

    }

    public AvailableSpot (double longitude, double latitude, int hourLeaving, int minLeaving) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.hourLeaving = hourLeaving;
        this.minLeaving = minLeaving;
        this.timeLeaving = Integer.toString(hourLeaving)+":"+Integer.toString(minLeaving);
    }

    public double getLongitude () {
        return this.longitude;
    }

    public double getLatitude () {
        return this.latitude;
    }

    public int getHourLeaving () { return this.hourLeaving; }

    public int getMinLeaving() { return minLeaving; }

    public String getTimeLeaving() { return timeLeaving; }

    public void setLongitude (double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude (double latitude) {
        this.latitude = latitude;
    }

    public void setHourLeaving (int hourLeaving) { this.hourLeaving = hourLeaving; }

    public void setMinLeaving(int minLeaving) { this.minLeaving = minLeaving; }

    public void setTimeLeaving(String timeLeaving) { this.timeLeaving = timeLeaving; }
}
