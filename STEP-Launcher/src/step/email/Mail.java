package step.email;

import step.address.ContactInfo;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.step.launcher.R;
import com.sun.mail.pop3.POP3SSLStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;



public class Mail{
	
	
	private Session session = null;
    private Store store = null;
    private String username, password;
    private Folder inbox;
    private Activity activity;
    private boolean isConnected;
    private boolean haveMsgs;
    //private TableModel tm;
    public Message[] msgs;
    private EmailList emailList;
    private ListView email_listView;
    private ArrayList<ContactInfo> cli_w_email;
    private CurrentMessage mCurMsg;
    boolean pop3 = false;
    
    public Mail(Activity a){
    	this.activity = a;
    	this.isConnected = false;
    	this.haveMsgs = false;
    	this.emailList = new EmailList();
    	this.cli_w_email = new ArrayList<ContactInfo>();
    	mCurMsg = new CurrentMessage();
    	getContactsWithEmail();
    };
    
    public void setHaveMsgs(boolean tf){
    	this.haveMsgs = tf;
    }
    
    public boolean haveMsgs()
    {
    	return this.haveMsgs;
    }
    
    public boolean getIsConnected(){
    	return this.isConnected;
    }
    
    public void setIsConnected(boolean conn){
    	this.isConnected = conn;
    }
    
    public void setListView(ListView lv){
    	this.email_listView = lv;
    }
    
    public EmailList getEmailList(){
    	return this.emailList;
    }
    
    public CurrentMessage getCurMsg(){
    	return mCurMsg;
    }
    
    public Activity getActivity(){
    	return this.activity;
    }
    
    public ArrayList<ContactInfo> get_cli_w_email(){
    	return this.cli_w_email;
    }
    
    public void getContactsWithEmail(){
    	this.cli_w_email.clear();
    	ContentResolver cr = this.activity.getContentResolver();
    	String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
    	Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, sortOrder);
        if (cur.getCount() > 0) {
		    while (cur.moveToNext()) {
		        String contactId = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
		        String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		        Cursor emails = cr.query(Email.CONTENT_URI, null, Email.CONTACT_ID + " = " + contactId, null, null);
		        if(emails.moveToNext()) {
		            String email = emails.getString(emails.getColumnIndex(Email.DATA));
		            if(email!=null && !email.contentEquals("")){
			            ContactInfo ci = new ContactInfo();
			            ci.setName(name);
			            ci.setEmail(email);
			            this.cli_w_email.add(ci);
		            }
		        }
		    }
		}
    }
	
    public void setUserPass(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public void connect() throws Exception{
    	if(this.pop3){
    		connect_pop();
    	}
    	else
    	{
    		connect_imap();
    	}
    }
    public void connect_imap() throws Exception{
    	
    	Properties imapProps = new Properties();
    	imapProps.setProperty("mail.store.protocol", "imaps");
    	this.session = Session.getDefaultInstance(imapProps, null);
		this.store = this.session.getStore("imaps");
		this.store.connect("imap.gmail.com", this.username, this.password);
        //Download message headers from server
        // Open the Folder
        this.inbox = this.store.getDefaultFolder();
        this.inbox = this.inbox.getFolder("INBOX");
        
        if (this.inbox == null) {
            throw new Exception("Invalid folder");
        }
        
        // try to open read/write and if that fails try read-only
        try {
            
            this.inbox.open(Folder.READ_WRITE);
            
        } catch (MessagingException ex) {
            
            this.inbox.open(Folder.READ_ONLY);
            
        }
    	
    }
    public void connect_pop() throws Exception{
    	
    	String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        Properties pop3Props = new Properties();
        pop3Props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
        pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
        pop3Props.setProperty("mail.pop3.port",  "995");
        pop3Props.setProperty("mail.pop3.socketFactory.port", "995");
        
        URLName url = new URLName("pop3", "pop.gmail.com", 995, "",
        		this.username, this.password);
        
        this.session = Session.getInstance(pop3Props, null);
        this.store = new POP3SSLStore(this.session, url);
        this.store.connect();
        
        //Download message headers from server
        // Open the Folder
        this.inbox = this.store.getDefaultFolder();
        this.inbox = this.inbox.getFolder("INBOX");
        
        if (this.inbox == null) {
            throw new Exception("Invalid folder");
        }
        
        // try to open read/write and if that fails try read-only
        try {
            
        	this.inbox.open(Folder.READ_WRITE);
            
        } catch (MessagingException ex) {
            
        	this.inbox.open(Folder.READ_ONLY);
            
        }

        
    }
    
    public boolean getNewMessages() throws Exception{
       
       if(this.msgs.length >= this.inbox.getMessageCount()){
    	   return false;
       }
       
       //this.inbox.getMessages();
       Flags seen = new Flags(Flags.Flag.SEEN);
       FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
       Message newMsgs[] = this.inbox.search(unseenFlagTerm);
    	
       if(newMsgs.length != 0){
    	   setHaveMsgs(false);
    	   ArrayList<Message> msgList = new ArrayList<Message>(Arrays.asList(this.msgs));
    	   for(int i = 0; i < newMsgs.length; i++){
    		   msgList.add(newMsgs[i]);
    	   }
    	   Message full[] = new Message[msgList.size()];
    	   msgList.toArray(full);
    	   this.msgs = full;
    	   this.emailList.clearMessages();
    	   this.emailList.addMessages(this.msgs);
    	   return true;
       }
       return false;
     }
    
    public void getMessages() throws Exception{
       this.emailList.clearMessages();
	   //Get folder's list of messages
	   this.msgs = this.inbox.getMessages();
	   //Retrieve message headers 
	   FetchProfile profile = new FetchProfile();
	   profile.add(FetchProfile.Item.ENVELOPE);
	   this.inbox.fetch(this.msgs, profile);
	   this.emailList.addMessages(this.msgs);
    }
    
    public void displayMessages() throws Exception{
		//setup the data adaptor
		EmailListAdapter adapter = new EmailListAdapter(this.activity, R.layout.message_list_element, this.emailList.getEmailList());
		this.email_listView.setAdapter(adapter);
    }
    
    public void readEmail(int idx) throws Exception{
    	ReadEmailMessageTask task = new ReadEmailMessageTask(this, idx);
    	task.execute();
    }
    
    public void deleteMsg() throws Exception{
    	this.msgs[this.mCurMsg.getMsgNumber()].setFlag(Flags.Flag.DELETED, true);
        this.inbox.close(true);
    	try {
            
        	this.inbox.open(Folder.READ_WRITE);
            
        } catch (MessagingException ex) {
            
        	this.inbox.open(Folder.READ_ONLY);
            
        }
    	getMessages();
    }
    
    public void sendEmail() throws Exception{
    	Properties props = System.getProperties();
    	 
        props.put("mail.smtp.user", this.username);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.debug", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", 
              "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
 
        // Required to avoid security exception.
        MyAuthenticator authentication = 
              new MyAuthenticator(this.username, this.password);
        Session smtpSession = 
              Session.getInstance(props,authentication);
        smtpSession.setDebug(true);
    	
    	
    	MimeMessage message = new MimeMessage(smtpSession);
    	EditText body = (EditText) this.activity.findViewById(R.id.compose_message_content);
    	EditText subj = (EditText) this.activity.findViewById(R.id.compose_subject);
    	TextView to   = (TextView) this.activity.findViewById(R.id.compose_to);
    	
    	MimeMultipart content = new MimeMultipart("alternative");
    	MimeBodyPart text = new MimeBodyPart();
    	text.setText(body.getText().toString());
    	content.addBodyPart(text);
    	message.setContent(content);
    	message.setSubject(subj.getText().toString());
    	message.setFrom(new InternetAddress(this.username));
    	message.addRecipient(RecipientType.TO, new InternetAddress(to.getText().toString()));

    	SendEmailTask task = new SendEmailTask(this.activity, smtpSession.getTransport("smtps"), this.username, this.password, message);
    	task.execute();
    	
    }
    
    private class MyAuthenticator extends javax.mail.Authenticator {
        String User;
        String Password;
        public MyAuthenticator (String user, String password) {
            User = user;
            Password = password;
        }
         
        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new javax.mail.PasswordAuthentication(User, Password);
        }
    }
    
    public void closeFolder() throws Exception {
    	this.inbox.close(false);
    }
}