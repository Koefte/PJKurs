package de.anbeli.dronedelivery;

import static java.lang.System.out;

import org.json.JSONException;
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
import android.content.SharedPreferences;


public class DatabaseConnector {
    static ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    public static final String db_access = "http://10.0.2.2:3001/api/users";
    public static int session_id = -1;
    public interface onTaskFinishListener {
        void on_request_completed(JSONObject res) throws JSONException;
    }

    public static void process_async_get_request(onTaskFinishListener listener) {
        process_async_get_request(db_access, listener);
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
                con.disconnect();
                in.close();
            } catch (Exception e) {

            }

            try {
                listener.on_request_completed(new JSONObject(result));
            } catch (JSONException e) {
                System.err.println("Parsing JSON response failed");
            }
        };

        mExecutor.execute(backgroundRunnable);
    }

    public static void process_async_post_request(String data, onTaskFinishListener listener) {
        process_async_post_request(db_access, data, listener);
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
                con.disconnect();
                in.close();

            } catch (Exception e) {

            }

            try {
                listener.on_request_completed(new JSONObject(result));
            } catch (JSONException e) {
                System.err.println("Parsing JSON response failed");
            }
        };

        mExecutor.execute(backgroundRunnable);
    }
}
