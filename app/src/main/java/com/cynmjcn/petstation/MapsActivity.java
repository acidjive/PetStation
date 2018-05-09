package com.cynmjcn.petstation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.cynmjcn.petstation.Model.Results;
import com.cynmjcn.petstation.Model.vetPlaces;
import com.cynmjcn.petstation.Remote.IGoogleApiService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener{

    private static final int MY_PERMISSION_CODE = 1000;
    private GoogleMap mMap;

    private double latitude,longitude;
    private Location mLastLocation;
    private Marker mMarker;
    private GoogleApiClient mGoogleApiClient;


    IGoogleApiService mService;

    vetPlaces currentNearbyPlaces;



    private LocationRequest mLocationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Innit Services
        mService = Common.getGoogleAPIServices();

        //Request Runtime Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           checkLocationPermisson();


        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navBar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.vetClinic:
                     nearbyPlace("veterinary_care");
                     break;
                    case R.id.park:
                        nearbyPlace("park");
                        break;
                    case R.id.petStore:
                        nearbyPlace("pet_store");
                        break;
                    default:
                        break;
                }

                return true;
            }
        });

    }

    private boolean checkLocationPermisson() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION

                    }, MY_PERMISSION_CODE);
                else
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION

                    }, MY_PERMISSION_CODE);
                return false;

            } else
            return true;
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       switch (requestCode){
           case MY_PERMISSION_CODE:
           {
               if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
               {
                  if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                   {
                       if(mGoogleApiClient == null)
                           buildGoogleApiClient();
                       mMap.setMyLocationEnabled(true);
                   }
               } else
                   Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
           }
           break;
       }

    }


    //SEARCH NEARBY
    private void nearbyPlace(final String placeType) {
        mMap.clear();

        String url = getUrl(latitude,longitude,placeType);

        //gets information based on the selected place
        mService.getNearByPlaces(url).enqueue(new Callback<vetPlaces>() {
            @Override
            public void onResponse(Call<vetPlaces> call, Response<vetPlaces> response) {

                //Value for currentPlace
                currentNearbyPlaces = response.body();

                if (response.isSuccessful())
                {
                    for (int i = 0; i<response.body().getResults().length;i++)
                    {
                        MarkerOptions markerOptions = new MarkerOptions();
                        Results googlePlace = response.body().getResults()[i];

                        double lat = Double.parseDouble(googlePlace.getGeometry().getLocation().getLat());
                        double lng = Double.parseDouble(googlePlace.getGeometry().getLocation().getLng());

                        String placeName = googlePlace.getName();
                        String  vicinity = googlePlace.getVicinity();
                        LatLng latLng = new LatLng(lat,lng);

                        //position of marker
                        markerOptions.position(latLng);
                        markerOptions.title(placeName);


                        //Marker icon
                        if (placeType.equals("veterinary_care"))
                          markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_vet));

                        else if (placeType.equals("pet_store"))
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_store));

                        else if (placeType.equals("park"))
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_park));

                        else
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));


                        //Assign Index for markers. (Details)
                        //snippet - Additional text that's displayed below the title. You can change this value at any time.
                        // valueOf(i) - Returns the string representation of the int argument.
                        //Returns a string
                        markerOptions.snippet(String.valueOf(i));

                        //Move Camera to the chosen place / map view
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                        //Place marker on map
                        mMap.addMarker(markerOptions);


                    }
                }
            }

            @Override
            public void onFailure(Call<vetPlaces> call, Throwable t) {

            }
        });

    }

    private String getUrl(double latitude, double longitude, String placeType) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude+","+ longitude);
        googlePlacesUrl.append("&radius="+ 10000);
        googlePlacesUrl.append("&type="+ placeType);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key="+ getResources().getString(R.string.browser_key));
        Log.d("getUrl",googlePlacesUrl.toString());
        return googlePlacesUrl.toString();

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
        mMap = googleMap;

        //Init Google Play Services LOCATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
            else{
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }


            //When icon is clicked...
           mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
               @Override
               public boolean onMarkerClick(Marker marker) {

                    //Converting number string to int.
                   Common.currentResult = currentNearbyPlaces.getResults()[Integer.parseInt(marker.getSnippet())];

                   //Second activity
                   startActivity(new Intent(MapsActivity.this,SecondActivity.class));

                   return true;
               }
           });

        }

    private synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if(mMarker != null)
            mMarker.remove();

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude,longitude);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Youre Here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        mMarker = mMap.addMarker(markerOptions);

        //Camera movement
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


        if(mGoogleApiClient != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);





    }
}

