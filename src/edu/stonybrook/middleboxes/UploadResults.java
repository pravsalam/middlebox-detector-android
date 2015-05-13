/**
 * 
 */
package edu.stonybrook.middleboxes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

/**
 * @author praveenkumaralam
 *
 */
public class UploadResults extends AsyncTask<Object, Void, Integer> {

	private String local_ip;
	@Override
	protected Integer doInBackground(Object... params) {
		// TODO Auto-generated method stub
		// make a connection to server and deliver the results
		  	View view = (View)params[0];
	        String server = (String)params[1];
	        Map results = (Map)params[2];
	        Context context = view.getContext();
	        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
	        TelephonyManager manager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
	        String networkOperator = manager.getNetworkOperatorName().replaceAll("\\s","");
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
	                    		local_ip = someString;
	                    	}
	                    }
	                }
	            }
	        }
	        catch (SocketException ex)
	        {
	            Log.e("ServerActivity", ex.toString());
	        }
	        results.put("AndroidId",androidID);
	        results.put("operator", networkOperator);
	        //results.put("DeviceIp", local_ip);
	        int natProxy = uploadToServer(context, server,results);
		return null;
	}
	private int uploadToServer(Context cntxt, String serverIp, Map Results)
	{
		Log.i("INFO","Uploading results to server" );
		Resources res = cntxt.getResources();
		String servPort = res.getString(R.string.dataColPort);
		int port = Integer.parseInt(servPort);
		try
		{
			Socket sock = new Socket(InetAddress.getByName(serverIp), port);
			DataOutputStream outputStream = new DataOutputStream(sock.getOutputStream());
			DataInputStream inputStream = new DataInputStream(sock.getInputStream());
			// send the Map to server
			JSONObject json = new JSONObject(Results);
			String resultString = json.toString();
			outputStream.writeUTF(resultString);
			Log.i("INFO",resultString);
			String responseLine;
			/*while((responseLine = inputStream.readUTF()) != null)
			{
				if(responseLine.equals("OK"))
					break;
			}*/
			return ErrorConstants.SUCCESS;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return ErrorConstants.INTERNAL_ERROR;
		}
	}

}
