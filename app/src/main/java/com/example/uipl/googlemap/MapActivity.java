package com.example.uipl.googlemap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uipl.googlemap.utils.LocationTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener {

    // widgets
    private AutoCompleteTextView mSearchText;
    private ImageView  mGps;

    //varibles
    private boolean mLoctionPermisionGranted = false;
    private static final String TAG = "MapActivity";
    private  SupportMapFragment mapFragment;
    private double latitude;
    private double longitude;
    private LocationTracker locationTracker;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISION_REQUEST_CODE = 1234;
    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private  static final float DEFAULT_ZOOM =15f;
    private  PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    protected GoogleApiClient mGoogleApiClient;
    private   static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-168),new LatLng(71,136));



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /*if (mLoctionPermisionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            // navigate to your current  location
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            //init();
        }*/
        if(latitude >0 && longitude >0){
            Toast.makeText(this,"drawMap method callled ",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "draw method called ");
            drawMap(latitude,longitude);
        }
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText =(AutoCompleteTextView) findViewById(R.id.input_search);
        mGps =(ImageView)findViewById(R.id.ic_gps);
        getlcationPermision();
        getLatLong();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTracker.stopListener();
    }
    private void  getLatLong(){
        locationTracker = new LocationTracker(MapActivity.this);
        locationTracker.setRequestProcessedListener(new ServiceListener<LatLng>() {
            @Override
            public void result(LatLng result) {
                if (result!= null)
                {
                    longitude = locationTracker.getLongitude();
                    latitude = locationTracker.getLatitude();
                    Log.d("getLatLong latitude :",""+latitude);
                    Log.d("getLatLong longitude :",""+longitude);
                    mMap.clear();
                }else {
                  locationTracker.showSettingsAlert();
                }
            }
        });
        if (locationTracker.canGetLocation())
        {
            longitude = locationTracker.getLongitude();
            latitude = locationTracker.getLatitude();
            // start service
            //startService(this);
        }else {
            locationTracker.showSettingsAlert();
        }
    }
   /* private void startService(MapActivity mapActivity) {
        Log.d("startService latitude :",""+latitude);
        Log.d("startServicelongitude :",""+longitude);

        drawMap(latitude,longitude);
    }
*/
    private void drawMap(double latitude, double longitude) {
        Log.d("drawMap latitude :",""+latitude);
        Log.d("drawMap longitude :",""+longitude);

        LatLng latLng = new LatLng(latitude, longitude);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }
    // locate to graph
    private void geoLocate() {
        Log.d(TAG, "geoLocate: geoLocating");
        String searchString = mSearchText.getText().toString();
       Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
       try{
           list = geocoder.getFromLocationName(searchString,10);
       }catch (IOException e)
       {
           Log.d(TAG, "geoLocate: IOExcepion :" + e.getMessage());
       }
       if (list.size()>0){
           Address address = list.get(0);
           Log.d(TAG, "geoLocate: found a Location" + address.toString());
           moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,
                   address.getAddressLine(0));
       }
    }
    // get the device current location
    private void getDeviceLocation() { Log.d(TAG, "getDeviceLocation: getting the device current location ");
        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLoctionPermisionGranted) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    //   here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //  int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Task location = mfusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location)task.getResult();

                            assert currentLocation != null;
                            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM,
                                    "My Location");
                        }else {
                            Log.d(TAG, "onComplete: current location is null");
                          Toast.makeText(MapActivity.this,"Unable to get Locationn ",Toast.LENGTH_SHORT).show();
                           }
                    }
                });
            }
        }catch (SecurityException e ){
            Log.d(TAG, "getDeviceLocation: Security Exception "+ e.getMessage());  // e.getPrintStack
        }
    }
     // camera zoom code
    private void moveCamera(LatLng latLng,float zoom,String tittle){
        Log.d(TAG, "moveCamera: moving the camera to: lat:" + latLng.latitude + ",lng :" +latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        if (!tittle.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(tittle);
            mMap.addMarker(options);
        }
        hideSoftKeyboard();
    }
    // initialize the map
    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);

            mGoogleApiClient = new GoogleApiClient
                    .Builder(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)

                    .enableAutoManage(this,this)
                    .build();
            mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this,mGoogleApiClient,
                    LAT_LNG_BOUNDS,null);
            mSearchText.setAdapter(mPlaceAutocompleteAdapter);

            mSearchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    CharSequence charSequence = mPlaceAutocompleteAdapter.getItem(i).getFullText(null);
                    //Toast.makeText(MapActivity.this, charSequence, Toast.LENGTH_SHORT).show();
                    geoLocate();
                }
            });
            mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || keyEvent.getAction() == keyEvent.ACTION_DOWN
                            || keyEvent.getAction() ==keyEvent.KEYCODE_ENTER){
                        // Execute our method for searching
                        geoLocate();
                    }
                    return false;
                }
            });
            // clickd on icon of gps
            mGps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: gps icon ");
                    getDeviceLocation();
                }
            });
            hideSoftKeyboard();
        }
        // getting the device location permission
    private void getlcationPermision() {
        Log.d(TAG, "getlcationPermision: getting location permision");
        String[] permision = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLoctionPermisionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permision, LOCATION_PERMISION_REQUEST_CODE);
            }
        }else {
            ActivityCompat.requestPermissions(this,
                        permision, LOCATION_PERMISION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLoctionPermisionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLoctionPermisionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permision failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLoctionPermisionGranted = true;
                    // intialize our map
                    initMap();
                }
            }
        }
    }
    private   void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
