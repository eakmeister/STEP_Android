package step.music;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.nio.ByteOrder;

import com.step.launcher.R;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.GridLayoutAnimationController;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import step.ButtonScrollView;
import step.music.MusicAsyncTask;

public class MusicFragment extends Fragment implements MusicAsyncTaskCallback, View.OnClickListener {
	
	private final int baseStationPort = 13330;
	
	private TextView title;
	
	private MusicAsyncTask asyncTask;
	private MusicAdapter adapter;
	private Map<String, Genre> stations;
	private ArrayList<Button> genre_buttons;
	private LinearLayout scrollLayout;
	private RadioGroup musicScrollGroup;
	private ButtonScrollView musicStationsView;
	private LinearLayout musicStationsLayout;
	private Genre selected_genre;
	private View v;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        v = inflater.inflate(R.layout.music_fragment, container, false);
        title = (TextView)v.findViewById(R.id.music_title);
        
        musicScrollGroup = (RadioGroup)v.findViewById(R.id.musicScrollGroup);
        musicStationsView = (ButtonScrollView)v.findViewById(R.id.musicGridView);
        title = (TextView)v.findViewById(R.id.music_title);
        musicStationsLayout = (LinearLayout)v.findViewById(R.id.musicStationLayout);
        adapter = new MusicAdapter(this);
        
        if (genre_buttons == null)
        {
        	// find base station ip
        	WifiManager manager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        	int addr = manager.getDhcpInfo().gateway;
        	byte[] bytes = new byte[4];
        	
        	// Need to flip the byte order, since java is big-endian and the
        	// tablet is little-endian. Java is so fucking useless that it
        	// doesn't handle this shit for you.
        	if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        	{
	        	bytes[0] = (byte)addr;
	        	bytes[1] = (byte)(addr >> 8);
	        	bytes[2] = (byte)(addr >> 16);
	        	bytes[3] = (byte)(addr >> 24);
        	}
        	else
        		bytes = BigInteger.valueOf(addr).toByteArray();
        	
        	InetAddress baseStationAddr = null;
        	
			try {
				baseStationAddr = InetAddress.getByAddress(bytes);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
	        asyncTask = new MusicAsyncTask(baseStationAddr);
	        asyncTask.setCallback(this);
	        asyncTask.execute(baseStationPort);
        }
        else
        {
        	for (Button b : genre_buttons)
        		musicScrollGroup.addView(b);
        }
        
        return v;
	}

	//@Override
	public void taskGotStations(Map<String, Genre> stations)
	{
		this.stations = stations;
		//LinearLayout layout = (LinearLayout)scrollLayout;
		//layout.removeAllViews();
		musicScrollGroup.removeAllViews();
		
		Resources r = getResources();
		genre_buttons = new ArrayList<Button>(stations.size());
		
		for (String genre : stations.keySet())
		{
			RadioButton b = new RadioButton(this.getActivity());
			b.setText(genre);
			b.setButtonDrawable(r.getDrawable(R.drawable.null_drawable));
			b.setBackgroundDrawable(r.getDrawable(R.drawable.generic_radio_button));
			b.setTextSize(24);
			b.setOnClickListener(this);
			genre_buttons.add(b);
			musicScrollGroup.addView(b);
		}
	}
	
	public void onClick(View v)
	{
		if (genre_buttons.contains(v))
		{
			String title = ((Button)v).getText().toString();
			selected_genre = stations.get(title);
			musicStationsLayout.removeAllViews();
			
			for (Station s : selected_genre.stations)
			{
				Button button = new Button(getActivity());
				button.setHeight(100);
				button.setBackgroundDrawable(getResources().getDrawable(R.drawable.generic_button));
				button.setTextSize(24);
				button.setLayoutParams(new AbsListView.LayoutParams(200, LayoutParams.WRAP_CONTENT));
				button.setText(s.name);
				button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onStationClick(v);
					}
				});
				
				musicStationsLayout.addView(button);
			}
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
			this.title.setText("Now Playing: " + selected_station.name);
			asyncTask.sendMessage("PLAY " + selected_genre.id + " " + selected_station.id);
		}
	}
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		musicScrollGroup.removeAllViews();
	}
}
