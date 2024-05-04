package de.anbeli.dronedelivery;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DatabaseConnector {
    static ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public interface onTaskFinishListener {
        void on_request_completed(String res);
    }

    public static void process_async_get_request(String url, onTaskFinishListener listener) {
        Runnable backgroundRunnable = () -> {

            String result = "";

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
            } catch (Exception e) {
                e.printStackTrace();
            }

            listener.on_request_completed(result);
        };

        mExecutor.execute(backgroundRunnable);
    }

    public static void process_async_post_request() {
        Runnable backgroundRunnable = new Runnable(){
            @Override
            public void run(){

            }
        };

        mExecutor.execute(backgroundRunnable);

        //TODO erster connect failed, da nicht auf Concurrent Task gewartet wird, und result deswegen leer ist
    }
}
