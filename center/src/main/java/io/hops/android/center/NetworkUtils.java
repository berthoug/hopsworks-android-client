package io.hops.android.center;

import android.content.Context;
import android.net.ConnectivityManager;


public class NetworkUtils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return (conManager.getActiveNetworkInfo() != null &&
                conManager.getActiveNetworkInfo().isConnected());
    }




}
