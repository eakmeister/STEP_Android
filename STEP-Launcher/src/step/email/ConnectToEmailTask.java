package step.email;

import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.view.*;

public class ConnectToEmailTask extends AsyncTask<String, Void, String> {
	Mail m;
	View v;
	public ConnectToEmailTask(Mail m1){
		m = m1;
	}
	@Override
	protected String doInBackground(String...strings)
	{
		String Header = "";
        try
        {
			m.connect();
            //int totalMessages = gmail.getMessageCount();
            //int newMessages = gmail.getNewMessageCount();
            
            //Header = "Total Messages = " + totalMessages + "\nNew Messages = " + newMessages + "\n";
            //textView.setText(Header);
            //gmail.printAllMessageEnvelopes();
            //gmail.printAllMessages();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Connection Failed";
		}
        return "Connected";
	}

	@Override
	protected void onPostExecute(String result)
	{
		Log.d("EMAIL APP", result);
		if(result.contentEquals("Connection Failed")){
			Toast.makeText(this.m.getActivity(), "Email Connection Failed. Retrying", Toast.LENGTH_SHORT).show();
	        ConnectToEmailTask task = new ConnectToEmailTask(this.m);
	    	task.execute();
		}
		this.m.setIsConnected(true);
		Toast.makeText(this.m.getActivity(), "Email Connected", Toast.LENGTH_SHORT).show();
		UpdateInboxTask task = new UpdateInboxTask(this.m);
		task.execute();
		//textView.setText(result);
	}
}

