package com.example.miguelamores.noisemeter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.miguelamores.data.Medicion;
import com.example.miguelamores.data.SQLHelper;
import com.example.miguelamores.data.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import measureRest.UserGet;


public class LoginActivity extends Activity {

    private Button btnIngresar, register;
    private TextView signIn, signUp;
    private EditText name, email, password;

    private UserGet userGet;
    String  statusCode, id, userName, sqliteEmail, sqlitePassword;

    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final SQLHelper sqlHelper = new SQLHelper(LoginActivity.this);
        sqLiteDatabase = sqlHelper.getWritableDatabase();
        Cursor cursor;

        cursor = sqLiteDatabase.query("usuario", new String[]{"session", "_id", "nombre"}, null, null, null, null, null);


        if (cursor.moveToFirst()) {
            do {
                boolean sessionBool = cursor.getInt(0)>0;
                if (sessionBool){

                    Intent intent = new Intent(LoginActivity.this, MeasureActivity.class);
                    intent.putExtra("id", cursor.getInt(1));
                    intent.putExtra("name", cursor.getString(2));
                    startActivity(intent);
                    System.out.println("Encontrado BOOLEAN");
                    break;
                }

            }
            while (cursor.moveToNext());
        }
        cursor.close();



        if(!isConnected()){
            Toast.makeText(getApplicationContext(), "You don't have internet connection", Toast.LENGTH_LONG).show();
        }


        signIn = (TextView) findViewById(R.id.sigInTextView);
        signUp = (TextView) findViewById(R.id.signUpTextView);
        name = (EditText) findViewById(R.id.nameEditText);
        email = (EditText) findViewById(R.id.emailEditText);
        password = (EditText) findViewById(R.id.passwordEditText);
        register = (Button) findViewById(R.id.registerButton);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //signUp.setTextColor(getResources().getColor(R.color.material_blue_grey_950));
                signUp.setTextColor(Color.WHITE);
                signIn.setTextColor(Color.GRAY);
                name.setVisibility(View.VISIBLE);
                register.setVisibility(View.VISIBLE);
                btnIngresar.setVisibility(View.INVISIBLE);
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //signIn.setTextColor(getResources().getColor(R.color.accent_material_dark));
                signIn.setTextColor(Color.WHITE);
                signUp.setTextColor(Color.GRAY);
                name.setVisibility(View.INVISIBLE);
                register.setVisibility(View.INVISIBLE);
                register.setVisibility(View.INVISIBLE);
                btnIngresar.setVisibility(View.VISIBLE);
            }
        });

        btnIngresar = (Button) findViewById(R.id.loginButton);

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userGet = new UserGet(new AsyncResponseUser() {
                    @Override
                    public void getUserRest(String response) {
                        statusCode = response;
                        try {
                            JSONObject jsonObject = new JSONObject(statusCode);
                            id = jsonObject.get("id").toString();
                            userName = jsonObject.get("name").toString();
                            sqliteEmail = jsonObject.getString("mail").toString();
                            sqlitePassword = jsonObject.getString("password").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                userGet.execute("http://192.168.1.5:3000/user" + "?mail=" + email.getText().toString() + "&password=" + password.getText().toString());


                // Validate login data
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (statusCode.equals("404")){
                            Toast.makeText(getApplicationContext(), "Email or password are incorrect "+ statusCode, Toast.LENGTH_LONG).show();
                        }else {

                            Cursor cursor;
                            cursor = sqLiteDatabase.query("usuario", new String[]{"_id"}, null, null, null, null, null);

                            if (cursor.moveToFirst()) {
                                do {
                                    if (String.valueOf(cursor.getInt(0)).equals(id)){
                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put("session", true);
                                        sqLiteDatabase.update("usuario", contentValues, "_id=" + id, null);
                                        System.out.println("Encontrado");
                                        break;
                                    }
                                    else {
                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put("_id", Integer.parseInt(id));
                                        contentValues.put("nombre", userName);
                                        contentValues.put("email", sqliteEmail);
                                        contentValues.put("contrasena", sqlitePassword);
                                        contentValues.put("session", true);
                                        sqLiteDatabase.insert("usuario",null,contentValues);
                                        System.out.println("Guardado--------------");
                                        //Toast.makeText(getApplicationContext(), "No existe", Toast.LENGTH_SHORT).show();
                                    }

                                }
                                while (cursor.moveToNext());
                            }else {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("_id", Integer.parseInt(id));
                                contentValues.put("nombre", userName);
                                contentValues.put("email", sqliteEmail);
                                contentValues.put("contrasena", sqlitePassword);
                                contentValues.put("session", true);
                                sqLiteDatabase.insert("usuario",null,contentValues);
                                System.out.println("guardado00000000000000000");
                            }
                            cursor.close();



                            Intent intent = new Intent(LoginActivity.this, MeasureActivity.class);
                            intent.putExtra("id", id);
                            intent.putExtra("mail", email.getText().toString());
                            intent.putExtra("name", userName);
                            startActivity(intent);
                            finish();
                        }
                    }
                }, 3500);




            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new HttpAsyncTask().execute("http://192.168.1.5:3000/user");

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    // Register new user
    public static String POST(String url, User user){
        InputStream inputStream = null;
        String result = "";
        int code = 0;
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("name", user.getName());
            jsonObject.accumulate("mail", user.getEmail());
            jsonObject.accumulate("password", user.getPassword());


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
            if(code == 500){
                throw new Exception();
            }
            System.out.println("Status------------------> " + code);
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

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

//            double value;
//
//            try {
//                value = new Double(tex.getText().toString());
//            } catch (NumberFormatException e) {
//                value = 0; // your default value
//            }

            User user = new User();

            user.setName(name.getText().toString());
            user.setEmail(email.getText().toString());
            user.setPassword(password.getText().toString());


            return POST(urls[0],user);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if(result.equals("201")){
                Toast.makeText(getBaseContext(), "Create success", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, MeasureActivity.class);
                startActivity(intent);
                finish();
            }else {
                Toast.makeText(getBaseContext(), "Failure in create user", Toast.LENGTH_LONG).show();
            }

        }
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


}
