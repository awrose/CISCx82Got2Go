package com.example.ciscx82got2go;

public class LocationInfo {
    //save location data to firebase

    //name of the building that the bathroom is in
    private String locationName;

    //lecture hall, cafe, restaurant, etc...
    private String locationType;

    //comments that other users leave about the bathroom
    //private String[] locationComments;

    //the latitude of the location
    private float locationLat;

    //the longitude of the location
    private float locationLong;

    //users ratings of the bathroom
    //private int[] locationRatings;

    //quick description about the location - free? accessible? what genders are availabe?
    private String locationDescription;

    //avg ratings
    private int ratingAvg;

    // an empty constructor is
    // required when using
    // Firebase Realtime Database.
    public LocationInfo() {

    }

    // created getter and setter methods
    // for all our variables.
    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    /*public String[] getLocationComments() {
        return locationComments;
    }

    public void setLocationComments(String comment){
        this.locationComments[this.locationComments.length - 1] = comment;
    }*/

    public float getlocationLat(){
        return locationLat;
    }

    public void setLocationLat(float lat){
        this.locationLat = lat;
    }

    public float getLocationLong(){
        return locationLong;
    }

    public void setLocationLong(float longitude){
        this.locationLong = longitude;
    }

    /*public int[] getlocationRatings(){
        return locationRatings;
    }

    public void setLocationRatings(int rating){
        this.locationRatings[this.locationRatings.length - 1] = rating;
    }*/

    public String getLocationDescription(){
        return locationDescription;
    }

    public void setLocationDescription(String description){
        this.locationDescription = description;
    }

    /*public int getratingAvg(){
        int sum = 0;
        for(int i = 0; i<locationRatings.length; i++){
            sum += locationRatings[i];
        }

        return sum/locationRatings.length;
    }*/

}
