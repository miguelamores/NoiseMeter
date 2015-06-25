package com.example.miguelamores.noisemeter;

import android.app.Activity;
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
import android.os.Handler;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;


public class MeasureActivity extends Activity {

    private MediaRecorder mRecorder = null;
    double powerDb = 0;

    TextView tex;
    Button btnPlay, btnParar;
    GPSTracker gps;
    Handler handler = new Handler();
    Runnable runnable;
    private SpeedometerGauge speedometer;
    double v=10;
    double v1=100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);


        tex = (TextView)findViewById(R.id.textView);
        btnPlay = (Button)findViewById(R.id.btnPlay);
        btnParar = (Button)findViewById(R.id.btnParar);
        speedometer = (SpeedometerGauge) findViewById(R.id.speedometer);

        runnable = new Runnable() {
            @Override
            public void run() {
                powerDb = 0;

                tex.setText(String.valueOf(powerDb));
                //powerDb = (20 * Math.log10((double) mRecorder.getMaxAmplitude()/2));

                powerDb = (20 * Math.log10(getAmplitude()/1));

                //setGauge(powerDb);
                speedometer.setSpeed(powerDb);
                tex.setText(String.valueOf(String.format("%.2f",powerDb)));

                handler.postDelayed(this, 300);
            }
        };

        // Add label converter
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double v, double v1) {
                return String.valueOf((int) Math.round(v1));
            }
        });

        // configure value range and ticks
        speedometer.setMaxSpeed(120);
        speedometer.setMajorTickStep(10);
        speedometer.setMinorTicks(2);
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
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

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

    public String setGauge(double power){
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double power, double v1) {
                return String.valueOf((int) Math.round(power));
            }
        });
        return null;
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude()+1);
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

}
