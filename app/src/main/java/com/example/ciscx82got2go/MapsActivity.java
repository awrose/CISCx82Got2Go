package com.example.ciscx82got2go;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition cameraPosition;

    private PlacesClient placesClient;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation;

    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;

    private static int REQUEST_CODE = 101;

    Marker newMarker;

    //private ChildEventListener mChildEventListener;


    //to save the map's state
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    //Variable for firebase database
    FirebaseDatabase firebaseDatabase;
    //variable for database reference
    DatabaseReference databaseReference;

    //variable for object class
    LocationInfo locationInfo;

    //FirebaseFirestore db;
    private LocationInfo[] markers;

    private ListView locationsLV;
    ArrayList<LocationInfo> locationInfoList;

    private int id;

    SearchView searchView;




    //final LatLng melbourneLocation = new LatLng(-37.813, 144.962);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //searchView = findViewById(R.id.idSearchView);



        locationInfoList = new ArrayList<>();
        getData();



        //initialize object class variable
        locationInfo = new LocationInfo();

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // ...

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        //searchView = findViewById(R.id.idSearchView);

        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), "AIzaSyA7t_QerDyB-Xuy3CyhC0fjF3U6tFFiDzE");
        placesClient = Places.createClient(this);



        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //assert autocompleteFragment != null;
        //autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS);

        /*autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(-33.880490, 151.184363),
                new LatLng(-33.858754, 151.229596)
        ));*/

        //autocompleteFragment.setCountries("IN");
        
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener(){

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "PLACE: " + place.getName() + " " + place.getId());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(place.getLatLng().latitude,
                                    place.getLatLng().longitude), DEFAULT_ZOOM));

            }
        });




        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    private void getData(){
        databaseReference.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapshot: snapshot.getChildren()){
                    LocationInfo locationInfo = postSnapshot.getValue(LocationInfo.class);
                    locationInfoList.add(locationInfo);
                    //access to name property
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed:" + error.getMessage());
            }
        });

    }




    private void addDataToFirebase(String locationName, String locationDescription, float lat, float longitude, int ratings, int count){
        locationInfo.setLocationName(locationName);
        locationInfo.setLocationDescription(locationDescription);
        locationInfo.setLocationLat(lat);
        locationInfo.setLocationLong(longitude);
        locationInfo.setDatabaseRatings(ratings);
        locationInfo.setDatabaseCount(count);


        databaseReference.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReference.child(locationName).setValue(locationInfo);
                locationInfoList.add(locationInfo);
                Toast.makeText(MapsActivity.this, "bathroom added", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapsActivity.this, "failed to add bathroom" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        //can add markers, add listeners, or move the camera
        this.mMap = googleMap;




        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

            //this will add a bathroom
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                //popup
                final Dialog newBathroom = new Dialog(MapsActivity.this);
                newBathroom.setTitle("Enter Bathroom Info:");
                newBathroom.setContentView(R.layout.new_bathroom_input);
                newBathroom.show();
                //Log.e("myLog", "tapped");

                TextView inputLocationName = newBathroom.findViewById(R.id.idEditLocatinName);
                //String locationName = inputLocationName.getText().toString();

                TextView inputDescription = newBathroom.findViewById(R.id.idEditLocationDescription);
                //String description = inputDescription.getText().toString();

                Button submit = newBathroom.findViewById(R.id.idBtnClose);

                submit.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        String locationName = inputLocationName.getText().toString();
                        String description = inputDescription.getText().toString();

                        if(locationName.length() > 0) {
                            addDataToFirebase(locationName, description, (float) latLng.latitude, (float) latLng.longitude, 0, 0);

                            markerOptions.position(latLng);
                            markerOptions.title(locationName);
                            markerOptions.icon(BitmapFromVector(getApplicationContext(), R.drawable.toilet_svgrepo_com));
                            mMap.addMarker(markerOptions);
                        }
                        //submit all the information to the database
                        //locationName, Description, Lat, Long
                        newBathroom.dismiss();

                        /*markerOptions.position(latLng);
                        markerOptions.title(locationName);
                        markerOptions.icon(BitmapFromVector(getApplicationContext(), R.drawable.toilet_svgrepo_com));
                        mMap.addMarker(markerOptions);*/


                    }
                });
            }
        });




        getLocationPermission();

        //don't know what this does quite yet
        updateLocationUI();

        //get the current location of the device and set the position
        getDeviceLocation();

        //MarkerOptions newMarker = new MarkerOptions();
        //newMarker.position(new LatLng(37.42454954352804, -122.08442900329828)).title("CUSTOM MARKER").icon(BitmapFromVector(getApplicationContext(), R.drawable.toilet_svgrepo_com));
        LocationInfo locationMarkerForTesting = new LocationInfo();
        locationMarkerForTesting.setLocationName("Custom Marker - Test Bathroom 1");
        locationMarkerForTesting.setLocationDescription("Male/Female/Gender Neutral\nOpen 24/7\nNo Key Needed to Access");
        locationMarkerForTesting.setLocationType("Gas Station");
        locationMarkerForTesting.setLocationLat((float) 37.42454954352804);
        locationMarkerForTesting.setLocationLong((float) -122.08442900329828);
        locationInfoList.add(locationMarkerForTesting);

        for(int i = 0; i<locationInfoList.size(); i++){
            newMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(locationInfoList.get(i).getlocationLat(), locationInfoList.get(i).getLocationLong()))
                    .title(locationInfoList.get(i).getLocationName())
                    .icon(BitmapFromVector(getApplicationContext(), R.drawable.toilet_svgrepo_com))
            );
        }




        //add all of the markers within the database
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){

            @Override
            public boolean onMarkerClick(final Marker marker) {
                //get the id of the marker that is clicked, then add a variable to the view, pass in the variable to the view
                String name = marker.getTitle();

                final Dialog locationInfo = new Dialog(MapsActivity.this);
                locationInfo.setTitle("Bathroom");
                locationInfo.setContentView(R.layout.marker1_info);

                TextView locationName = locationInfo.findViewById(R.id.bathroomBuildingName);
                TextView ratings = locationInfo.findViewById(R.id.ratingText);
                TextView locationDescription = locationInfo.findViewById(R.id.buildingDescriptionInfo);

                //now find the index of the bathroom that is clicked

                int index = -1;

                for(int i = 0; i<locationInfoList.size(); i++){
                    String currLocationName = locationInfoList.get(i).getLocationName();
                    if(currLocationName.equalsIgnoreCase(name)){
                        index = i;
                    }
                }

                if(index != -1){
                    //set the text
                    locationName.setText(locationInfoList.get(index).getLocationName());
                    ratings.setText(locationInfoList.get(index).getAvgRatings() + " / 5 ???");
                    locationDescription.setText(locationInfoList.get(index).getLocationDescription());
                }

                Button closeBtn = (Button)locationInfo.findViewById(R.id.closeBtn);

                    //textView.setText(marker.get)

                    closeBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            locationInfo.dismiss();
                        }
                    });

                Button rateBtn = (Button) locationInfo.findViewById(R.id.rateBtn);
                int finalIndex = index;
                rateBtn.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        int count = 0;
                        int ratings = 0;

                        final Dialog ratingPage = new Dialog(MapsActivity.this);
                        ratingPage.setTitle("Bathroom");
                        ratingPage.setContentView(R.layout.ratings_popup);

                        //get each star button
                        ImageButton star1 = (ImageButton)ratingPage.findViewById(R.id.star1);
                        ImageButton star2 = (ImageButton)ratingPage.findViewById(R.id.star2);
                        ImageButton star3 = (ImageButton)ratingPage.findViewById(R.id.star3);
                        ImageButton star4 = (ImageButton)ratingPage.findViewById(R.id.star4);
                        ImageButton star5 = (ImageButton)ratingPage.findViewById(R.id.star5);

                        star1.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View view) {
                                //set ratingstotal to += 1,
                                //set count to ++
                                if(finalIndex != -1) {
                                    locationInfoList.get(finalIndex).setCount();
                                    locationInfoList.get(finalIndex).setRatings(1);

                                    databaseReference.child(locationInfoList.get(finalIndex).getLocationName()).child("count").setValue(locationInfoList.get(finalIndex).getCount());
                                    databaseReference.child(locationInfoList.get(finalIndex).getLocationName()).child("ratings").setValue(locationInfoList.get(finalIndex).getRatings());
                                }

                                //close both
                                ratingPage.dismiss();
                                locationInfo.dismiss();
                            }
                        });

                        star2.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View view) {
                                //set ratingstotal to += 1,
                                //set count to ++
                                if(finalIndex != -1) {
                                    locationInfoList.get(finalIndex).setCount();
                                    locationInfoList.get(finalIndex).setRatings(2);

                                    databaseReference.child(locationInfoList.get(finalIndex).getLocationName()).child("count").setValue(locationInfoList.get(finalIndex).getCount());
                                    databaseReference.child(locationInfoList.get(finalIndex).getLocationName()).child("ratings").setValue(locationInfoList.get(finalIndex).getRatings());
                                }

                                //close both
                                ratingPage.dismiss();
                                locationInfo.dismiss();
                            }
                        });

                        star3.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View view) {
                                //set ratingstotal to += 1,
                                //set count to ++
                                if(finalIndex != -1) {
                                    locationInfoList.get(finalIndex).setCount();
                                    locationInfoList.get(finalIndex).setRatings(3);

                                    databaseReference.child(locationInfoList.get(finalIndex).getLocationName()).child("count").setValue(locationInfoList.get(finalIndex).getCount());
                                    databaseReference.child(locationInfoList.get(finalIndex).getLocationName()).child("ratings").setValue(locationInfoList.get(finalIndex).getRatings());
                                }

                                //close both
                                ratingPage.dismiss();
                                locationInfo.dismiss();
                            }
                        });

                        star4.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View view) {
                                //set ratingstotal to += 1,
                                //set count to ++
                                if(finalIndex != -1) {
                                    locationInfoList.get(finalIndex).setCount();
                                    locationInfoList.get(finalIndex).setRatings(4);

                                    databaseReference.child(locationInfoList.get(finalIndex).getLocationName()).child("count").setValue(locationInfoList.get(finalIndex).getCount());
                                    databaseReference.child(locationInfoList.get(finalIndex).getLocationName()).child("ratings").setValue(locationInfoList.get(finalIndex).getRatings());
                                }

                                //close both
                                ratingPage.dismiss();
                                locationInfo.dismiss();
                            }
                        });

                        star5.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View view) {
                                //set ratingstotal to += 1,
                                //set count to ++
                                if(finalIndex != -1) {
                                    locationInfoList.get(finalIndex).setCount();
                                    locationInfoList.get(finalIndex).setRatings(5);

                                    databaseReference.child(locationInfoList.get(finalIndex).getLocationName()).child("count").setValue(locationInfoList.get(finalIndex).getCount());
                                    databaseReference.child(locationInfoList.get(finalIndex).getLocationName()).child("ratings").setValue(locationInfoList.get(finalIndex).getRatings());
                                }

                                //close both
                                ratingPage.dismiss();
                                locationInfo.dismiss();
                            }
                        });


                        ratingPage.show();

                    }
                });

                Button directionsBtn = (Button) locationInfo.findViewById(R.id.directionsBtn);
                //int directionsBtn = index;
                directionsBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        double destinationLat;
                        double destinationLong;
                        //find the latitude and longitude of the selected bathroom

                        locationInfo.dismiss();
                        if(finalIndex != -1) {
                            destinationLat = locationInfoList.get(finalIndex).getlocationLat();
                            destinationLong = locationInfoList.get(finalIndex).getLocationLong();

                            Polyline directions = mMap.addPolyline(new PolylineOptions()
                                    .clickable(true)
                                    .add(
                                            new LatLng(37.42247772216797, -122.08404541015625),
                                            new LatLng(destinationLat, destinationLong)));

                            mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                                @Override
                                public void onPolylineClick(@NonNull Polyline polyline) {
                                    polyline.remove();

                                }
                            });
                        }


                        //locationInfo.dismiss();


                    }
                });


                        locationInfo.show();

                return false;
            }
        });




    }
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == REQUEST_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener (new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();

                        // Set the count, handling cases where less than 5 entries are returned.
                        int count;
                        if (likelyPlaces.getPlaceLikelihoods().size() < 5) {
                            count = likelyPlaces.getPlaceLikelihoods().size();
                        } else {
                            count = 5;
                        }

                        int i = 0;
                        likelyPlaceNames = new String[count];
                        likelyPlaceAddresses = new String[count];
                        likelyPlaceAttributions = new List[count];
                        likelyPlaceLatLngs = new LatLng[count];

                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                            likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                            likelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                    .getAttributions();
                            likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                            i++;
                            if (i > (count - 1)) {
                                break;
                            }
                        }

                        // Show a dialog offering the user the list of likely places, and add a
                        // marker at the selected place.
                        //MapsActivityCurrentPlace.this.openPlacesDialog();
                    }
                    else {
                        //Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } else {
            getLocationPermission();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }


    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


}