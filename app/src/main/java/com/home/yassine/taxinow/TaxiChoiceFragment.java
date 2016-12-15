package com.home.yassine.taxinow;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.home.yassine.taxinow.types.TaxiInfo;

import java.util.ArrayList;

/**
 * Created by Yassine on 29/09/2016.
 */
public class TaxiChoiceFragment extends Fragment implements OnMapReadyCallback {

    public SearchActivity hostActivity;
    private ListView mListView;
    private TaxiChoiceAdapter mAdapter;
    public ArrayList<TaxiInfo> mTaxiInfos;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private Marker Mmarker;
    public boolean showingMap = false;

    public TaxiChoiceFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_taxi_choice, container, false);

        mListView = (ListView) v.findViewById(R.id.taxi_list);

        mTaxiInfos = new ArrayList<>();

        mAdapter = new TaxiChoiceAdapter(hostActivity, mTaxiInfos, this);
        mListView.setAdapter(mAdapter);
        mAdapter.setMode(Attributes.Mode.Single);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((SwipeLayout)(mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).open(true);
            }
        });

        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mMapFragment.getMapAsync(this);

        getChildFragmentManager().beginTransaction().hide(mMapFragment).commit();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        hostActivity = (SearchActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hostActivity = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(hostActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(hostActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    public void showMap(double lat, double lon) {

        LatLng driverLoc = new LatLng(lat, lon);

        if (Mmarker != null)
            Mmarker.remove();

        Mmarker = mMap.addMarker(new MarkerOptions()
                .position(driverLoc)
                .title("Taxi"));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(driverLoc).zoom(14).build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        getChildFragmentManager().beginTransaction().show(mMapFragment).commitAllowingStateLoss();

        this.showingMap = true;
    }

    public void hideMap() {
        getChildFragmentManager().beginTransaction().hide(mMapFragment).commitAllowingStateLoss();
        this.showingMap = false;
    }

    public void refreshList(ArrayList<TaxiInfo> taxiInfos) {

        mTaxiInfos.clear();

        for (TaxiInfo taxiInfo : taxiInfos) {
            mTaxiInfos.add(taxiInfo);
        }

        mAdapter = new TaxiChoiceAdapter(hostActivity, mTaxiInfos, this);
        mListView.setAdapter(mAdapter);
    }
}
