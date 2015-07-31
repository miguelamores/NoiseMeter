package measureRest;

import android.content.SyncStatusObserver;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.miguelamores.noisemeter.AsyncResponse;
import com.example.miguelamores.noisemeter.R;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by miguelamores on 7/29/15.
 */
public class MeasureGet extends AsyncTask<String, String, String> {

    public AsyncResponse delegate=null;

    public MeasureGet(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... strings) {

        String urlString = strings[0]; // URL to call
        String result = "";

        // HTTP Get
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
        //EditText dynCount = (EditText)findViewById(R.id.dynamicCountEdit);
        //dynCount.setText(result + " records were found");
        delegate.processFinish(result);
        Log.i("FromOnPostExecute", result);
        System.out.println(result);
    }
}
