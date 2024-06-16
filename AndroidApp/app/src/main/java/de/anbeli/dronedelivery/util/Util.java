package de.anbeli.dronedelivery.util;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.anbeli.dronedelivery.data.Delivery;
import de.anbeli.dronedelivery.data.Drone;

public class Util {
    public static String build_user_obj_string(String name, String mail, String password) {
        String jsonString = null;
        try {
            jsonString = new JSONObject()
                    .put("name", name)
                    .put("email", mail)
                    .put("passwort", password)
                    .toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonString;
    }

    public static String build_login_obj_string(String mail, String password) {
        String jsonString = null;
        try {
            jsonString = new JSONObject()
                    .put("email", mail)
                    .put("passwort", password)
                    .toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonString;
    }

    public static String build_request_a_obj_string(String receiverEmail) {
        String jsonString = null;
        try {
            jsonString = new JSONObject()
                    .put("sessionID", DatabaseConnector.session_id)
                    .put("receiver", receiverEmail)
                    .toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonString;
    }

    public static String build_session_id_obj_string() {
        String jsonString = null;
        try {
            jsonString = new JSONObject()
                    .put("sessionID", DatabaseConnector.session_id)
                    .toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonString;
    }


    public static String build_acceptor_session_id_obj_string() {
        String jsonString = null;
        try {
            jsonString = new JSONObject()
                    .put("acceptorSession", DatabaseConnector.session_id)
                    .toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonString;
    }

    public static String build_request_b_obj_string(LatLng location, Delivery delivery) {
        String jsonString = null;
        String location_str = location.latitude + "," + location.longitude;

        try {
            jsonString = new JSONObject()
                    .put("sessionID", DatabaseConnector.session_id)
                    .put("receiver", delivery.get_receiver())
                    .put("geoString", location_str)
                    .toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonString;
    }

    public static String build_owner_session_id_obj_string() {
        String jsonString = null;
        try {
            jsonString = new JSONObject()
                    .put("ownerSession", DatabaseConnector.session_id)
                    .toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jsonString;
    }

    public static ArrayList<Delivery> parse_fetch_deliveries_outgoing(JSONObject toParse) {
        ArrayList<Delivery> parsed = new ArrayList<>();
        try {
            JSONArray rawArray = toParse.getJSONArray("out");

            JSONObject[] deliveries = new JSONObject[rawArray.length()];

            for(int i = 0; i < rawArray.length(); i++) {
                deliveries[i] = (JSONObject) rawArray.get(i);
            }

            for(JSONObject o : deliveries) {
                String receiver_email = o.getString("receiver");

                if (o.has("geoString")) {
                    parsed.add(new Delivery(receiver_email, Delivery.delivery_state.TO_BE_DELIVERED, o.getString("geoString")));

                } else if(!o.getBoolean("accepted")) {
                    parsed.add(new Delivery(receiver_email, Delivery.delivery_state.TO_BE_CONFIRMED));
                }


            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return parsed;
    }

    public static ArrayList<Delivery> parse_fetch_deliveries_incoming(JSONObject toParse) {
        ArrayList<Delivery> parsed = new ArrayList<>();
        try {
            JSONArray rawArray = toParse.getJSONArray("in");

            JSONObject[] deliveries = new JSONObject[rawArray.length()];

            for(int i = 0; i < rawArray.length(); i++) {
                deliveries[i] = (JSONObject) rawArray.get(i);
            }

            for(JSONObject o : deliveries) {
                String receiver_email = o.getString("sender");

                if (o.has("geoString")) {
                    parsed.add(new Delivery(receiver_email, Delivery.delivery_state.TO_BE_DELIVERED, o.getString("geoString")));

                } else if(!o.getBoolean("accepted")) {
                    parsed.add(new Delivery(receiver_email, Delivery.delivery_state.TO_BE_CONFIRMED));
                }


            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return parsed;
    }

    public static ArrayList<Drone> parse_fetch_drones(JSONObject toParse) {
        ArrayList<Drone> parsed = new ArrayList<>();

        System.out.println("teeesst");

        return parsed;
    }
}
