package com.terwaamo.futis;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.flurry.android.FlurryAgent;
import com.terwaamo.futis.R;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;



public class Alarm extends Activity {

	//private Handler mHandler;
	private MediaPlayer mp = null;
	
	@Override
	protected void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, "5PNF6S6J3D43KSBWCCYP");
	}
	 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);				
        
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        this.getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON | LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		setContentView(R.layout.activity_alarm);
	
		Calendar c = Calendar.getInstance(); 
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
	
		TextView currentTimeLabel = (TextView)findViewById(R.id.currentTimeLabel);
		currentTimeLabel.setText((String.format("%02d", hour) + ":" + String.format("%02d", min)));
		
		SharedPreferences settings = getSharedPreferences("AlarmSettings", 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean("AlarmSet", false);
	    editor.commit();
	    
	    
	    setVolumeControlStream(AudioManager.STREAM_MUSIC);
	   
	    
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		
		mp.stop();
		mp.reset();
		mp.release();
        mp = null;
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	protected void onResume()
	{
		playAlarmMusic();
		super.onResume();
	}

	
	public void closeAlarmClick(View view)
	{
		FlurryAgent.logEvent("AlarmSound Close Click");
		finish();
	}
	
	public void snoozeClick(View view)
	{
		final Context context = Alarm.this;
		FlurryAgent.logEvent("AlarmSound Snooze Click");
		Intent intent = new Intent(context, Alarm.class);	    
	    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		//am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000 * 60, pendingIntent);
		int snoozeTimeMilliSeconds = 300000;
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + snoozeTimeMilliSeconds, pendingIntent);
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.MILLISECOND, snoozeTimeMilliSeconds);
		
		SharedPreferences settings = getSharedPreferences("AlarmSettings", 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean("AlarmSet", true);
	    editor.putInt("AlarmHour", cal.get(Calendar.HOUR_OF_DAY));
	    editor.putInt("AlarmMin",  cal.get(Calendar.MINUTE));
	    editor.commit();
	    
		
		finish();
	}
	
	public void playAlarmMusic()
	{
		try
		{
			mp = MediaPlayer.create(this, R.raw.veikkausliiga);
			
	        mp.start();
		}
		catch(Exception e)
		{
			Log.e("Alarm", "exception", e);
		}
	}
	
}
