package step.music;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.os.AsyncTask;
import android.util.Log;

public class MusicAsyncTask extends AsyncTask<Integer, ArrayList<ArrayList<String>>, Object>
{
	private Socket sock;
	private DataOutputStream sendStream;
	private DataInputStream recvStream;
	private MusicAsyncTaskCallback callback;
	private MusicContentHandler handler;

	public void setCallback(MusicAsyncTaskCallback callback)
	{
		this.callback = callback;
	}
	
	@Override
	protected Object doInBackground(Integer... arg0)
	{
		int port = arg0[0];
		handler = new MusicContentHandler();
		
		while (true)
		{
			try {
				sock = new Socket("192.168.1.4",  port);
				sendStream = new DataOutputStream(sock.getOutputStream());
				recvStream = new DataInputStream(sock.getInputStream());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConnectException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				continue;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		}
		
		try {
			sendStream.writeBytes("GENRES");
			SAXParserFactory factory = SAXParserFactory.newInstance();
			
			StringBuilder sb = new StringBuilder();
			while (true)
			{
				String data = recvStream.readLine().trim();
				sb.append(data);
				
				if (data.compareTo("</categories>") == 0)
					break;
			}
			
			try {
				SAXParser parser = factory.newSAXParser();
				XMLReader reader = parser.getXMLReader();
				reader.setContentHandler(handler);
				InputSource is = new InputSource();
				is.setByteStream(new ByteArrayInputStream(sb.toString().getBytes()));
				reader.parse(is);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			publishProgress(handler.getCategories());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	protected void onProgressUpdate(ArrayList<ArrayList<String>>... data)
	{
		if (data.length > 0)
			callback.taskGotGenres(data[0]);
    }

}
