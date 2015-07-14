package com.example.miguelamores.noisemeter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.miguelamores.data.Medicion;
import com.example.miguelamores.data.SQLHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    SQLiteDatabase sqLiteDatabase;
    Medicion medicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        final SQLHelper sqlHelper = new SQLHelper(this);
        sqLiteDatabase = sqlHelper.getWritableDatabase();
        Cursor cursor;
        //cursor = sqLiteDatabase.query("medicion",null,null,null,null,null,null);
        cursor = sqLiteDatabase.query("medicion", new String[]{"valor_db", "latitud", "longitud"}, null, null, null, null, null);

        ArrayList<Medicion> medicions = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                medicion = new Medicion();
                medicion.setValor_db(cursor.getInt(0));
                medicion.setLatitud(cursor.getDouble(1));
                medicion.setLongitud(cursor.getDouble(2));
                medicions.add(0, medicion);
                mMap.addMarker(new MarkerOptions().position(new LatLng(medicion.getLatitud(), medicion.getLongitud())).title(String.valueOf(medicion.getValor_db())+" dB"));
            }
            while (cursor.moveToNext());
        }


        //mMap.addMarker(new MarkerOptions().position(new LatLng(-0.9415252, -78.6110042)).title("80 Decibels"));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(-0.1695956, -78.4712558)).title("50 Decibels"));

    }
}
