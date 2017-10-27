package com.terwaamo.futis;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;



public class HttpWorker extends AsyncTask<String, Void, String> {
	
	private Context mContext;
	private NetworkResultHandler mResultHandler;
	private boolean mSecureModeOn = false;
	
	public void setResultHandler(NetworkResultHandler handler)
	{
		mResultHandler = handler;
	}
	
	public void setKahakkaSecure()
	{
		mSecureModeOn = true;
	}
	
	public void setContext(Context ctx)
	{
		mContext=ctx;
	}
	
    @Override
    protected String doInBackground(String... params) {    	                         
        HttpResponse response;
        String link = null;
        if(mSecureModeOn == false)
        {
        	link = params[0];
        }
        else
        {
        	MCrypt mcrypt = new MCrypt();
    		String encryptedurldata =null;
			try {
				encryptedurldata = MCrypt.bytesToHex( mcrypt.encrypt(params[0]) );
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	//link = "http://78.129.251.194/kahakka/service.php?data="+encryptedurldata;
			link = mContext.getResources().getText(R.string.serverAddress) + "service.php?data=" + encryptedurldata;
        }
        
        //Log.d("HttpWorker", "URL string " + link);
        HttpGet request = new HttpGet(link);
        
        DefaultHttpClient client = new DefaultHttpClient();
        try {
        	response = client.execute(request);        	
        	StatusLine statusLine = response.getStatusLine();
        	if(statusLine.getStatusCode() == HttpStatus.SC_OK)
        	{
        		return EntityUtils.toString(response.getEntity());
        	}
        	else
        	{
        		//Closes the connection.               
                throw new IOException(statusLine.getReasonPhrase());
        	}
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
        	//client.close();
        }
    }

    @Override
    protected void onPostExecute(String result) {
    	super.onPostExecute(result);
    	
    	if(mResultHandler != null)
    	{
    		if(mSecureModeOn)
    		{
    			MCrypt mcrypt = new MCrypt();
				/* Decrypt */
				try {
					result = new String( mcrypt.decrypt( result ) );
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					result = null;
				}
    		}
    		mResultHandler.processStringResult(result);
			
    		
    	}
    	
    }

}
