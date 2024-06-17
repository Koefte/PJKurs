package de.anbeli.dronedelivery.util;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.System.out;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import javax.net.ssl.HttpsURLConnection;


public class DatabaseConnector {
    static ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    //Address of Server
    public static final String db_access = "https://vtol.weylyn.net/api/";
    public static long session_id = -1;
    public interface onTaskFinishListener {
        void on_request_completed(JSONObject res) throws JSONException;
    }

    //Currently no use

    /*public static void process_async_get_request(String url_add, onTaskFinishListener listener) {
        Runnable backgroundRunnable = () -> {

            String result = "";

            try {
                URL obj = new URL(db_access + url_add);

                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
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
    }*/

    //Method for posting to Server

    public static void process_async_post_request(String url_add, String data, onTaskFinishListener listener) {
        Runnable backgroundRunnable = () -> {

            String result = "";

            try {
                HttpURLConnection con = get_http_url_connection(url_add);

                //push data into output stream to send to server

                OutputStream os = con.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                //read input with BufferedReader

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                result = response.toString();
                con.disconnect();
                in.close();

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error connecting to " + db_access + url_add);
            }

            try {
                System.out.println(result);
                listener.on_request_completed(new JSONObject(result));
            } catch (JSONException e) {
                System.err.println("Parsing JSON response failed");
            }
        };

        mExecutor.execute(backgroundRunnable);
    }

    private static HttpURLConnection get_http_url_connection(String url_add) throws IOException {
        URL obj = new URL(db_access + url_add);

        HttpURLConnection con;

        //Check if connecting to http or https
        if(obj.openConnection() instanceof HttpsURLConnection) {
            con = (HttpsURLConnection) obj.openConnection();
        } else {
            con = (HttpURLConnection) obj.openConnection();
        }
        con.setDoOutput(true);
        con.setDoInput(true);

        //Set post type request and json as format

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        return con;
    }

    public static void save_session_id(Context c) {
        //save session ID in SharedPreferences
        SharedPreferences.Editor e = c.getSharedPreferences("save_data", MODE_PRIVATE).edit();
        e.putLong("session_id", DatabaseConnector.session_id);
        e.apply();
        System.out.println("put sessionID " + DatabaseConnector.session_id);
    }
}
