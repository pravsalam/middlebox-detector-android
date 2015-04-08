/**
 * Created by praveenkumaralam on 3/23/15.
 */
package edu.stonybrook.middleboxes;

import android.os.AsyncTask;
import android.content.Context;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.app.FragmentManager;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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

public class HtmlTests extends AsyncTask<Object, Void, Map>{

    final String USER_AGENT = "TEST";
    final int HTTP_MOD_YES = 1;
    final int HTTP_MOD_NO = 2;
    final int HTTP_404_MOD = 3;
    final int HTTP_404_NOT_MOD = 4;
    final int HTTP_HOST_OK= 5;
    final int HTTP_HOST_NOT_OK=6;
    @Override
    protected Map doInBackground(Object... params) {
        String local_ip="";
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
                    { local_ip = inetAddress.getHostAddress().toString(); }
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
        int headertest = headerTests(url);
        int http404test = http404Test(url);
        int hostModTest = httpHostTest(url);
        Map testResuls = new HashMap();
        Log.i("INFO",  (new Integer(headertest)).toString());
        if (headertest == HTTP_MOD_YES)
            testResuls.put("Header Modified","Yes");
        else if(headertest == HTTP_MOD_NO)
        {
        	Log.i("INFO",  "why not come here");
            testResuls.put("Header Modified","No");
        }
        else
            testResuls.put("Header Modified","Internal Error");
        
        Log.i("INFO",  (new Integer(http404test)).toString());
        if(http404test == HTTP_404_MOD)
            testResuls.put("Http 404 Modified","Yes");
        else if(http404test == HTTP_404_NOT_MOD)
            testResuls.put("Http 404 Modified","No");
        else
            testResuls.put("Http 404 Modified","Internal Error");
        
        if (hostModTest == HTTP_HOST_OK)
            testResuls.put("Header Host test","Reached our server");
        else if(hostModTest == HTTP_HOST_NOT_OK)
            testResuls.put("Header Host test","Reached google");
        else
            testResuls.put("Header Host test","Internal Error");
        return testResuls;

    }
    private int headerTests(String url)
    {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();
            httpCon.setRequestMethod("GET");
            httpCon.setRequestProperty("User-Agent", USER_AGENT);
            httpCon.setRequestProperty("Accept","*/*");
            httpCon.setRequestProperty("Test-Type","HeaderTest");
            httpCon.setConnectTimeout(1500);

            int responseCode = httpCon.getResponseCode();
            Integer code = responseCode;
            Log.i("INFO",code.toString());
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
            Log.i("INFO",response.toString());
            //return response.toString();
            if(response.toString().equals("HTTP_HEADER_OK"))
            {
                return HTTP_MOD_NO;
            }
            else if( response.toString().equals("HTTP_HEADER_MANIPULATED")){
                return HTTP_MOD_YES;
            }
            else{
                return ErrorConstants.INTERNAL_ERROR;
            }
        } catch (IOException e) {
            //malformed URL do nothing

            Log.i("INFO", e.getLocalizedMessage());
            return ErrorConstants.INTERNAL_ERROR;
        }

    }
    private int http404Test(String url)
    {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();
            httpCon.setRequestMethod("GET");
            httpCon.setRequestProperty("User-Agent", USER_AGENT);
            httpCon.setRequestProperty("Accept","*/*");
            httpCon.setRequestProperty("Test-Type","Http404");
            httpCon.setConnectTimeout(1500);

            Integer responseCode = httpCon.getResponseCode();
            Log.i("INFO",responseCode.toString());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpCon.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            Log.i("INFO",response.toString());
            in.close();
            httpCon.disconnect();
            if(response.toString().equals("HTTP_404"))
                return HTTP_404_NOT_MOD;
            else
                return HTTP_404_MOD;

        }catch(IOException e)
        {
            Log.i("INFO","Something Went wrong");
            return ErrorConstants.INTERNAL_ERROR;
        }
    }
    private int httpHostTest(String url)
    {
    	Log.i("INFO","HTTP HEADER HOST MODIFICATION TEST");
    	try {
            URL urlObj = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();
            httpCon.setRequestMethod("GET");
            httpCon.setRequestProperty("User-Agent", USER_AGENT);
            httpCon.setRequestProperty("Host", "www.google.com");
            httpCon.setRequestProperty("Accept","*/*");
            httpCon.setRequestProperty("Test-Type","HeaderHostTest");
            httpCon.setConnectTimeout(1500);

            Integer responseCode = httpCon.getResponseCode();
            Log.i("INFO",responseCode.toString());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpCon.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            httpCon.disconnect();
            Log.i("INFO",response.toString());
            //return response.toString();
            if(response.toString().equals("MIDDLEBOX_SERVER"))
            {
                return HTTP_HOST_OK;
            }
            else{
                return HTTP_HOST_NOT_OK;
            }

        }catch(IOException e)
        {
            Log.i("INFO","Something Went wrong");
            return ErrorConstants.INTERNAL_ERROR;
        }	
    }


}
