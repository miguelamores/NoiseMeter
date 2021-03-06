package measureRest;

import android.os.AsyncTask;
import android.util.Log;

import com.example.miguelamores.noisemeter.AsyncResponseMeasure;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by miguelamores on 7/29/15.
 */
public class MeasureGet extends AsyncTask<String, String, String> {

    public AsyncResponseMeasure asyncResponseMeasureGet =null;

    public MeasureGet(AsyncResponseMeasure asyncResponseMeasureGet){
        this.asyncResponseMeasureGet = asyncResponseMeasureGet;
    }

    @Override
    protected String doInBackground(String... strings) {

        String urlString = strings[0]; // URL to call
        String result = "";

        // HTTP Get Measure
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            if (null != inputStream)
                result = IOUtils.toString(inputStream);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
        return result;

    }

    @Override
    protected void onPostExecute(String result) {

        asyncResponseMeasureGet.getMeasureRest(result);
        Log.i("FromOnPostExecute", result);
        System.out.println(result);
    }
}
