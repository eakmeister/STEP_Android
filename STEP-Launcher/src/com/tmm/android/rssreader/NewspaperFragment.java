/**
 * 
 */
package com.tmm.android.rssreader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import step.music.MusicAsyncTask;

import android.util.Log;



/**
 * @author rob
 *
 */

import com.step.launcher.R;
import com.tmm.android.rssreader.RssActivity;
import com.tmm.android.rssreader.RssListAdapter;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class NewspaperFragment extends Fragment {
	RssActivity rssActivity;
	private RssListAdapter adapter;
	RssReader rssReader;
	List<JSONObject> rssreader = new ArrayList<JSONObject>();

	
	private OnItemClickListener listItemSelectListener = new OnItemClickListener() {
    	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    	{
    		getActivity().findViewById(R.id.newsFrag_listview).setVisibility(View.VISIBLE);
    		
    		try
        	{
        		RssReader.getLatestRssFeed();
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        	}
        		
    	}
    };
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			final Bundle savedInstanceState) {

				
		final View V = inflater.inflate(R.layout.newspaper_fragment, container, false);
		
		final ListView list = (ListView) V.findViewById(R.id.newsFrag_listview);
		list.setTextFilterEnabled(true);
		list.setOnItemClickListener(listItemSelectListener);
		
		
		


				  
					try {
						Log.e("test 1","we are testing");
						List<JSONObject> jobs = new ArrayList<JSONObject>();
						rssReader = new RssReader();
				        
				        rssReader.execute(jobs);

						
						
						//rssReader.doInBackground(jobs);

						//RssReader.getLatestRssFeed();
						Log.e("test 1","we are testing2");
						
						 adapter = new RssListAdapter(getActivity(), 0,(ArrayList<JSONObject>) jobs);
						 Log.e("test 1","we are testing3");
						 adapter.getView(0, V, list);
						 Log.e("test 1","we are testing4");
						
						 

					} catch (Exception e) {
						Log.e("RSS ERROR", "Error loading RSS Feed Stream >> " + e.getMessage() + " //" + e.toString());
					}
				  
					
					
					


		
		//setup the data source
		//rssActivity = new RssActivity();
		
        //mail.setUserPass("capstone.group6.2012", "capstone2012");
        //ConnectToEmailTask task = new ConnectToEmailTask(this.mail, V.findViewById(R.id.txtConn));
    	//task.execute();
		
		

		
		//adapter = new RssListAdapter(this,jobs);
		//adapter = new RssListAdapter(this,jobs);
		
		//setListAdapter(adapter);
		
		
		return V;
		
		
		
		/*V.findViewById(R.id.job_text);

		
		Log.v("somethign worked","finally");

		
		Runnable r1= new Runnable() {
			  public void run() {
				  
				  rssreader = RssReader.getLatestRssFeed();
				  
					
					
					

			  }
			};
			Thread thr1 = new Thread(r1);
			thr1.start();
			
			*/

		
	}
	
}