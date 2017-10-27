package com.terwaamo.futis;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
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
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;

class TeamStat
{
	String name;
	
	int games;
	int wins;
	int ties;
	int losses;
	String goalDifference;
	int points;	
	
	public TeamStat(){
        super();
    }
}

public class LeagueResults extends ActionBarActivity {

	private ProgressDialog mProgress;
	private Context mContext;
	ArrayList mStatList;
	
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_league_results);
		
		mContext = this;
				
		
		// enable back button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mStatList = new ArrayList<TeamStat>();
		
		mProgress = new ProgressDialog(this);
		mProgress.setIndeterminate(true);		
		mProgress.setMessage("Lataa tilastoja...");
		mProgress.show();
		
		// Create URL string
        String URL = "http://www.veikkausliiga.com/veikkausliigaWS/sarjataulukko.asmx/GetCurrent";
        try
        {
        	
        	NetworkResultHandler leagueStatsHandler = new NetworkResultHandler(){
    			@Override
    			public void processStringResult(String result) 
    			{
    				//Log.d("BestPlayerVote", "Server response " + result);      
    				
    				if(result == null)
    				{
    					networkError();    					
    					return;
    				}
    				
    				InputSource inputSrc = new InputSource(new StringReader( result ));
    				XPath xpath = XPathFactory.newInstance().newXPath();
    				String expression = "//sarjataulukko/joukkue";
    				try {
    					NodeList nodes = (NodeList)xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);    					
    					
    					if(nodes != null && nodes.getLength() > 0) {
    			            
    			            int len = nodes.getLength();
    			            for(int i = 0; i < len; ++i) {
    			            	TeamStat stat = new TeamStat();
    			                // query value
    			                Node node = nodes.item(i);
    			                NamedNodeMap attr = node.getAttributes();
    			                
    			                stat.name = attr.getNamedItem("Lyhenne").getTextContent();
    			                stat.games = Integer.parseInt(attr.getNamedItem("ottelut").getTextContent());
    			                stat.wins = Integer.parseInt(attr.getNamedItem("voitot").getTextContent());
    			                stat.ties = Integer.parseInt(attr.getNamedItem("tasapelit").getTextContent());
    			                stat.losses = Integer.parseInt(attr.getNamedItem("tappiot").getTextContent());
    			                stat.goalDifference = attr.getNamedItem("maaliero").getTextContent();
    			                stat.points = Integer.parseInt(attr.getNamedItem("pisteet").getTextContent());
    			                    			                    			                		            	    				            		         
    			                mStatList.add(stat);	                	               	               
    			            }
    			            initTable();
    			            mProgress.dismiss();
    			                			          
    			        }
    					
    				} catch (XPathExpressionException e) {
    					
    					networkError();
    					e.printStackTrace();
    					return;
    				}
    				
    							   		
    			}
            };
    		
            HttpWorker httpWorker = new HttpWorker();
            httpWorker.setResultHandler(leagueStatsHandler);
            httpWorker.execute(URL);
        }
        catch(Exception ex)
        {
            // Fail
        	networkError();
        	ex.printStackTrace();
        	
        }
				
	}
	
	private void initTable()
	{
		TableLayout tl = (TableLayout) findViewById(R.id.statResultLayout);
		
		for(int i = 0; i < mStatList.size(); i++)
		{
			TeamStat ts = (TeamStat)mStatList.get(i);

			LayoutInflater vi;
            vi = LayoutInflater.from(this);
            View v = vi.inflate(R.layout.league_results_row, tl, false);
            
            TextView vclub = (TextView) v.findViewById(R.id.leagueResultsRowClub);
            vclub.setText(ts.name);   
            
            TextView vo = (TextView) v.findViewById(R.id.leagueResultsRowGames);
            vo.setText(new Integer(ts.games).toString());
            
            TextView vv = (TextView) v.findViewById(R.id.leagueResultsRowWins);
            vv.setText(new Integer(ts.wins).toString());
            vv.setTextColor(Color.parseColor("#3CE01F"));
            
            TextView vt = (TextView) v.findViewById(R.id.leagueResultsRowTies);
            vt.setText(new Integer(ts.ties).toString());
            vt.setTextColor(Color.parseColor("#E3E30E"));
            
            TextView vh = (TextView) v.findViewById(R.id.leagueResultsRowLosses);
            vh.setText(new Integer(ts.losses).toString());
            vh.setTextColor(Color.parseColor("#F24633"));
            
            TextView vmero = (TextView) v.findViewById(R.id.leagueResultsRowGoalDiff);            
            vmero.setText(ts.goalDifference);
            vmero.setTextColor(Color.parseColor("#33F2EF"));
            
            
            TextView vpist = (TextView) v.findViewById(R.id.leagueResultsRowPoints);            
            vpist.setText(new Integer(ts.points).toString());  
            vpist.setTextColor(Color.parseColor("#3CE01F"));
			
			tl.addView(v);
		}
		
	}
	
	public void networkError()
	{		
		if((mProgress != null) && (mProgress.isShowing()))
		{
			mProgress.dismiss();
		}
		new AlertDialog.Builder(this)
	    .setTitle("Virhe")
	    .setMessage("Verkkovirhe. Yritä myöhemmin uudestaan.")
	    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            finish();
	        }
	     })				    
	     .show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if((mProgress != null) && (mProgress.isShowing()))
		{
			mProgress.dismiss();
		}
	}
	

}
