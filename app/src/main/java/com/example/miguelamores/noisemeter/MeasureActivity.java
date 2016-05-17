package com.example.miguelamores.noisemeter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.os.Handler;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.example.miguelamores.data.Medicion;
import com.example.miguelamores.data.SQLHelper;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.widgets.SnackBar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import measureRest.MeasureGet;


public class MeasureActivity extends Activity{

    private static final String url = "https://polar-fjord-2695.herokuapp.com";

    private MediaRecorder mRecorder = null;
    double powerDb = 0;

    TextView tex, welcome;
    Button btnMap;
    ButtonRectangle btnPlay, btnParar, btnSave;
    GPSTracker gps;
    Handler handler = new Handler();
    Runnable runnable;
    private SpeedometerGauge speedometer;
    SQLiteDatabase sqLiteDatabase;
    double latitude;
    double longitude;

    String name;
    String mail;
    int idUser;

    private MeasureGet measureGet;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);


        tex = (TextView)findViewById(R.id.textView);
        btnPlay = (ButtonRectangle)findViewById(R.id.btnPlay);
        btnParar = (ButtonRectangle)findViewById(R.id.btnParar);
        btnSave = (ButtonRectangle)findViewById(R.id.saveButton);
        welcome = (TextView)findViewById(R.id.welcomeTextView);
        speedometer = (SpeedometerGauge) findViewById(R.id.speedometer);

        btnParar.setEnabled(false);
        btnSave.setEnabled(false);


        Bundle extras = getIntent().getExtras();
        name = getIntent().getStringExtra("name");
        mail = getIntent().getStringExtra("mail");
        idUser = getIntent().getIntExtra("id", 0);
        welcome.setText("Welcome " + name);
        welcome.setTextColor(Color.WHITE);


        //Obtener mediciones
//        measureGet = new MeasureGet(new AsyncResponseMeasure() {
//            @Override
//            public void getMeasureRest(String output) {
//                //Toast.makeText(getApplicationContext(),output,Toast.LENGTH_LONG).show();
//            }
//
//        });
//        measureGet.execute("https://"+url+"/measure");

        final SQLHelper sqlHelper = new SQLHelper(this);
        sqLiteDatabase = sqlHelper.getWritableDatabase();

        runnable = new Runnable() {
            @Override
            public void run() {
                powerDb = 0;
                tex.setText(String.valueOf(powerDb));
                powerDb = (20 * Math.log10(getAmplitude()));
                speedometer.setSpeed(powerDb);
                tex.setText(String.valueOf(String.format("%.2f",powerDb)));

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

        if(isConnected()){
            sincronizar();
        }


        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnParar.setEnabled(true);
                btnSave.setEnabled(false);
                gps = new GPSTracker(MeasureActivity.this);

                if (gps.canGetLocation()) {

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                    Toast.makeText(
                            getApplicationContext(),
                            "Your location is -\nLat: " + latitude + "\nLong: "
                                    + longitude, Toast.LENGTH_LONG).show();

                    if (latitude != 0 && longitude != 0){
                        try {
                            start();
                            handler.removeCallbacks(runnable);
                            handler.postDelayed(runnable, 0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }else {
                        btnParar.setEnabled(false);
                    }

                } else {
                    stop();
                    btnParar.setEnabled(false);
                    gps.showSettingsAlert();

                }


//                try {
//                    start();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                handler.removeCallbacks(runnable);
//                handler.postDelayed(runnable, 0);

            }
        });

        btnParar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
                btnSave.setEnabled(true);
                gps.stopUsingGPS();
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(latitude != 0 && longitude != 0){
                    Medicion medicion = new Medicion();

                    medicion.setValor_db(powerDb);
                    medicion.setLongitud(longitude);
                    medicion.setLatitud(latitude);
                    medicion.setUsuario_id(idUser);
                    medicion.setTipo_medicion("app");

                    new HttpAsyncTask(medicion).execute(url+"/measure");

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("valor_db", powerDb);
                    contentValues.put("latitud", latitude);
                    contentValues.put("longitud", longitude);
                    contentValues.put("hora", String.valueOf(new Date()));
                    contentValues.put("usuario_id", idUser);
                    if (isConnected()) {
                        contentValues.put("db_externa", Boolean.TRUE);
                    }else {
                        contentValues.put("db_externa", Boolean.FALSE);
                    }
                    sqLiteDatabase.insert("medicion", null, contentValues);
                    Toast.makeText(MeasureActivity.this, "Measure saved!", Toast.LENGTH_LONG).show();
                }else {
                    SnackBar snackBar = new SnackBar(MeasureActivity.this, "Push PLAY button again!");
                    snackBar.show();
                }


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

            try {
                System.out.println("Logout----" + mail);
                ContentValues contentValues = new ContentValues();
                contentValues.put("session", false);
                sqLiteDatabase.update("usuario", contentValues, "email = ?", new String[]{mail});

                Intent intent = new Intent(MeasureActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                //sqLiteDatabase.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (id == R.id.action_map) {
            Intent intent = new Intent(MeasureActivity.this, MapsActivity.class);
            intent.putExtra("id", idUser);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude()+1);
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

    public void sincronizar(){
        String status = null;
        final Cursor cursor;
        cursor = sqLiteDatabase.rawQuery("select valor_db, latitud, longitud, db_externa, medicion_id, usuario_id from medicion",null);

        final Medicion medicion = new Medicion();

            if (cursor.moveToFirst()) {

                do {
                    boolean db_externa = cursor.getInt(3) > 0;
                    if (db_externa == Boolean.FALSE) {
                        medicion.setValor_db(cursor.getDouble(0));
                        medicion.setLatitud(cursor.getDouble(1));
                        medicion.setLongitud(cursor.getDouble(2));
                        medicion.setUsuario_id(cursor.getInt(5));
                        try {
                            status = new HttpAsyncTask(medicion).execute(url + "/measure").get();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MeasureActivity.this, "Sincronizando...", Toast.LENGTH_SHORT).show();
                        if ("201".equals(status)){
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("db_externa", true);
                            sqLiteDatabase.update("medicion", contentValues, "medicion_id=" + cursor.getInt(4), null);
                            Log.i("Login", "User updated to true");
                        }
                    }

                }
                while (cursor.moveToNext());

            }


        cursor.close();

    }


    public static String POST(String url, Medicion medicion){
        InputStream inputStream = null;
        int code = 0;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("value", medicion.getValor_db());
            jsonObject.accumulate("latitude", medicion.getLatitud());
            jsonObject.accumulate("longitude", medicion.getLongitud());
            jsonObject.accumulate("user_id", medicion.getUsuario_id());
            jsonObject.accumulate("measure_type", medicion.getTipo_medicion());

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);
            code = httpResponse.getStatusLine().getStatusCode();
            if(code != 201){
                throw new Exception();
            }

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            //Log.d("InputStream", e.getLocalizedMessage());
            return String.valueOf(code);
        }

        // 11. return result
        return String.valueOf(code);
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        private Medicion medicion;

        private HttpAsyncTask(Medicion medicion) {
            this.medicion = medicion;
        }

        @Override
        protected String doInBackground(String... urls) {

//            double value;
//
//            try {
//                value = new Double(tex.getText().toString());
//            } catch (NumberFormatException e) {
//                value = 0; // your default value
//            }

//            Medicion medicion = new Medicion();
//
//            medicion.setValor_db(powerDb);
//            medicion.setLongitud(longitude);
//            medicion.setLatitud(latitude);
//            medicion.setUsuario_id(idUser);

            return POST(urls[0],medicion);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("201")){
                SnackBar snackBar = new SnackBar(MeasureActivity.this, "Data sent to server.");
                snackBar.show();
            } else {
                SnackBar snackBar = new SnackBar(MeasureActivity.this, "Error sending data to server.");
                snackBar.show();
            }
        }
    }


}
