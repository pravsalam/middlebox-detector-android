package edu.stonybrook.middleboxes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mobilyzer.MeasurementResult;
import com.mobilyzer.MeasurementScheduler.DataUsageProfile;
import com.mobilyzer.MeasurementScheduler.TaskStatus;
import com.mobilyzer.MeasurementTask;
import com.mobilyzer.UpdateIntent;
import com.mobilyzer.api.API;
import com.mobilyzer.exceptions.MeasurementError;

public class MainActivity extends Activity {
	private String server="54.187.128.15";
	private API api;
	private BroadcastReceiver broadcastReceiver;
	private String clientKey;
	private ListView consoleView;
	private ArrayAdapter<String> resultList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.consoleView = (ListView) this.findViewById(R.id.resultConsole);
	    this.resultList = new ArrayAdapter<String>(getApplicationContext(),
	        R.layout.list_item);
	    this.consoleView.setAdapter(this.resultList);
	    resultList.clear();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	public void runAllTests(View view) {
        //do nothing
        //Get the unique Android ID
        Map allResults = new HashMap();
        boolean allTestsFailed = true;
        String outputStr="";
        HtmlTests htmltester = new HtmlTests();
        NatTests natTester = new NatTests();
        UploadResults resUploader = new UploadResults();
        TcpTests tcpTester = new TcpTests();
        TcpConnectionTests tcpConnTester = new TcpConnectionTests();
        try {
            Map htmlTests = htmltester.execute(view, server).get();
            Map natTests = natTester.execute(view, server).get();
            Boolean tcpTests = tcpConnTester.execute(view, server).get();
            String ttlLoc = tcpTester.execute(view, server).get();
            allResults.putAll(htmlTests);
            allResults.putAll(natTests);
            allResults.put("TCP_RST", tcpTests.toString());
            allResults.put("TTL loc",ttlLoc);
            //TextView resultPane = (TextView)findViewById(R.id.resultsScreen);
            Iterator iter = allResults.keySet().iterator();
            while(iter.hasNext()){
                Object key = iter.next();
                Object value = allResults.get(key);
                if(!((String)value).equals("Internal Error"))
                    allTestsFailed = false;
                outputStr +=(String)key+" : "+(String)value+"\n";
                //Log.i("INFO", outputStr);
            }
            //outputStr +="TCP RST test: "+tcpTests.toString();
            if(!allTestsFailed)
            {
                resultList.insert(outputStr,0);
                // upload result to server
                Log.i("INFO",outputStr);
                resUploader.execute(view, server, allResults);
            }
            else
                resultList.insert("Check your Internet Connection",0);
            Log.i("INFO", "Whats with new layout");
        }catch(Exception e)
        {
            //do Nothing
            e.printStackTrace();
            Log.i("INFO", "Internal Error Occurred");
        }
        runOnUiThread(new Runnable() {
            public void run() { resultList.notifyDataSetChanged(); }
          });
    }
}
