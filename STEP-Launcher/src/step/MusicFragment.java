package step;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.step.launcher.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MusicFragment extends Fragment {
	
	private final String baseStationAddr = "10.0.50.1";
	private final int baseStationRecvPort = 13337;
	private final int baseStationSendPort = 13338;
	
	private TextView title;
	private Socket sendSocket;
	private Socket recvSocket;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View v = inflater.inflate(R.layout.music_fragment, container, false);
        title = (TextView)v.findViewById(R.id.music_title);
        title.setText("HELLOOOO");
        
        return v;
	}
}
