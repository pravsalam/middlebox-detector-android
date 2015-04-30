package edu.stonybrook.middleboxes;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by praveenkumaralam on 3/26/15.
 */
public class NatTests extends AsyncTask<Object, Void, Map>{
    final String USER_AGENT = "TEST";
    private String local_ip="";
    private String publicIp="";
    private String public_ip_port80="";
    private int publicPort=0;
    private int NAT_PROXY_YES = 1;
    private int NAT_PROXY_NO = 2;
    @Override
    protected Map doInBackground(Object... params) {
        View view = (View)params[0];
        String server = (String)params[1];
        Context context = view.getContext();
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        TelephonyManager manager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        String networkOperator = manager.getNetworkOperatorName();
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
                    	local_ip = Formatter.formatIpAddress(inetAddress.hashCode());
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            Log.e("ServerActivity", ex.toString());
        }
        String url = "http://";
        url += server + ":8080/?" + "unique_id=" + androidID + "&network_operator=" + networkOperator;
        url +="&local_ip="+local_ip;
        Log.i("INFO", url);
        int natProxy = runNatTest(url);
        
        String url_port80 = "http://"+server+":80";
        int flipTestResult = runIpFlipTest(url_port80);
        Map testResults  = new HashMap();
        if(natProxy == NAT_PROXY_YES )
        {
            testResults.put("NATProxy","Yes");
            testResults.put("Device_local_IP",local_ip);
            testResults.put("Device_public_IP",publicIp);
        }
        else if(natProxy == NAT_PROXY_NO)
        {
            testResults.put("NATProxy","No");
        }
        else
            testResults.put("NATProxy","Internal Error");
        if(flipTestResult == ErrorConstants.SUCCESS)
        {
        	if(publicIp.equals(public_ip_port80))
        	{
        		testResults.put("IpFlipping","No");
        	}
        	else
        		testResults.put("IpFlipping","Yes");
        }
        else{
        	testResults.put("IpFlipping ", "Internal Error");
        }
        return testResults;
    }
    private int runNatTest(String url) {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();
            httpCon.setRequestMethod("GET");
            httpCon.setRequestProperty("User-Agent", USER_AGENT);
            httpCon.setRequestProperty("Accept", "*/*");
            httpCon.setRequestProperty("Test-Type", "NatTest");
            httpCon.setConnectTimeout(1500);

            int responseCode = httpCon.getResponseCode();
            Integer code = responseCode;
            Log.i("INFO", code.toString());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpCon.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            Map<String, List<String>> map = httpCon.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                Log.i("INFO", entry.getKey() + " : " + entry.getValue());
            }

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            httpCon.disconnect();
            Log.i("INFO", response.toString());
            try {
                JSONObject parentJsonObj = new JSONObject(response.toString());
                JSONObject childJsonObj = (JSONObject) parentJsonObj.get("NatProxy");
                if (((String) childJsonObj.get("NatPresent")).equals("Yes")) {
                    publicIp = (String) childJsonObj.get("clientIp");
                    publicPort = (Integer)childJsonObj.get("clientPort");
                    return NAT_PROXY_YES;
                } else
                    return NAT_PROXY_NO;

            } catch (Exception e) {
                e.printStackTrace();
                return ErrorConstants.INTERNAL_ERROR;
            }
        } catch (IOException e) {
            //malformed URL do nothing

            Log.i("INFO", e.getLocalizedMessage());
            return ErrorConstants.INTERNAL_ERROR;
        }
    }
    
    private int runIpFlipTest(String url) {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();
            httpCon.setRequestMethod("GET");
            httpCon.setRequestProperty("User-Agent", USER_AGENT);
            httpCon.setRequestProperty("Accept", "*/*");
            httpCon.setConnectTimeout(2400);

            int responseCode = httpCon.getResponseCode();
            Integer code = responseCode;
            Log.i("INFO", code.toString());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpCon.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            Map<String, List<String>> map = httpCon.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                Log.i("INFO", entry.getKey() + " : " + entry.getValue());
            }

            if ((inputLine = in.readLine()) != null) {
                public_ip_port80 = inputLine;
            }
            in.close();
            httpCon.disconnect();
            return ErrorConstants.SUCCESS;
        } catch (IOException e) {
            //malformed URL do nothing

            Log.i("INFO", e.getLocalizedMessage());
            return ErrorConstants.INTERNAL_ERROR;
        }
    }
}
