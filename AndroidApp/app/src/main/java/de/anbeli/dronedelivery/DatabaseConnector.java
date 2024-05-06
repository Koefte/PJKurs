package de.anbeli.dronedelivery;

import static java.lang.System.out;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

                con.connect();

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

    public static void process_async_post_request(String url, String data, onTaskFinishListener listener) {
        Runnable backgroundRunnable = () -> {

            String result = "";

            try {
                URL obj = new URL(url);

                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                OutputStream os = con.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

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
}
