package com.terwaamo.futis;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.text.DateFormat;
import com.flurry.android.FlurryAgent;

import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;


 
 
public class Widget extends AppWidgetProvider {	
	
	private static final String TAG = "SeurakelloWidget";	
	 
	@Override
	public void onUpdate (Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		Log.i(TAG, "onUpdate");
		
		/*Log.i("Seurakello", "DPI" + context.getResources().getDisplayMetrics().densityDpi);
		Log.i("Seurakello", "density" + context.getResources().getDisplayMetrics().density);
		Log.i("Seurakello", "widthPixels" + context.getResources().getDisplayMetrics().widthPixels);
		Log.i("Seurakello", "heightPixels" + context.getResources().getDisplayMetrics().heightPixels);
		 */
		 
		
 
		updateWidgetWithLayout(context);			
        
        // Wallpaper
        /*WallpaperManager wpm = WallpaperManager.getInstance(context);
        try {
            wpm.setResource(R.drawable.tausta_kentta);                  
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
        } */      
                     
        
       
	}
	
	@Override
    public void onEnabled(Context context) {
        Log.i(TAG, "onEnabled");
        FlurryAgent.logEvent("Kellowidget aktivoitu");
        ClockStarter.startAlarmManagerForClock(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.i(TAG,"onDisabled");
        
        // Poistetaan alarm managerin asetus
        Intent intent = new Intent(context, Widget.class);
        
	    intent.setAction("com.terwaamo.futis.kups.AIKALEIMA");
	    PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);        
        alarmManager.cancel(pi);
        
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.i(TAG, String.format("onDeleted"));     
        FlurryAgent.logEvent("Kellowidget poistettu");
        
        super.onDeleted(context, appWidgetIds);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	//Log.i(TAG, String.format("onReceive")); 
    	/*Bundle extras = intent.getExtras();    	
       	  
    	if(extras != null)
    	{
    		for (String key : extras.keySet()) {
        	    Object value = extras.get(key);
        	    Log.d(TAG, String.format("%s %s (%s)", key,  
        	        value.toString(), value.getClass().getName()));
        	}
    	}*/
    	
    	
    	String action = intent.getAction();
    	//Log.d(TAG, action.toString());
    	//if((action != null) && (action.equalsIgnoreCase("com.terwaamo.futis.kups.AIKALEIMA")))
        if((action != null) && (action.equalsIgnoreCase(context.getPackageName() + ".AIKALEIMA")))
    	{    		    		                	    	    	   
    		updateWidgetWithLayout(context);
    	}    	
    	
    	super.onReceive(context, intent);
    }


    private void setTime(RemoteViews view)
    {      
        Date thisDate = new Date();        
        Format formatter = new SimpleDateFormat("HH:mm");
        String time = formatter.format(thisDate);        
        
        view.setTextViewText(R.id.aikateksti, time);        
    }
    
    private void setDate(RemoteViews view)
    {
    	DateFormat dateFormat = DateFormat.getDateInstance();
        
        Date thisDate = new Date();                
        String date = dateFormat.format(thisDate);
        
        view.setTextViewText(R.id.pvmteksti, date);
        
    }
    
    private void updateWidgetWithLayout(Context context)
    {
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
		ComponentName thisWidget = new ComponentName(context.getApplicationContext(), this.getClass());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		
		
		// lue asetukset		
	    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String clockModePreference = sharedPref.getString("prefClockStyle", "Digitaalikello");
		

        
		int layoutId;
		int clickLayoutId;
		boolean isDigital = false;			
				
		if(clockModePreference.equals("Digitaalikello"))
		{
			layoutId = R.layout.kello_digital;
			clickLayoutId = R.id.digitausta;
			isDigital = true;
		}
		else
		{
			layoutId = R.layout.kello_analog;
			clickLayoutId = R.id.kello_analog;
		}
		
		for (int i=0; i<appWidgetIds.length; i++) 
		{
			int appWidgetId = appWidgetIds[i];
			    				    							
			RemoteViews myView = new RemoteViews(context.getPackageName(), layoutId);
			
			if(isDigital)
			{
				setTime(myView);
	        	setDate(myView);
			}
        	
        	Intent configIntent = new Intent(context, AppWidgetConfigure.class);
        	configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        	
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            myView.setOnClickPendingIntent(clickLayoutId, pendingIntent);            
            appWidgetManager.updateAppWidget(appWidgetId, myView);
        }
    	
    }          
}