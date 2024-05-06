package de.anbeli.dronedelivery;

import android.util.JsonWriter;

import org.json.JSONException;
import org.json.JSONObject;

public class Util {
    public static String build_user_obj_string(String name, String mail, String password) throws JSONException {
        String jsonString = new JSONObject()
                .put("name", name)
                .put("email", mail)
                .put("passwort", password)
                .toString();

        return jsonString;
    }

    public static String build_login_obj_string(String mail, String password) throws JSONException {
        String jsonString = new JSONObject()
                .put("email", mail)
                .put("passwort", password)
                .toString();

        return jsonString;
    }

    public static String escape(String jsString) {
        jsString = jsString.replace("\\", "\\\\");
        jsString = jsString.replace("\"", "\\\"");
        jsString = jsString.replace("\b", "\\b");
        jsString = jsString.replace("\f", "\\f");
        jsString = jsString.replace("\n", "\\n");
        jsString = jsString.replace("\r", "\\r");
        jsString = jsString.replace("\t", "\\t");
        jsString = jsString.replace("/", "\\/");
        return jsString;
    }
}
