package de.anbeli.dronedelivery.util;

import static android.content.Context.MODE_PRIVATE;
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

import android.content.Context;
import android.content.SharedPreferences;


public class DatabaseConnector {
    static ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    public static final String db_access = "https://vtol.weylyn.net/api/";
    public static int session_id = -1;
    public interface onTaskFinishListener {
        void on_request_completed(JSONObject res) throws JSONException;
    }

    public static void process_async_get_request(String url_add, onTaskFinishListener listener) {
        Runnable backgroundRunnable = () -> {

            String result = "";

            try {
                URL obj = new URL(db_access + url_add);

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
                System.err.println("Error connecting to " + db_access + url_add + " Error: " + e.getMessage());
            }

            try {
                listener.on_request_completed(new JSONObject(result));
            } catch (JSONException e) {
                System.err.println("Parsing JSON response failed");
            }
        };

        mExecutor.execute(backgroundRunnable);
    }

    public static void process_async_post_request(String url_add, String data, onTaskFinishListener listener) {
        Runnable backgroundRunnable = () -> {

            String result = "";

            try {
                URL obj = new URL(db_access + url_add);

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
                System.err.println("Error connecting to " + db_access + url_add);
            }

            try {
                listener.on_request_completed(new JSONObject(result));
            } catch (JSONException e) {
                System.err.println("Parsing JSON response failed");
            }
        };

        mExecutor.execute(backgroundRunnable);
    }

    public static void save_session_id(Context c) {
        SharedPreferences.Editor e = c.getSharedPreferences("save_data", MODE_PRIVATE).edit();
        e.putInt("session_id", DatabaseConnector.session_id);
        e.apply();
        System.out.println("put sessionID " + DatabaseConnector.session_id);
    }
}