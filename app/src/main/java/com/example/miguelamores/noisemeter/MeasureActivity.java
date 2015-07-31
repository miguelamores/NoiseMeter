package com.example.miguelamores.noisemeter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.Date;

import android.os.Handler;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.example.miguelamores.data.SQLHelper;

import measureRest.MeasureGet;


public class MeasureActivity extends Activity{

    private MediaRecorder mRecorder = null;
    double powerDb = 0;

    TextView tex;
    Button btnPlay, btnParar, btnMap, btnSave;
    GPSTracker gps;
    Handler handler = new Handler();
    Runnable runnable;
    private SpeedometerGauge speedometer;
    private double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;
    SQLiteDatabase sqLiteDatabase;
    double latitude;
    double longitude;

    private MeasureGet measureGet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);


        tex = (TextView)findViewById(R.id.textView);
        btnPlay = (Button)findViewById(R.id.btnPlay);
        btnParar = (Button)findViewById(R.id.btnParar);
        btnMap = (Button)findViewById(R.id.mapButton);
        btnSave = (Button)findViewById(R.id.saveButton);
        speedometer = (SpeedometerGauge) findViewById(R.id.speedometer);

        measureGet = new MeasureGet(new AsyncResponse() {
            @Override
            public void processFinish(String output) {
                Toast.makeText(getApplicationContext(),output,Toast.LENGTH_LONG).show();
            }
        });
        measureGet.execute("http://192.168.1.5:3000/measure");

        final SQLHelper sqlHelper = new SQLHelper(this);
        sqLiteDatabase = sqlHelper.getWritableDatabase();

        runnable = new Runnable() {
            @Override
            public void run() {
                powerDb = 0;

                tex.setText(String.valueOf(powerDb));

                //powerDb = (20 * Math.log10(getAmplitude()/1));
                //powerDb = (20 * Math.log10(getAmplitude()));
                double amp = getAmplitude();
                //mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
                mEMA = 5*EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;

                //setGauge(powerDb);
                speedometer.setSpeed(mEMA);
                tex.setText(String.valueOf(String.format("%.2f",mEMA))+" dB");
                //tex.setText(String.valueOf(powerDb)+" dB");

                handler.postDelayed(this, 500);
            }
        };

        // Add label converter
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        // configure value range and ticks
        speedometer.setMaxSpeed(100);
        speedometer.setMajorTickStep(10);
        speedometer.setMinorTicks(4);
        speedometer.setLabelTextSize(20);

        // Configure value range colors
        speedometer.addColoredRange(0, 40, Color.GREEN);
        speedometer.addColoredRange(40, 80, Color.YELLOW);
        speedometer.addColoredRange(80, 120, Color.RED);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                gps = new GPSTracker(getApplicationContext());

                if (gps.canGetLocation()) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                    Toast.makeText(
                            getApplicationContext(),
                            "Your location is -\nLat: " + latitude + "\nLong: "
                                    + longitude, Toast.LENGTH_LONG).show();
                } else {
                    gps.showSettingsAlert();
                }


                try {
                    start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 0);

            }
        });

        btnParar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ContentValues contentValues = new ContentValues();
                contentValues.put("valor_db", mEMA);
                contentValues.put("latitud", latitude);
                contentValues.put("longitud", longitude);
                contentValues.put("hora", String.valueOf(new Date()));
                contentValues.put("usuario_id", 1);
                sqLiteDatabase.insert("medicion", null, contentValues);
                Toast.makeText(MeasureActivity.this, "Medicion guardada", Toast.LENGTH_LONG).show();
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MeasureActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_measure, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude()/2700.0);
        //return (mRecorder.getMaxAmplitude()/2700.0);
        else
            return 0;

    }

    public void start() throws IOException {

        if (mRecorder == null) {
            handler.removeCallbacks(runnable);
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            mRecorder.prepare();
            mRecorder.start();

        }
    }

    public void stop() {
        handler.removeCallbacks(runnable);

        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    /*@Override
    public void processFinish(String output) {
        Toast.makeText(getApplicationContext(),output,Toast.LENGTH_LONG).show();
    }*/
}
