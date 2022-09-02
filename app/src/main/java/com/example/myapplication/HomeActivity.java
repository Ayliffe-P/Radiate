package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import com.google.maps.android.SphericalUtil;
import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
//import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
//import rx.Scheduler;
//import rx.schedulers.Schedulers;


public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {
    GoogleMap _map;
    SupportMapFragment _mapFragment;

    private List<LatLng> polylinelist;
    private PolylineOptions polylineOptions;
    private LatLng orig,dest;

    SharedPreferences.Editor _sharedPrefsEdit;
    SharedPreferences _appSettingPrefs;
    Boolean _isNightModeOn;


    FusedLocationProviderClient _fusedLocationProviderClient;
    static final int REQUEST_CODE = 101;
    Location _currentLocation;

    SearchView _searchView;
    Address _address;

    Boolean  _showCurrentLocation = false;

    BottomSheetBehavior _location,_main;

    static final String PLACES_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=";

    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d("tag", "Hi"  );

        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).baseUrl("https://maps.googleapis.com/").build();
        apiInterface = retrofit.create(ApiInterface.class);

        _mapFragment  = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map_home);

        _location = BottomSheetBehavior.from(findViewById(R.id.ll_standard_slideUpMenu));
        _location.setState(BottomSheetBehavior.STATE_HIDDEN);

        _main = BottomSheetBehavior.from(findViewById(R.id.ll_standard_searchMenu));
        _main.setState(BottomSheetBehavior.STATE_COLLAPSED);

        _searchView  = findViewById(R.id.svSearch_searchMenu);
        _searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = _searchView.getQuery().toString();
                List<Address> addressList = null;

                if(location != null || !location.equals("")){
                    Geocoder geocoder = new Geocoder(HomeActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());

                    _map.addMarker(new MarkerOptions().position(latLng).title(location));
                    _map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        _mapFragment.getMapAsync(this::onMapReady);




        ImageButton btnCurrentLocation  = findViewById(R.id.btn_CurrentLocation_home);
        btnCurrentLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowCurrentLocation();
            }
        });

        ImageButton btnSettings  = findViewById(R.id.btn_Settings_searchMenu);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class); //replace with check login
                startActivity(intent);
            }
        });

        //region places
        ImageButton btnMuseum  = findViewById(R.id.btnMuseum_filter);
        btnMuseum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowPlaces("museum");
            }
        });

        ImageButton btnAtm  = findViewById(R.id.btnATM_Filter);
        btnAtm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowPlaces("atm");
            }
        });

        ImageButton btnResturants  = findViewById(R.id.btnResturant_Filter);
        btnResturants.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowPlaces("restaurant");
            }
        });

        ImageButton btnHospital  = findViewById(R.id.btnHospital_filter);
        btnHospital.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowPlaces("hospital");
            }
        });
        //endregion

        //_appSettingPrefs = getSharedPreferences("AppSettingPrefs",0); // get the storage reference
        //_sharedPrefsEdit = _appSettingPrefs.edit(); //set the storage editor reference
        //_isNightModeOn = _appSettingPrefs.getBoolean("NightMode",false); //check the variable status

        //if(_isNightModeOn) // set the mode
        //    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); //night
        //else
         //   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //day
        //ChangeTheme();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        _map = googleMap;
        _map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        _map.setTrafficEnabled(true);

        _map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                orig = new LatLng(_currentLocation.getLatitude(), _currentLocation.getLongitude());

                dest = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
                // dest = "" + point.latitude + "," + point.longitude;
                _location.setState(BottomSheetBehavior.STATE_COLLAPSED);
                _main.setState(BottomSheetBehavior.STATE_HIDDEN);

                TextView text  = findViewById(R.id.txtName_standard_slideup);
                text.setText(marker.getTitle());

                ImageView directions =  findViewById(R.id.btnFavourite_standard_slideup2);
                directions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getDirections(  _currentLocation.getLatitude() + "," + _currentLocation.getLongitude(),   marker.getPosition().latitude + "," + marker.getPosition().longitude);
                    }
                });
                return false;
            }
        });
        _map.addMarker(new MarkerOptions().position(new LatLng( -5.40689,37.42266)).title("location"));
        _map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                Log.d("tag", " clicked");

            }
        });
    }

//region Current Location
    void ShowCurrentLocation(){
        _fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        FetchLastLocation();

    }

    private  void getDirections(String origin, String destination){
        Log.d("tag", " directing: " + origin + " Destination " + destination);
        apiInterface.getDirection("driving", "less_driving", origin, destination, getString(R.string.map_key)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Result>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d("tag", " subbed");
            }

            @Override
            public void onSuccess(Result result) {
                Log.d("tag", " success");
                polylinelist = new ArrayList<>();
                List<Route> routeList = result.getRoutes();
                for (Route route : routeList) {

                    String polyline = route.getOverviewPolyline().getPoints();
                    Log.d("tag", " route: " + decodePoly(polyline));
                    polylinelist.addAll(decodePoly(polyline));
                    Log.d("tag", " route2 : " + polylinelist);
                }
                polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.BLUE);
                polylineOptions.width(16);
                polylineOptions.startCap(new ButtCap());
                polylineOptions.jointType(JointType.ROUND);
                polylineOptions.addAll(polylinelist);
                Log.d("tag", " poly : " + polylineOptions);
                _map.addPolyline(polylineOptions);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(orig);
                builder.include(dest);
                Log.d("tag", " building");
                _map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
                double distance;
                String symbol;
                if (Profile.getInstance().getMetricSystem()) {
                    distance = (SphericalUtil.computeDistanceBetween(orig, dest) / 1000);
                    symbol = "km";
                }
                else {
                    distance = convertToImperial((SphericalUtil.computeDistanceBetween(orig, dest) / 1000));
                    symbol = "mil";
                }

                LinearLayout layout = findViewById(R.id.ll_standard_slideUpMenu);
                LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
                LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.direction_button, null, false);
                layout.addView(lay);
                lay.setGravity(Gravity.CENTER);
                distance = Math.round(distance * 100.0) / 100.0;
                TextView text =findViewById(R.id.txtDistance_directionButton);
                text.setText(distance +symbol+" from your location");
            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }


    void FetchLastLocation() {
        _showCurrentLocation = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
                return;
        }
        Task<Location> task = _fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location !=null){
                    _currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map_home);
                    supportMapFragment.getMapAsync(HomeActivity.this::onMapReady);
                    _map.addMarker(new MarkerOptions().position(new LatLng(_currentLocation.getLatitude(),_currentLocation.getLongitude())).title("You Are Here"));
                    _map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),10));
                    _showCurrentLocation = false;

                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && _showCurrentLocation)
                    FetchLastLocation();
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    //endregion Current Location
    private double convertToImperial(double kiloMeters){
        double miles;
        double conversionFactor = 1.609344;
        miles = kiloMeters / conversionFactor;
        return miles;
    }
//region Show places
void ShowPlaces(String type){

    ShowCurrentLocation();


   String url = PLACES_URL + _currentLocation.getLatitude()+","
          +_currentLocation.getLongitude() + "&radius=5000&types="
          + type + "&sensor=true&key=" + getResources().getString(R.string.map_key);
  new PlaceTask().execute(url);
    }

    private class PlaceTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            try {
                data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            new ParserTask().execute(s);
        }

        private String downloadUrl(String string) throws IOException {

            URL url = new URL(string);
            Log.d("tag", "url : " + url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.connect();

            InputStream stream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder builder = new StringBuilder();
            String line = "";

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            String data = builder.toString();
            reader.close();
            return data;

        }

        private void getDuration() {

            HashMap<String, String> googleDirectionMap = new HashMap<>();

        }


        private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
            @Override
            protected List<HashMap<String, String>> doInBackground(String... strings) {
                JsonParser parser = new JsonParser();
                List<HashMap<String, String>> mapList = null;
                JSONObject object = null;
                try {

                    object = new JSONObject(strings[0]);
                    mapList = parser.parseResult(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return mapList;
            }

            @Override
            protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
                _map.clear();
                for (int i = 0; i < hashMaps.size(); i++) {
                    HashMap<String, String> hashMap = hashMaps.get(i);

                    double lat = Double.parseDouble(hashMap.get("lat"));
                    double lng = Double.parseDouble(hashMap.get("lng"));

                    String name = hashMap.get("name");

                    LatLng latlng = new LatLng(lat, lng);
                    MarkerOptions options = new MarkerOptions();

                    options.position(latlng);
                    options.title(name);


                    _map.addMarker(options);

                }
            }
        }
    }
   private List<LatLng> decodePoly(String encoded) {

       List<LatLng> poly = new ArrayList<LatLng>();
       int index = 0, len = encoded.length();
       int lat = 0, lng = 0;

       while (index < len) {
           int b, shift = 0, result = 0;
           do {
               b = encoded.charAt(index++) - 63;
               result |= (b & 0x1f) << shift;
               shift += 5;
           } while (b >= 0x20);
           int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
           lat += dlat;

           shift = 0;
           result = 0;
           do {
               b = encoded.charAt(index++) - 63;
               result |= (b & 0x1f) << shift;
               shift += 5;
           } while (b >= 0x20);
           int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
           lng += dlng;

           LatLng p = new LatLng((((double) lat / 1E5)),
                   (((double) lng / 1E5)));
           poly.add(p);
       }

       return poly;
   }

    void ChangeTheme() {

        if(_isNightModeOn) // variable set in OnCreate
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            _sharedPrefsEdit.putBoolean("NightMode",false);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            _sharedPrefsEdit.putBoolean("NightMode",true);
        }
        _sharedPrefsEdit.apply();
    }

}
    //endregion Show Places
