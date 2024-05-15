package de.anbeli.dronedelivery;

import android.util.JsonWriter;

import org.json.JSONException;
import org.json.JSONObject;

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
}
