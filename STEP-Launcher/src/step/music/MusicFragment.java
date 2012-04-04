package step.music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.step.launcher.R;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import step.music.MusicAsyncTask;

public class MusicFragment extends Fragment implements MusicAsyncTaskCallback, View.OnClickListener {
	
	private final int baseStationPort = 13330;
	
	private TextView title;
	
	private MusicAsyncTask asyncTask;
	private MusicAdapter adapter;
	private Map<String, Genre> stations;
	private ArrayList<Button> genre_buttons;
	private LinearLayout scrollLayout;
	private GridView musicGridView;
	private Genre selected_genre;
	private View v;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        v = inflater.inflate(R.layout.music_fragment, container, false);
        title = (TextView)v.findViewById(R.id.music_title);
        
        scrollLayout = (LinearLayout)v.findViewById(R.id.musicScrollLayout);
        musicGridView = (GridView)v.findViewById(R.id.musicGridView);
        adapter = new MusicAdapter(this);
        musicGridView.setAdapter(adapter);
        
        if (genre_buttons == null)
        {
	        asyncTask = new MusicAsyncTask();
	        asyncTask.setCallback(this);
	        asyncTask.execute(baseStationPort);
        }
        else
        {
        	for (Button b : genre_buttons)
        		scrollLayout.addView(b);
        }
        
        return v;
	}

	//@Override
	public void taskGotStations(Map<String, Genre> stations)
	{
		this.stations = stations;
		LinearLayout layout = (LinearLayout)scrollLayout;
		layout.removeAllViews();
		Resources r = getResources();
		genre_buttons = new ArrayList<Button>(stations.size());
		
		for (String genre : stations.keySet())
		{
			Button b = new Button(this.getActivity());
			b.setText(genre);
			b.setBackgroundDrawable(r.getDrawable(R.drawable.generic_button));
			b.setTextSize(24);
			b.setOnClickListener(this);
			genre_buttons.add(b);
			layout.addView(b);
		}
	}
	
	public void onClick(View v)
	{
		if (genre_buttons.contains(v))
		{
			String title = ((Button)v).getText().toString();
			selected_genre = stations.get(title);
			String[] titles = new String[selected_genre.stations.length];
			int index = 0;
			
			for (Station s : selected_genre.stations)
			{
				titles[index] = s.name;
				index += 1;
			}
			
			adapter.setTitles(titles);
		}
	}
	
	public void onStationClick(View v)
	{
		String title = ((Button)v).getText().toString();
		Station selected_station = null;
		
		for (Station s : selected_genre.stations)
		{
			if (s.name.compareTo(title) == 0)
				selected_station = s;
		}
		
		if (selected_station != null)
		{
			asyncTask.sendMessage("PLAY " + selected_genre.id + " " + selected_station.id);
		}
	}
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		scrollLayout.removeAllViews();
	}
}
