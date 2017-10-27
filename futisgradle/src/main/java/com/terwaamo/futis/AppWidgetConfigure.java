package com.terwaamo.futis;


import java.util.HashMap;
import java.util.Map;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.flurry.android.FlurryAgent;
import com.terwaamo.futis.R;


public class AppWidgetConfigure extends ActionBarActivity implements OnClickListener {

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
	
	@Override
	protected void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, "5PNF6S6J3D43KSBWCCYP");
	}
	 
	@Override
	protected void onStop()
	{
		super.onStop();		
		FlurryAgent.onEndSession(this);
	}
	
	private void configureUI()
	{
		// luetaan ja tarkistetaan onko omaa pikalinkki
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String customURIPreference = sharedPref.getString("prefCustomURL", "");
        boolean customURIEnabled = sharedPref.getBoolean("prefCustomURLEnabled", false);
                        	        	       
        Button bookmarkButton=(Button)findViewById(R.id.OwnBookmark);
        
        if(customURIEnabled == false)
        {
        	bookmarkButton.setVisibility(View.GONE);
        }
        else
        {
        	bookmarkButton.setVisibility(View.VISIBLE);
        	if(customURIPreference.equals("http://") || customURIPreference.length() == 0)
            {        	
            	//Log.d("create", "nappi false " + customURIPreference);
            	//Log.d("customURIPreference", customURIPreference);
            	bookmarkButton.setEnabled(false);
            }
            else
            {
            	//Log.d("create", "nappi true " + customURIPreference);
            	//Log.d("customURIPreference", customURIPreference);
            	bookmarkButton.setEnabled(true);
            }
        }
        
        if( android.os.Build.VERSION.SDK_INT < 11 )
        {
        	LinearLayout ll = (LinearLayout)findViewById(R.id.mainMenuLinear);
        	Button b = new Button(this);
        	
        	b.setLayoutParams(new ViewGroup.LayoutParams(
        	        ViewGroup.LayoutParams.MATCH_PARENT,
        	        ViewGroup.LayoutParams.WRAP_CONTENT));
        	b.setOnClickListener(this);
        	b.setText("Asetukset");
        	ll.addView(b, 4);
        	/*
        	 * android:id="@+id/settingsButton"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:onClick="settingsClick"
            android:text="Asetukset" */
        	 
        }
	}
	
	public AppWidgetConfigure() {
        super();
    }
	
	@Override	
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.actionmenubar_main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    if(item.getItemId() == R.id.action_settings)
	    {
        	Intent i = new Intent(this, Preferences.class);
    		startActivity(i);
            return true;	    	   
	    }
	    else if(item.getItemId() == R.id.application_info)
	    {
	    	Intent i = new Intent(this, InfoActivity.class);
    		startActivity(i);
	    	return true;
	    }
	    else
	    {
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        /*setResult(RESULT_CANCELED);

        
     // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {

            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, 
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        else
        {
        	//Log.d("Konffaus","No widget ids");
        	finish();
        }                                  
        */       
        // Set the view layout resource to use.
        setContentView(R.layout.app_widget_configure);
        
        
        configureUI();      
        

    }
	
	@Override
	public void onResume()
	{
		//Log.d("AppWidgetConfigure", "onResume");
		configureUI();
		
		super.onResume();
	}	
	
	
	
	/*public void closeButtonClick(View view){
		
		final Context context = AppWidgetConfigure.this;
	    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	    ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
	    		  											AppWidgetConfigure.class.getName());
	    
	    //N.B.: we want to launch this intent to our AppWidgetProvider!
	    Intent firstUpdate = new Intent(context, Widget.class);
	    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
	    firstUpdate.setAction("android.appwidget.action.APPWIDGET_UPDATE");
	    firstUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
	    
	    
	    context.sendBroadcast(firstUpdate);			   
	    
	    
	
	    	    
		Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);                                    
        setResult(RESULT_OK, resultValue);
        finish(); 
	}*/
			
	
	public void settingsClick(View view)
	{
		Intent i = new Intent(this, Preferences.class);
		startActivity(i);
	}
	
	public void clubWebPageClick(View view)
	{
		FlurryAgent.logEvent("ClubLogo Click");
		Uri uriUrl = Uri.parse(getResources().getString(R.string.club_web_page_address));
		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
		startActivity(launchBrowser);		
	}
	
	public void leagueWebPageClick(View view)
	{
		FlurryAgent.logEvent("LeagueBanner Click");
		Uri uriUrl = Uri.parse(getResources().getString(R.string.league_web_page_address));
		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
		startActivity(launchBrowser);
	}	
	
	public void voteCenterClick(View view)
	{
		FlurryAgent.logEvent("BestPlayerVote view Click");
		Intent intent = new Intent(this, GameCalendar.class);	    
	    startActivity(intent);
		
	}
	
	
	public void ownBookmarkClick(View view)
	{
		// lue asetukset		
	    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String customURIPreference = sharedPref.getString("prefCustomURL", "");       
        
        if (!customURIPreference.startsWith("http://") && !customURIPreference.startsWith("https://"))
        {
        	customURIPreference = "http://" + customURIPreference;
        }
        
        // Flurry event log
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("OwnBookmarkLink", "customURIPreference");          
        FlurryAgent.logEvent("OwnBookmark Click", eventParams);
        
        
        Uri uriUrl = Uri.parse(customURIPreference);
		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
		startActivity(launchBrowser);
	
	}
	 
	public void alarmClockClick(View view)
	{
		FlurryAgent.logEvent("AlarmClock view Click");
		Intent intent = new Intent(this, AlarmClock.class);	    
	    startActivity(intent);
		
	}
	
	public void leagueResultsClick(View view)
	{
		FlurryAgent.logEvent("LeagueResults Click");
		Intent intent = new Intent(this, LeagueResults.class);	    
	    startActivity(intent);
	}

    public void rssNewsClick(View view)
    {
        FlurryAgent.logEvent("RSS News Click");
        Intent intent = new Intent(this, RSSNews.class);
        startActivity(intent);
    }


	// this is here for the dynamic settings button
	@Override
	public void onClick(View arg0) {
		settingsClick(arg0);
		
	}
	
	
}
