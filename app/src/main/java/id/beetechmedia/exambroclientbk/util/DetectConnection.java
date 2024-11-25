package id.beetechmedia.exambroclientbk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Sabiqul on 23/11/2024.
 * BeeMedia
 * sabiqul.ulum@gmail.com
 */
public class DetectConnection {
    public static boolean isNetworkStatusAvialable (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if(netInfos != null)
                //noinspection deprecation
                if(netInfos.isConnected())
                    return true;
        }
        return false;
    }
}