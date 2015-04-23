package edu.stonybrook.middleboxes;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

/**
 * Created by praveenkumaralam on 3/30/15.
 */
public class TcpTests extends AsyncTask<Object, Void, Map> {
    private String localIp;
    private String remoteIp;
    private int port = 8080;
    /*static {
        System.loadLibrary("tcptests");
    }*/
    //private native String tcpResetTest(String localIp, String serverIp, int port);
    @Override
    protected Map doInBackground(Object... params) {
    	//System.loadLibrary("tcptests");
        View view = (View)params[0];
        remoteIp = (String)params[1];
        Context context = view.getContext();
        try {
            for (Enumeration<NetworkInterface>
                         en =  NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress>
                             enumIpAddr = intf.getInetAddresses();enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                    	//localIp = inetAddress.getHostAddress().toString(); 
                    	localIp = Formatter.formatIpAddress(inetAddress.hashCode());
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            Log.e("ServerActivity", ex.toString());
            return null;
        }
        Log.i("INFO","localIp= "+localIp);
        try {
			Process suProcess = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
			DataInputStream osRes = new DataInputStream(suProcess.getInputStream());
			if(os !=null && osRes != null)
			{
				os.writeBytes("id\n");
				os.flush();
				String currUid = osRes.readLine();
				if(currUid == null)
				{
					Log.i("INFO","could't get root");
					return null;
				}
				else if(currUid.contains("uid=0")== true)
				{
					Log.i("INFO","got root access");
					os.writeBytes("tcptests");
					os.flush();
					String outputString = osRes.readLine();
					Log.i("INFO","Command output"+outputString);
				}
			}
			else
			{
				Log.i("INFO","Su didn't work");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //String ente = tcpResetTest(localIp, remoteIp, port);
        Log.i("INFO", "exiting tcptests.java");
     return null;
    }
}
