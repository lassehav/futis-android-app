package com.terwaamo.futis;


import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.flurry.android.FlurryAgent;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

class ScheduledGame
{
	String startTime;
	String date;
	int homeTeamId;
	String homeTeamName;
	String homeTeamShortName;
	int awayTeamId;
	String awayTeamName;
	String awayTeamShortName;
	int gameId;
	int homeGoals;
	int awayGoals;
	
	public String toString()
	{
		return homeTeamName + " - " + awayTeamName;
		
	}
	
	public ScheduledGame(){
        super();
    }
}


public class GameCalendar extends ActionBarActivity  {
	ScheduledGame[] games;
	ProgressDialog progress;
	private class StableArrayAdapter extends ArrayAdapter<ScheduledGame> {

		private final Context context;
		private ScheduledGame[] calendar;

	    public StableArrayAdapter(Context context, int textViewResourceId, ScheduledGame[] games) {
	      super(context, textViewResourceId, games);
	      this.context = context;	      	  
	      this.calendar = games;
	    }
	    	    	  

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	View v = convertView;
	
	        if (v == null) {
	
	            LayoutInflater vi;
	            vi = LayoutInflater.from(getContext());
	            v = vi.inflate(R.layout.game_calendar_list_item, parent, false);
	
	        }
	        
	        TeamData td = new TeamData();
	        String homeFileName = td.getTeamLogoFileName(calendar[position].homeTeamId);
	        String awayFileName = td.getTeamLogoFileName(calendar[position].awayTeamId);
	        
	        if(homeFileName != null)
	        {
	        	ImageView homeImage = (ImageView) v.findViewById(R.id.gameCalendarHomeIcon);	        
	        	int resId = getResources().getIdentifier(homeFileName, "drawable", getPackageName());
	        	homeImage.setImageResource(resId);
	        }
	        
	        if(awayFileName != null)
	        {
	        	ImageView awayImage = (ImageView) v.findViewById(R.id.gameCalendarAwayIcon);
	        	int resId2 = getResources().getIdentifier(awayFileName, "drawable", getPackageName());
	        	awayImage.setImageResource(resId2);
	        }
		    
		    TextView textView = (TextView) v.findViewById(R.id.gameCalendarMatchOpponents);
		    textView.setText(calendar[position].homeTeamShortName + " - " + calendar[position].awayTeamShortName);
		    textView.setTextColor(Color.parseColor("#FFFFFF"));
		    
		    TextView textViewLower = (TextView) v.findViewById(R.id.gameCalendarMatchDateTime);
		    textViewLower.setText(calendar[position].date + ", " + calendar[position].startTime);
		    textViewLower.setTextColor(Color.parseColor("#BBBBFF"));
		    

		    return v;
	    }

	  }
	
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if((progress != null) && (progress.isShowing()))
		{
			progress.dismiss();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_calendar);
		
		// enable back button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		initCalendar();		
	}

	public void initCalendar()
	{
		progress = new ProgressDialog(this);
		progress.setIndeterminate(true);		
		progress.setMessage("Lataa ottelutietoja...");
		progress.show();
		
		NetworkResultHandler dataHandler = new NetworkResultHandler(){
			@Override
			public void processStringResult(String result) {
				boolean error = false;
								
				if(result == null)
				{
					//Log.d("GameCalendar", "result on null");
					error = true;
				}
									
				if((error == false) && (result != null))
				{										
					processCalendarData(result);
					progress.dismiss();	
				}
				else
				{
					progress.dismiss();					
					networkError();
				}
				
			}
        };
		
        HttpWorker httpWorker = new HttpWorker();
        httpWorker.setResultHandler(dataHandler);
        httpWorker.execute("http://www.veikkausliiga.com/VeikkausliigaWS/otteluohjelma.asmx/Get?");
        
	}
	
	public void networkError()
	{
		new AlertDialog.Builder(this)
	    .setTitle("Virhe")
	    .setMessage("Verkkovirhe. Yrit� my�hemmin uudestaan.")				    
	    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            finish();
	        }
	     })				    
	     .show();
	}
	
	public void processCalendarData(String data)
	{
		//Log.i("GameCalendar", data);
		ArrayList gameList = new ArrayList<ScheduledGame>();
		
		InputSource inputSrc = new InputSource(new StringReader( data ));
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//game_schedule/game";
		try {
			NodeList nodes = (NodeList)xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);
			TeamData td = new TeamData();
			
			if(nodes != null && nodes.getLength() > 0) {
	            
	            int len = nodes.getLength();
	            for(int i = 0; i < len; ++i) {
	            	ScheduledGame game = new ScheduledGame();
	                // query value
	                Node node = nodes.item(i);
	                
	                Element e = (Element)node;
	                game.gameId = Integer.parseInt(e.getAttribute("id"));
	                
	                
	                NodeList childNodes = node.getChildNodes();
	                int childLen= childNodes.getLength();	                
		            for(int c = 0; c < childLen; c++) {
		            	Node childNode = childNodes.item(c);		            	
		            	String text = childNode.getTextContent();		            	
		            	String name = childNode.getNodeName();
		            	NamedNodeMap attr = childNode.getAttributes();
		            	
		            	
		            	if(name.equals("date_of_game"))
				    	{
				    		game.date = text;
				    	}		            	
				    	else if(name.equals("start_time"))
				    	{
				    		game.startTime = text;
				    	}
				    	else if(name.equals("home_team"))
				    	{				    	
				    		game.homeTeamName = text;
				    		game.homeTeamId = Integer.parseInt(attr.getNamedItem("id").getTextContent());
				    		game.homeTeamShortName = td.getTeamShortDesc(game.homeTeamId);
				    		
				    		game.homeGoals = Integer.parseInt(attr.getNamedItem("goals").getTextContent());
				    	}
				    	else if(name.equals("away_team"))
				    	{	
				    		game.awayTeamName = text;
				    		game.awayTeamId = Integer.parseInt(attr.getNamedItem("id").getTextContent());
				    		game.awayTeamShortName = td.getTeamShortDesc(game.awayTeamId);
				    		
				    		game.awayGoals = Integer.parseInt(attr.getNamedItem("goals").getTextContent());
				    	}		            	
		            }		            
		            gameList.add(game);	                	               	               
	            }
	        }
			
		} catch (XPathExpressionException e) {
			networkError();
			e.printStackTrace();
			return;
		}
		
		// ------ TEST
		/*
		gameList.clear();
		ScheduledGame game1 = new ScheduledGame();
		game1.awayTeamShortName = "Test2";
		game1.homeTeamShortName = "Test1";
		game1.date = "23.3.2014";
		game1.startTime = "15:00";
		gameList.add(game1);
		
		ScheduledGame game2 = new ScheduledGame();
		game2.awayTeamShortName = "Test4";
		game2.homeTeamShortName = "Test3";
		game2.date = "25.3.2014";
		game2.startTime = "18:00";
		gameList.add(game2);
		
		ScheduledGame game3 = new ScheduledGame();
		game3.homeTeamShortName = "Test5";
		game3.awayTeamShortName = "Test6";		
		game3.date = "26.3.2014";
		game3.startTime = "18:00";
		gameList.add(game3);
		
		ScheduledGame game4 = new ScheduledGame();
		game4.homeTeamShortName = "Test7";
		game4.awayTeamShortName = "Test8";		
		game4.date = "27.3.2014";
		game4.startTime = "18:00";
		gameList.add(game4);
		
		ScheduledGame game5 = new ScheduledGame();
		game5.homeTeamShortName = "Test9";
		game5.awayTeamShortName = "Test10";		
		game5.date = "1.4.2014";
		game5.startTime = "15:00";
		gameList.add(game5);
		
		ScheduledGame game6 = new ScheduledGame();
		game6.homeTeamShortName = "Test11";
		game6.awayTeamShortName = "Test12";		
		game6.date = "1.4.2014";
		game6.startTime = "16:00";
		gameList.add(game6);
		
		ScheduledGame game7 = new ScheduledGame();
		game7.homeTeamShortName = "Test13";
		game7.awayTeamShortName = "Test14";		
		game7.date = "1.4.2014";
		game7.startTime = "17:00";
		gameList.add(game7);
		
		ScheduledGame game8 = new ScheduledGame();
		game8.homeTeamShortName = "Test15";
		game8.awayTeamShortName = "Test16";		
		game8.date = "1.4.2014";
		game8.startTime = "18:00";
		gameList.add(game8);
		
		ScheduledGame game9 = new ScheduledGame();
		game9.homeTeamShortName = "Test17";
		game9.awayTeamShortName = "Test18";		
		game9.date = "1.4.2014";
		game9.startTime = "18:00";
		gameList.add(game9);
		
		ScheduledGame game10 = new ScheduledGame();
		game10.homeTeamShortName = "Test19";
		game10.awayTeamShortName = "Test20";		
		game10.date = "1.4.2014";
		game10.startTime = "18:00";
		gameList.add(game10);
		
		ScheduledGame game11 = new ScheduledGame();
		game11.homeTeamShortName = "Test21";
		game11.awayTeamShortName = "Test22";		
		game11.date = "1.4.2014";
		game11.startTime = "18:00";
		gameList.add(game11);
		
		ScheduledGame game12 = new ScheduledGame();
		game12.homeTeamShortName = "Test21";
		game12.awayTeamShortName = "Test22";		
		game12.date = "2.4.2014";
		game12.startTime = "18:00";
		gameList.add(game12);
		
		ScheduledGame game13 = new ScheduledGame();
		game13.homeTeamShortName = "Test21";
		game13.awayTeamShortName = "Test22";		
		game13.date = "3.4.2014";
		game13.startTime = "18:00";
		gameList.add(game13);
		
		ScheduledGame game14 = new ScheduledGame();
		game14.homeTeamShortName = "Test21";
		game14.awayTeamShortName = "Test22";		
		game14.date = "4.4.2014";
		game14.startTime = "18:00";
		gameList.add(game14);
		
		ScheduledGame game15 = new ScheduledGame();
		game15.homeTeamShortName = "Test21";
		game15.awayTeamShortName = "Test22";		
		game15.date = "4.4.2014";
		game15.startTime = "18:00";
		gameList.add(game15);
		
		ScheduledGame game16 = new ScheduledGame();
		game16.homeTeamShortName = "Test21";
		game16.awayTeamShortName = "Test22";		
		game16.date = "5.4.2014";
		game16.startTime = "18:00";
		gameList.add(game16);
		
		ScheduledGame game17 = new ScheduledGame();
		game17.homeTeamShortName = "Test21";
		game17.awayTeamShortName = "Test22";		
		game17.date = "6.4.2014";
		game17.startTime = "18:00";
		gameList.add(game17);
		
		ScheduledGame game18 = new ScheduledGame();
		game17.homeTeamShortName = "Test21";
		game17.awayTeamShortName = "Test22";		
		game17.date = "7.4.2014";
		game17.startTime = "18:00";
		gameList.add(game17);
		
		ScheduledGame game19 = new ScheduledGame();
		game19.homeTeamShortName = "Test21";
		game19.awayTeamShortName = "Test22";		
		game19.date = "8.4.2014";
		game19.startTime = "18:00";
		gameList.add(game19);
		
		ScheduledGame game20 = new ScheduledGame();
		game20.homeTeamShortName = "Test21";
		game20.awayTeamShortName = "Test22";		
		game20.date = "9.4.2014";
		game20.startTime = "18:00";
		gameList.add(game20);
		*/
		// ------
		
		
		
		//Time today = new Time(Time.getCurrentTimezone());
		//today.setToNow();
					
		
		ListView listview = (ListView) findViewById(R.id.GameCalendarListView);
		games = new ScheduledGame[gameList.size()];
		for(int i = 0; i < gameList.size(); i++)
		{
			games[i] = (ScheduledGame)gameList.get(i);							
		}
		

		StableArrayAdapter adapter = new StableArrayAdapter(this, R.layout.game_calendar_list_item, games);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(mMessageClickedHandler); 
		
		
		// Hoidellaan lista oikeaan kohti vuodessa
		int lastPassedGameIndex = -1;
		int todayGameIndex = -1;
		SimpleDateFormat sdfa = new SimpleDateFormat("yyyy-MM-dd");      
	    Date today = null;
		try {
			today = sdfa.parse(sdfa.format(new Date()));
		} catch (java.text.ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		for(int i = 0; i < gameList.size(); i++)
		{		
			
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("dd.M.yyyy");
	        	Date gameDate = sdf.parse(games[i].date);
	        	
	        	if(today.compareTo(gameDate)>0)
	        	{
	        		//System.out.println("Today is after gamedate");
	        		lastPassedGameIndex = i + 1;
	        	}
	        	else if(today.compareTo(gameDate)<0)
	        	{
	        		//System.out.println("Date1 is before Date2");
	        	}
	        	else if(today.compareTo(gameDate)==0)
	        	{
	        		//System.out.println("Date1 is equal to Date2");
	        		todayGameIndex = i;
					break;
	        	}	        										
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block				
				e.printStackTrace();
			}
			
		}
		
		if(todayGameIndex != -1)
		{
			listview.setSelection(todayGameIndex);
			//Log.d("GameCalendar", "todayGameIndex = " + todayGameIndex);
		}
		else if(lastPassedGameIndex != -1)
		{		
			listview.setSelection(lastPassedGameIndex);
			//Log.d("GameCalendar", "lastPassedGameIndex = " + lastPassedGameIndex);
			
		}
									
		 
	}
	
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView parent, View v, int position, long id) {
	    	Intent intent = new Intent(GameCalendar.this, BestPlayerVote.class);
	    	
	    	intent.putExtra("gameId", games[position].gameId);
	    	intent.putExtra("homeId", games[position].homeTeamId);
	    	intent.putExtra("homeTeamShortName", games[position].homeTeamShortName);
	    	intent.putExtra("awayId", games[position].awayTeamId);
	    	intent.putExtra("awayTeamShortName", games[position].awayTeamShortName);
	    	intent.putExtra("gameDate", games[position].date);
	    	intent.putExtra("gameTime", games[position].startTime);
	    	intent.putExtra("homeGoals", games[position].homeGoals);
	    	intent.putExtra("awayGoals", games[position].awayGoals);
	    		    		    
		    startActivity(intent);
	    }
	};


}

