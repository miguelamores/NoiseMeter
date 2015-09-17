package measureRest;

import android.os.AsyncTask;
import android.util.Log;

import com.example.miguelamores.noisemeter.AsyncResponseMeasure;
import com.example.miguelamores.noisemeter.AsyncResponseUser;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by miguelamores on 8/13/15.
 */
public class UserGet extends AsyncTask<String, String, String> {

    public AsyncResponseUser asyncResponseGet=null;

    public UserGet(AsyncResponseUser asyncResponseGet){
        this.asyncResponseGet = asyncResponseGet;
    }

    @Override
    protected String doInBackground(String... strings) {
        String urlString = strings[0]; // URL to call

        String result = "";
        int statusCode = 0;

        // HTTP Get Measure
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            statusCode = urlConnection.getResponseCode();
            InputStream inputStream = urlConnection.getInputStream();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

            if (statusCode != 200){
                throw new Exception();
            }
            if (null != inputStream)
                result = IOUtils.toString(inputStream);
        } catch (SocketTimeoutException s) {
            return "Server is down!";
        } catch (Exception e) {
            System.out.println("Status-----------------> " + statusCode);
            System.out.println(e.getMessage());
            return String.valueOf(statusCode);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {

        asyncResponseGet.getUserRest(result);
        Log.i("FromOnPostExecute", result);
        System.out.println(result);
    }
}
