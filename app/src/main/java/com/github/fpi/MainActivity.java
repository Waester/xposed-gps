package com.github.fpi;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements OnMapClickListener {

    private MarkerOptions mMarker = null;
    private GoogleMap mMap = null;
    private LatLng mInit = null;
    private Settings settings = null;
    private CameraUpdate cam = null;
    private ToggleButton tb = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = new Settings(getApplicationContext());

        tb = (ToggleButton) findViewById(R.id.toggleButton);
        tb.setChecked(settings.isStarted());

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.map);
        if (f instanceof MapFragment) {
            MapFragment m = (MapFragment) f;
            mMap = m.getMap();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setOnMapClickListener(this);
            mMarker = new MarkerOptions();
            mInit = new LatLng(settings.getLat(), settings.getLng());
            mMarker.position(mInit);

            cam = CameraUpdateFactory.newLatLngZoom(mInit, settings.getZoom());
            mMap.moveCamera(cam);
            mMap.addMarker(mMarker);

            mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
                public boolean onMarkerClick(Marker m) {
                    return true;
                }
            });
        }
    }

    public void setLocation(View v) {
        mInit = mMarker.getPosition();
        settings.update(mInit.latitude, mInit.longitude, mMap.getCameraPosition().zoom, tb.isChecked());
        toastInfo();
    }

    public void selectApps(View v) {
        Intent i = new Intent(getApplicationContext(), AppChooser.class);
        startActivity(i);
    }

    private void toastInfo() {
        int duration = Toast.LENGTH_SHORT;
        CharSequence text;

        ToggleButton tb = (ToggleButton) findViewById(R.id.toggleButton);
        Context context = getApplicationContext();

        if (tb.isChecked()) {
            text = getString(R.string.location_msg) + " " + mInit.latitude + " " + mInit.longitude;
        } else {
            text = getString(R.string.location_msg_stopped);
        }

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void searchAddress(View v) {
        List<Address> resolvedAddress;

        EditText editAddress = (EditText) findViewById(R.id.address);
        String addressString = editAddress.getText().toString();

        if (!TextUtils.isEmpty(addressString)) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                resolvedAddress = geocoder.getFromLocationName(addressString, 1);

                if (resolvedAddress.size() != 0) {
                    Address address = resolvedAddress.get(0);
                    double latitude = address.getLatitude();
                    double longitude = address.getLongitude();
                    LatLng addressCoord = new LatLng(latitude, longitude);
                    cam = CameraUpdateFactory.newLatLng(addressCoord);
                    mMap.moveCamera(cam);
                }
            } catch (Exception ex) {

            }
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        mMarker.position(point);
        mMap.clear();
        mMap.addMarker(mMarker);
    }
}
