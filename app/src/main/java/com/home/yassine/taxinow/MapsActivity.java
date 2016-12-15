package com.home.yassine.taxinow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.home.yassine.taxinow.types.LocationData;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String TAG = "MAP LOCATION";
    Context mContext;
    private LatLng mCenterLatLong;

    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;


    /**
     * The formatted location address.
     */
    protected String mAddressOutput;
    protected String mAreaOutput;
    protected String mCityOutput;
    protected String mStateOutput;
    EditText mLocationAddress;
    AppCompatButton mLocationText;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private LocationRequest mLocationRequest;
    private static final float MIN_ACCURACY = 25.0f;
    public Location mCurrentLocation;
    public MapsActivity mActivity = this;


    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext = this;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mLocationAddress = (EditText) findViewById(R.id.Address);
        mLocationText = (AppCompatButton) findViewById(R.id.Locality);

        getSupportActionBar().hide();

        mLocationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAutocompleteActivity();
            }
        });

        mapFragment.getMapAsync(this);
        mResultReceiver = new AddressResultReceiver(new Handler());

        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.
            if (!AppUtils.isLocationEnabled(mContext)) {
                // notify user
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setMessage(R.string.activate_gps);
                dialog.setPositiveButton(R.string.location_params, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub

                    }
                });
                dialog.show();
            }
            buildGoogleApiClient();
        } else {
            Toast.makeText(mContext, R.string.gps_not_supported, Toast.LENGTH_SHORT).show();
        }

        AppCompatButton mQuickSearchBtn = (AppCompatButton) findViewById(R.id.quickSearch);

        mQuickSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            if (mCenterLatLong == null)
                return;

            Intent intent = new Intent(mContext, SearchActivity.class);

            JSONObject jsonWriter = new JSONObject();

            LocationData destinationData = new LocationData();
            destinationData.Long = mCenterLatLong.longitude;
            destinationData.Lat = mCenterLatLong.latitude;
            destinationData.Acc = -1;

            try {

                destinationData.pack(jsonWriter);
                intent.putExtra("Destination", jsonWriter.toString());

                if (mCurrentLocation != null) {

                    LocationData locationData = new LocationData();
                    locationData.Long = mCurrentLocation.getLongitude();
                    locationData.Lat = mCurrentLocation.getLatitude();
                    locationData.Acc = mCurrentLocation.getAccuracy();
                    locationData.pack(jsonWriter);
                    intent.putExtra("CurrentLoc", jsonWriter.toString());
                }

                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            }
        });

        LayoutInflater inflater = LayoutInflater.from(mContext);
        final Dialog dialog = new AlertDialog.Builder(mContext).setView(inflater.inflate(R.layout.activity_advanced_search_options, null)).create();

        AppCompatButton advancedSearch = (AppCompatButton) findViewById(R.id.advancedSearch);

        if (advancedSearch != null) {
            advancedSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.show();

                    final AppCompatButton searchBtn = (AppCompatButton) dialog.findViewById(R.id.search_adv);
                    final DiscreteSeekBar seekBar = (DiscreteSeekBar) dialog.findViewById(R.id.my_seek_bar);
                    final CheckBox check = (CheckBox) dialog.findViewById(R.id.check);
                    final CheckBox isUserChoosingTaxi = (CheckBox) dialog.findViewById(R.id.i_choose_taxi);
                    final CheckBox check2 = (CheckBox) dialog.findViewById(R.id.check2);
                    final TextView check2Text = (TextView) dialog.findViewById(R.id.female_only_text);
                    searchBtn.setEnabled(true);
                    String gender = Storage.LoadUser(mActivity).getGender();

                    if (gender != null && gender.equals("F")) {
                        check2Text.setVisibility(View.VISIBLE);
                        check2.setVisibility(View.VISIBLE);
                    }

                    searchBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            searchBtn.setEnabled(false);

                            Intent intent = new Intent(mContext, SearchActivity.class);

                            JSONObject jsonWriter = new JSONObject();

                            LocationData destinationData = new LocationData();
                            destinationData.Long = mCenterLatLong.longitude;
                            destinationData.Lat = mCenterLatLong.latitude;
                            destinationData.Acc = -1;

                            try {

                                destinationData.pack(jsonWriter);
                                intent.putExtra("Destination", jsonWriter.toString());
                                intent.putExtra("numRiders", seekBar.getProgress());
                                intent.putExtra("detour", check.isChecked());
                                intent.putExtra("femaleOnly", check2.isChecked());
                                intent.putExtra("userChoosingTaxi", isUserChoosingTaxi.isChecked());

                                if (mCurrentLocation != null) {

                                    LocationData locationData = new LocationData();
                                    locationData.Long = mCurrentLocation.getLongitude();
                                    locationData.Lat = mCurrentLocation.getLatitude();
                                    locationData.Acc = mCurrentLocation.getAccuracy();
                                    locationData.pack(jsonWriter);
                                    intent.putExtra("CurrentLoc", jsonWriter.toString());
                                }

                                startActivity(intent);
                                dialog.hide();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    final AppCompatButton cancelBtn = (AppCompatButton) dialog.findViewById(R.id.cancel_dialog);

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.hide();
                        }
                    });
                }
            });
        }
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

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mCenterLatLong = cameraPosition.target;

                mMap.clear();

                try {

                    Location mLocation = new Location("");
                    mLocation.setLatitude(mCenterLatLong.latitude);
                    mLocation.setLongitude(mCenterLatLong.longitude);
                   // Log.e("CameraChanged",String.valueOf(mLocation.getLatitude()) + String.valueOf(mLocation.getLongitude()));
                    startIntentService(mLocation);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
//        mMap.setMyLocationEnabled(true);
//        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            changeMap(mLastLocation);
            Log.d(TAG, "ON connected");
        }
        else {
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, this);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(3000);
            mLocationRequest.setFastestInterval(3000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
           // Log.e("LOCATION", String.valueOf(location.getAccuracy()) +" "+ String.valueOf(location.getLatitude()) +" "+ String.valueOf(location.getLongitude()));

            if (mCurrentLocation == null && location.getAccuracy() < 100) {
                changeMap(location);
                mCurrentLocation = location;
            }
            if (location.getAccuracy() < MIN_ACCURACY) {

                    mCurrentLocation = location;
                //Log.e("Location", String.valueOf(location.getLongitude()) + String.valueOf(location.getLatitude()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mGoogleApiClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //finish();
            }
            return false;
        }
        return true;
    }

    private void changeMap(Location location) {

        Log.d(TAG, "Reaching map" + mMap);

        //Log.e("changeMap", String.valueOf(location.getAccuracy()) +" "+ String.valueOf(location.getLatitude()) +" "+ String.valueOf(location.getLongitude()));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // check if map is created successfully or not
        if (mMap != null && location.getAccuracy() != 0) {
            mMap.getUiSettings().setZoomControlsEnabled(false);
            LatLng latLong;

            latLong = new LatLng(location.getLatitude(), location.getLongitude());

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(18f).build();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

            startIntentService(location);

        } else {
//            Toast.makeText(getApplicationContext(),
//                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
//                    .show();
        }

    }


    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(AppUtils.LocationConstants.RESULT_DATA_KEY);

            mAreaOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_AREA);

            mCityOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_CITY);
            mStateOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_STREET);

            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == AppUtils.LocationConstants.SUCCESS_RESULT) {
                //  showToast(getString(R.string.address_found));
            }
        }
    }

    /**
     * Updates the address in the UI.
     */
    protected void displayAddressOutput() {
          mLocationAddress.setText(mAddressOutput);
        try {
            if (mAreaOutput != null)
                // mLocationText.setText(mAreaOutput+ "");

                mLocationAddress.setText(mAddressOutput);
            //mLocationText.setText(mAreaOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService(Location mLocation) {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(AppUtils.LocationConstants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(AppUtils.LocationConstants.LOCATION_DATA_EXTRA, mLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }


    private void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .zzku(mCityOutput != null && !mCityOutput.equals("") ? mCityOutput.concat(", ") : "")
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = getString(R.string.playservices_not_enabled) +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called after the autocomplete activity has finished to return its result.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(mContext, data);

                // TODO call location based filter

                LatLng latLong;

                latLong = place.getLatLng();

                //mLocationText.setText(place.getName() + "");

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLong).zoom(18f).build();

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            }


        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
            Status status = PlaceAutocomplete.getStatus(mContext, data);
        } else if (resultCode == RESULT_CANCELED) {
            // Indicates that the activity closed before a selection was made. For example if
            // the user pressed the back button.
        }
    }
}