package edu.stonybrook.middleboxes;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

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
public class TcpTests extends AsyncTask<Object, Void, String> {
    private String localIp="";
    private String remoteIp;
    private int port = 80;
    private String wifiIpString;
    
    /*static {
        System.loadLibrary("tcptests");
    }*/
    //private native String tcpResetTest(String localIp, String serverIp, int port);
    @Override
    protected String doInBackground(Object... params) {
    	Log.i("INFO","Inside TCPTest native code");
    	//System.loadLibrary("tcptests");
        View view = (View)params[0];
        //remoteIp = (String)params[1];
        remoteIp = "173.194.123.80";
        Context context = view.getContext();
        Log.i("INFO","good for who");
        String outputString  = "Internal Error";
        
        try{
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if(info != null )
        	Log.i("INFO","network type = "+info.getTypeName());
        }catch(Exception e)
        {
        	e.printStackTrace();
        }
        try{
	        WifiManager mgr = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	        int wifiIP = mgr.getConnectionInfo().getIpAddress();
	        wifiIpString = String.format(
	        		   "%d.%d.%d.%d",
	        		   (wifiIP & 0xff),
	        		   (wifiIP >> 8 & 0xff),
	        		   (wifiIP >> 16 & 0xff),
	        		   (wifiIP >> 24 & 0xff));
	        Log.i("INFO"," wifi ip"+ wifiIpString);
        }catch(Exception e)
        {
        	e.printStackTrace();
        }

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
                    	String someString = inetAddress.getHostAddress().toString();
                    	if(!inetAddress.isLinkLocalAddress())
                    	{
                    		localIp = someString;
                    	}
                    	//localIp = Formatter.formatIpAddress(inetAddress.hashCode());
                    	Log.i("INFO","Network name is "+intf.getDisplayName());
                    	Log.i("INFO",someString+" "+localIp);
                    	
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            Log.e("ServerActivity", ex.toString());
            return outputString;
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
					return outputString;
				}
				else if(currUid.contains("uid=0")== true)
				{
					Log.i("INFO","got root access");
					String command = "/data/tmp/tcptests "+localIp+" "+remoteIp+" "+(new Integer(port)).toString()+"\n";
					Log.i("INFO",command);
					os.writeBytes(command);
					os.flush();
					 outputString = osRes.readLine();
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
     return outputString;
    }
}
