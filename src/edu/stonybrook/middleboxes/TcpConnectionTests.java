package edu.stonybrook.middleboxes;

import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class TcpConnectionTests extends AsyncTask<Object, Void, Boolean>{

	@Override
	protected Boolean doInBackground(Object... params) {
		// TODO Auto-generated method stub
		View view = (View)params[0];
        String server = (String)params[1];
        Context context = view.getContext();
        try{
        	//server sends TCP RST on port 8081, check if the connection still succeed.
        	Socket s = new Socket(server, 8081);
        	//since no error was thrown, a connection was successful. 
        	SocketAddress sockaddr = s.getRemoteSocketAddress();
        	Log.i("INFO",sockaddr.toString());
        	return true;
        }catch(Exception e)
        {
        	e.printStackTrace();
        	Log.i("INFO","Unable to connect to server");
        	return false;
        }
	}

}
