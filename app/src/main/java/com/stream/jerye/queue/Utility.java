package com.stream.jerye.queue;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jerye on 7/8/2017.
 */

public class Utility {
    public static final String SPOTIFY_TOKEN = "token";

    public static void setPreference(Context context, String preferenceKey, String value) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        switch (preferenceKey) {
            case "token":
                prefs.edit().putString("token", value).apply();

        }
    }

    public static String getPreference(Context context, String preferenceKey) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        switch (preferenceKey) {
            case SPOTIFY_TOKEN:
                return prefs.getString(SPOTIFY_TOKEN, "");
            default:
                return "";
        }

    }
}

