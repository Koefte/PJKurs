package de.anbeli.dronedelivery;

import android.media.metrics.Event;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DatabaseConnector {
    ExecutorService mExecutor;
    Handler mHandler;
    String url;
    String result = "";
    public DatabaseConnector(String url) {
         mExecutor = Executors.newSingleThreadExecutor();
         mHandler = new Handler(Looper.getMainLooper());
         this.url = url;
    }
    public String process_async_get_request() {
        Runnable backgroundRunnable = new Runnable(){
            @Override
            public void run(){
                try {
                    URL obj = new URL(url);

                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");

                    // Read the response body
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    result = response.toString();

                    in.close();
                    con.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mExecutor.execute(backgroundRunnable);

        //TODO erster connect failed, da nicht auf Concurrent Task gewartet wird, und result deswegen leer ist

        if(result.equals(""))
            return "ERROR CONNECTING TO URL : " + url;
        return result;
    }
}
