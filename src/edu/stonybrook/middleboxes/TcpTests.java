package edu.stonybrook.middleboxes;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

/**
 * Created by praveenkumaralam on 3/30/15.
 */
public class TcpTests extends AsyncTask<Object, Void, Map> {
    private String local_ip="";
    private int port = 12123;
    static {
        System.loadLibrary("tcptests");
    }
    private native String tcpResetTest();
    @Override
    protected Map doInBackground(Object... params) {
    	System.loadLibrary("tcptests");
        View view = (View)params[0];
        String server = (String)params[1];
        Context context = view.getContext();
        String ente = tcpResetTest();
        Log.i("INFO", ente);
     return null;
    }
}
