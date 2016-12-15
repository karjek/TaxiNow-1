package com.home.yassine.taxinow;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.home.yassine.taxinow.protocol.AuthenticationMessage;
import com.home.yassine.taxinow.protocol.MessageReceiver;
import com.home.yassine.taxinow.protocol.UpdateLocationMessage;
import com.home.yassine.taxinow.types.LocationData;
import com.home.yassine.taxinow.types.TaxiInfo;
import com.wang.avi.AVLoadingIndicatorView;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    public LocationData mDestination;
    public LocationData mCurrentLocation;
    public WebSocketClient mWSClient;
    User user;
    SearchActivity context = this;

    private GoogleApiClient mGoogleApiClient;
    private static final float MIN_ACCURACY = 35.0f;
    private MessageReceiver mMessageReceiver;
    private GoogleMap mMap;
    private Marker Mmarker;
    private LatLng mDriverlocation;
    private SupportMapFragment _frag;
    private int mNotificationId = 0;
    public boolean Stopped = false;
    int mNumRiders = 1;
    Boolean mDetour = true;
    Boolean mFemaleOnly = false;
    private boolean mIsUserChoosingTaxi = false;
    private TaxiChoiceFragment mChoiceFrag;

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public enum ConnectionState {
        CONNECTED,
        CLOSED
    }

    public enum SearchState {
        SEARCHING,
        WAITING,
    }

    public ConnectionState mConnectionState = ConnectionState.CLOSED;
    public SearchState mSearchState = SearchState.SEARCHING;

    public SearchActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        user = Storage.LoadUser(this);

        if (user == null) {
            startActivity(new Intent(this, MainActivity.class));
            return;
        }

        AVLoadingIndicatorView mLoader = (AVLoadingIndicatorView) findViewById(R.id.avi);

        if (mLoader != null) {
            mLoader.show();
        }

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeClient();
            }
        });

        _frag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frame_layout);
        getSupportFragmentManager().beginTransaction().hide(_frag).commitAllowingStateLoss();

        mChoiceFrag = (TaxiChoiceFragment) getSupportFragmentManager().findFragmentById(R.id.choice_frame_layout);

        getSupportFragmentManager().beginTransaction().hide(mChoiceFrag).commitAllowingStateLoss();

        mMessageReceiver = new MessageReceiver();
        InitWebSocketClient();

        String destinationString = getIntent().getStringExtra("Destination");

        if (destinationString == null) {
            finish();
            return;
        }

        mNumRiders = getIntent().getIntExtra("numRiders", 1);
        mDetour = getIntent().getBooleanExtra("detour", true);
        mIsUserChoosingTaxi = getIntent().getBooleanExtra("userChoosingTaxi", false);
        mFemaleOnly = getIntent().getBooleanExtra("femaleOnly", false);

        try {

            JSONObject jsonReader = new JSONObject(destinationString);

            LocationData locationData = new LocationData();

            locationData.unpack(jsonReader);

            mDestination = locationData;

            String currentLocationString = getIntent().getStringExtra("CurrentLoc");

            if (currentLocationString != null) {

                JSONObject jsonReader2 = new JSONObject(currentLocationString);

                LocationData currentLocationData = new LocationData();

                currentLocationData.unpack(jsonReader2);

                mCurrentLocation = currentLocationData;

                mWSClient.connect();
            }

            buildGoogleApiClient();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            mGoogleApiClient.connect();
            Stopped = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {

            Stopped = true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void SendAuthMessage() {

        if (mCurrentLocation == null)
            return;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> destinations = null;
        List<Address> locations = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            destinations = geocoder.getFromLocation(
                    mDestination.Lat,
                    mDestination.Long,
                    // In this sample, we get just a single address.
                    1);

            locations = geocoder.getFromLocation(
                    mCurrentLocation.Lat,
                    mCurrentLocation.Long,
                    // In this sample, we get just a single address.
                    1);
        } catch (IOException | IllegalArgumentException ioException) {
            // Catch network or other I/O problems.
        }

        AuthenticationMessage authMessage = new AuthenticationMessage();
        authMessage.email = user.getEmail();
        authMessage.token = user.getToken();
        authMessage.manufacturer = Build.MANUFACTURER;
        authMessage.model = Build.MODEL;
        authMessage.destination = mDestination;
        authMessage.currentLocation = mCurrentLocation;
        authMessage.numRiders = mNumRiders;
        authMessage.detour = mDetour;
        authMessage.femaleOnly = mFemaleOnly;
        authMessage.isUserChoosingTaxi = mIsUserChoosingTaxi;

        if (destinations == null || destinations.size() == 0 || destinations.get(0).getMaxAddressLineIndex() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, R.string.address_dest_not_found, Toast.LENGTH_SHORT).show();
                }
            });
            closeClient();
            finish();
        }
        else
        {
            authMessage.destAddress = destinations.get(0).getAddressLine(0);
        }

        if (locations == null || locations.size() == 0 || locations.get(0).getMaxAddressLineIndex() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, R.string.actual_address_not_found, Toast.LENGTH_SHORT).show();
                }
            });
            closeClient();
            finish();
        }
        else
        {
            authMessage.locAddress = locations.get(0).getAddressLine(0);
        }

        JSONObject jsonWriter = new JSONObject();

        try {
            authMessage.pack(jsonWriter);
            mWSClient.send(jsonWriter.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void InitWebSocketClient() {

        URI uri;
        try {
            uri = new URI(HttpClient.BASE_URL_WS);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWSClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                SendAuthMessage();
            }

            @Override
            public void onMessage(String message) {
                Log.e("RECEIVED", message);
                mMessageReceiver.Receive(context, message);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.e("Websocket", "Closed " + s + " " + String.valueOf(b));
                finish();
            }

            @Override
            public void onError(Exception e) {
                Log.e("WEBSOCKET_ERROR_HERE", "Error " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mConnectionState == ConnectionState.CLOSED)
                            finish();

                        Toast.makeText(context, context.getString(R.string.err_connection), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
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

       // Log.e("LOCATION", String.valueOf(location.getAccuracy()) + " " + String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude()));

        if (mCurrentLocation == null && location.getAccuracy() < 100) {
            mCurrentLocation = new LocationData();
            UpdateCurrentLocationData(location);
            mWSClient.connect();
        }
        else if (location.getAccuracy() < MIN_ACCURACY && mConnectionState == ConnectionState.CONNECTED) {

            UpdateCurrentLocationData(location);
            SendUpdateCurrentLocation();
        }
    }

    private void SendUpdateCurrentLocation() {

        UpdateLocationMessage updateLocMessage = new UpdateLocationMessage();

        updateLocMessage.currentLocation = mCurrentLocation;

        JSONObject jsonWriter = new JSONObject();

        try {
            updateLocMessage.pack(jsonWriter);
            mWSClient.send(jsonWriter.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void UpdateCurrentLocationData(Location location) {
        mCurrentLocation.Lat = location.getLatitude();
        mCurrentLocation.Long = location.getLongitude();
        mCurrentLocation.Acc = location.getAccuracy();
    }

    public void pushNotification(String msg, Class activity) {
        mNotificationId++;
        NotificationManager mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        final Intent notificationIntent = new Intent(context, activity);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this)
                .setSmallIcon(R.drawable.white_logo)
                .setContentTitle("Taxi Now")
                .setColor(0xff448aff)
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(
                        RingtoneManager
                                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

    public void setupMap(double lat, double lon) {

        mDriverlocation = new LatLng(lat, lon);

        _frag.getMapAsync(this);
        mSearchState = SearchState.WAITING;

        if (Stopped)
            pushNotification(getString(R.string.taxi_is_coming), SearchActivity.class);
        else
            Toast.makeText(this, getString(R.string.taxi_is_coming), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        if (mChoiceFrag.showingMap) {
            mChoiceFrag.hideMap();
            return;
        }

        if (mSearchState != SearchState.WAITING) {
            super.onBackPressed();
            closeClient();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (mSearchState == SearchState.WAITING) {

            if (mIsUserChoosingTaxi) {
                getSupportFragmentManager().beginTransaction().hide(mChoiceFrag).commitAllowingStateLoss();
            }

            mMap = googleMap;

            AVLoadingIndicatorView mLoader = (AVLoadingIndicatorView) findViewById(R.id.avi);

            if (mLoader != null) {
                mLoader.hide();
            }

            getSupportFragmentManager().beginTransaction().show(_frag).commitAllowingStateLoss();

            Mmarker = mMap.addMarker(new MarkerOptions()
                    .position(mDriverlocation)
                    .title(getString(R.string.arrival)));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mDriverlocation).zoom(15).build();

            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    public void BackToSearch() {

        mSearchState = SearchState.SEARCHING;
        getSupportFragmentManager().beginTransaction().hide(_frag).commitAllowingStateLoss();

        AVLoadingIndicatorView mLoader = (AVLoadingIndicatorView) findViewById(R.id.avi);

        if (mLoader != null) {
            mLoader.show();
            mLoader.setVisibility(View.VISIBLE);
        }
    }

    public void refreshTaxiChoiceList(ArrayList<TaxiInfo> taxiInfos) {

        mChoiceFrag.refreshList(taxiInfos);

        AVLoadingIndicatorView mLoader = (AVLoadingIndicatorView) findViewById(R.id.avi);

        if (mLoader != null && mLoader.getVisibility() == View.VISIBLE) {
            mLoader.hide();
            getSupportFragmentManager().beginTransaction().show(mChoiceFrag).commitAllowingStateLoss();

            if (Stopped) {
                pushNotification(getString(R.string.taxi_in_view), SearchActivity.class);
            }
        }
    }

    public void UpdateDriverPosition(double lat, double lon) {

        mDriverlocation = new LatLng(lat, lon);

        Mmarker.setPosition(mDriverlocation);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mDriverlocation).zoom(15).build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    public void closeClient() {
        if(mWSClient != null)
            mWSClient.close();
    }
}

