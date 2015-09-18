package com.example.miguelamores.noisemeter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.miguelamores.data.Medicion;
import com.example.miguelamores.data.SQLHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    SQLiteDatabase sqLiteDatabase;
    Medicion medicion;
    HeatmapTileProvider heatMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-0.180653, -78.467834), 6));
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
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

        ArrayList<WeightedLatLng> list = new ArrayList<>();
        LatLng latLng;
        WeightedLatLng weightedLatLng;

        final SQLHelper sqlHelper = new SQLHelper(this);
        sqLiteDatabase = sqlHelper.getWritableDatabase();
        Cursor cursor;


        // Create the gradient.
        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.rgb(255, 0, 0)    // red
        };

        float[] startPoints = {
                0.2f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);


        //cursor = sqLiteDatabase.query("medicion",null,null,null,null,null,null);
        cursor = sqLiteDatabase.query("medicion", new String[]{"valor_db", "latitud", "longitud", "hora"}, null, null, null, null, null);

        ArrayList<Medicion> medicions = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {

                latLng = new LatLng(cursor.getDouble(1), cursor.getDouble(2));
                weightedLatLng = new WeightedLatLng(latLng, cursor.getDouble(0));
                //latLng = new LatLng(-0.180653, -78.467834);
                //list.add(latLng);
                list.add(weightedLatLng);

//                medicion = new Medicion();
//                medicion.setValor_db(cursor.getInt(0));
//                medicion.setLatitud(cursor.getDouble(1));
//                medicion.setLongitud(cursor.getDouble(2));
//                medicions.add(0, medicion);
//                mMap.addMarker(new MarkerOptions().position(latLng).title(String.valueOf(medicion.getValor_db())+" dB"));
                mMap.addMarker(new MarkerOptions().position(latLng).title(String.valueOf(String.format("%.2f", cursor.getDouble(0))) +
                        " dB"));
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        //heatMap = new HeatmapTileProvider.Builder().data(list).build();
        heatMap = new HeatmapTileProvider.Builder().weightedData(list).gradient(gradient).build();
        TileOverlay mOveray = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatMap));
    }


}
