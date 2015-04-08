package edu.stonybrook.middleboxes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private String server="54.68.198.93";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
        TcpConnectionTests tcpTester = new TcpConnectionTests();
        try {
            Map htmlTests = htmltester.execute(view, server).get();
            Map natTests = natTester.execute(view, server).get();
            Boolean tcpTests = tcpTester.execute(view, server).get();
            allResults.putAll(htmlTests);
            allResults.putAll(natTests);
            TextView resultPane = (TextView)findViewById(R.id.resultsScreen);
            Iterator iter = allResults.keySet().iterator();
            while(iter.hasNext()){
                Object key = iter.next();
                Object value = allResults.get(key);
                if(!((String)value).equals("Internal Error"))
                    allTestsFailed = false;
                outputStr +=(String)key+" : "+(String)value+"\n";
                Log.i("INFO", outputStr);
            }
            outputStr +="TCP RST test: "+tcpTests.toString();
            if(!allTestsFailed)
                resultPane.setText(outputStr);
            else
                resultPane.setText("Check your Internet Connection");
        }catch(Exception e)
        {
            //do Nothing
            e.printStackTrace();
            Log.i("INFO", "Internal Error Occurred");
        }

    }
}
