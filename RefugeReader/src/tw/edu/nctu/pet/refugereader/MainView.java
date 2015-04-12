package tw.edu.nctu.pet.refugereader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.crypto.NoSuchPaddingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.view.View.OnKeyListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.haggle.*;
import org.haggle.DataObject.DataObjectException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;


public class MainView extends Activity implements OnClickListener {
	
	private static String myIP = "";
	
	public static String domain_name = "";
	public static String person_name = "";
	public static String id_type = "";
	public static String id_number = "";
	public static String device_name = "";
	
	public static AESEncrypter aese = null;
	
	private TelephonyManager tM= null;
	
	public static final int MENU_INTERESTS = 2;
	public static final int MENU_SHUTDOWN_HAGGLE = 3;
	
	public static final int REGISTRATION_FAILED_DIALOG = 1;
	public static final int SPAWN_DAEMON_FAILED_DIALOG = 2;
	public static final int PICTURE_ATTRIBUTES_DIALOG = 3;
	
	public static final String KEY_PICTURE_PATHS = "KeyPicturePath";
	public static final String KEY_HAGGLE_HANDLE = "KeyHaggleHandle";
	public static final String KEY_PICTURE_POS = "KeyPicturePos";
	public static final String KEY_PHOTOSHARE = "KeyPhotoShare";
	
	public static final String SHARE_TOPIC = "TIBS";
	
	private ImageAdapter imgAdpt = null;
	private NodeAdapter nodeAdpt = null;
	private DocShare ps = null;
	private Gallery gallery = null;
	private TextView neighlistHeader = null;
	private boolean shouldRegisterWithHaggle = true;
	private boolean firstStartHaggle = true;
	private File takenPicture = null;
	
	private ArrayAdapter<String> attributeAdpt2 = null;
	private ArrayAdapter<String> attributeAdpt3 = null;
	private ListView attributeListView2;
	
	
	private TextView view_phone;
	private TextView view_longitude;
	private TextView view_latitude;
	private TextView view_address;
	
	private TextView view_phone_near;
	private TextView view_longitude_near;
	private TextView view_latitude_near;
	private TextView view_address_near;
	
	private TextView textview_tag_counter;
    private TextView textview_beacon_counter;
    private TextView textview_newest_tag_name;
    private TextView textview_newest_tag_time;
    
	
	private int push_state = 0;
	private String push_file = "";
	
	String gg = "";
	
	private DataObject dObj = null;
	private DataObject infoObj = null;
	
	public static AESEncrypter getAESE(){
		return aese;
	}
	
	public static String device_id = null;
	
	private static boolean start_ok = false;
	
	private Handler myHandler01 = new Handler();
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        try {
			MainActivity.aese = new AESEncrypter();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        tM = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        
        device_id = tM.getDeviceId();
        
    	Log.d(DocShare.LOG_TAG, "MainView:onCreate()" + device_id);
    	
    	File file = new File(Environment.getExternalStorageDirectory() + "/Share");
        if(!file.exists()){
        	file.mkdir();
        }
        
        File file2 = new File(Environment.getExternalStorageDirectory() + "/TIBSinfo");
        if(!file2.exists()){
				file2.mkdir();
        }
        
        String oldPath = "/sdcard/TIBS";
        String newPath = "/sdcard/Share";
        
        ps = (DocShare)getApplication();
        
        ps.setMainView(this);

		super.onCreate(savedInstanceState);
		
		Button button3 = (Button)findViewById(R.id.by_self);
        Button button5 = (Button)findViewById(R.id.set_domain_button);
        Button button6 = (Button)findViewById(R.id.device_shutdown_button);
        Button button7 = (Button)findViewById(R.id.remove_refuge_button);
        button3.setOnClickListener(getDataBySelf);
        button5.setOnClickListener(setDomain);
        button6.setOnClickListener(powerOff);
        button7.setOnClickListener(removeRefuge);
        
        textview_tag_counter = (TextView)findViewById(R.id.tag_count);
        textview_beacon_counter = (TextView)findViewById(R.id.beacon_count);
        textview_newest_tag_name = (TextView)findViewById(R.id.newest_tag_name);
        textview_newest_tag_time = (TextView)findViewById(R.id.newest_tag_time);
        
        
        new Thread(){
            int count=0;
            public void run() {
              while(true){
            	  
            	  FilenameFilter beaconfilter = new FilenameFilter(){
                  	@Override
                  	public boolean accept(File dir, String filename){
                  		if(filename.indexOf("--") == 13 && filename.lastIndexOf("Beacon.json")!=-1)
                  			return true;
                  		return false;
                  	}
                  };
                  
                  FilenameFilter tagfilter = new FilenameFilter(){
                  	@Override
                  	public boolean accept(File dir, String filename){
                  		if(filename.startsWith("tag"))
                  			return true;
                  		return false;
                  	}
                  };
                  
                  File filePath = new File(Environment.getExternalStorageDirectory() + "/haggle");
                  File filePath2 = new File(Environment.getExternalStorageDirectory() + "/notPubTagDoc");
                  
                  File[] beaconList = filePath.listFiles(beaconfilter);
                  File[] tagList = filePath2.listFiles(tagfilter);
                  
                  int beacon_count = beaconList.length;
                  int tag_count = tagList.length;
                  int new_tag_count = tagList.length;
                  
                  String newest_tag_name = "";
                  String newest_tag_time = "";
                  
              	long newestfiletime = 0;
              	int newestfileindex = -1;
              	
            	for(int i=0;i<tagList.length;i++){
            		File tag = tagList[i];
            		long filetime = new Date(tag.lastModified()).getTime();
            		if(filetime>newestfiletime){
            			newestfiletime = filetime;
            			newestfileindex = i;
            		}
            	}
              	if(newestfileindex !=-1){
      				
              		newest_tag_name = tagList[newestfileindex].getName();
              		
              		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
              		newest_tag_time = sdFormat.format(new Date(tagList[newestfileindex].lastModified()));
              		
              	}
              	
              	newestfiletime = 0;
              	newestfileindex = -1;
                  	
 
                myHandler01.post(new myRunnable(tag_count, beacon_count,newest_tag_name,newest_tag_time));
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            }}.start();
        
    	Log.d(DocShare.LOG_TAG, "MainView:onCreate() done");	
    }
    
    public class myRunnable implements Runnable {
        private int tag_count = 0;
        private int beacon_count = 0;
        private String newest_tag_name = "";
        private String newest_tag_time = "";
        
        public myRunnable(int count1, int count2, String s1, String s2) {
          this.tag_count = count1;
          this.beacon_count = count2;
          this.newest_tag_name = "";
          this.newest_tag_time = "";
        }
        
        @Override
        public void run() {
          // TODO Auto-generated method stub
        	textview_tag_counter.setText(String.valueOf(tag_count));
        	textview_beacon_counter.setText(String.valueOf(beacon_count));
        	textview_newest_tag_name.setText(newest_tag_name);
        	textview_newest_tag_time.setText(newest_tag_time);
        }
        
      }
    
    /*private int[] getMyIp(){
        
		int ip[] = new int[4];
		
		//WifiManager WIFI_SERVICE
		WifiManager wifi_service = (WifiManager)getSystemService(WIFI_SERVICE);
		//wifi
		WifiInfo wifiInfo = wifi_service.getConnectionInfo();
		//IP
		int ipAddress = wifiInfo.getIpAddress();
		//MAND p IP
		for(int i=0;i<4;i++){
			ip[i] = ipAddress >> (i*8) & 0xff;
		}
		return ip;	 
	}*/     
    
	@Override
    public void onRestart() {
    	super.onRestart();
    	Log.d(DocShare.LOG_TAG, "MainView:onRestart()");
    }
	
    @Override
    public void onStart() {
    	
    	super.onStart();
    	final ProgressDialog dialog = new ProgressDialog(this, 
				ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage("Connecting to Haggle...");
		dialog.setIndeterminate(true);
    	
    	Log.d(DocShare.LOG_TAG, "MainView:onStart() freemem=" +  Runtime.getRuntime().freeMemory());	
		
		/*int local_ip[] = new int[4];
		
		local_ip = getMyIp();
		
		String newIP = "";
		
		newIP = "" + local_ip[2];
		
		//Loading Old IP
		try {
    		File file = new File("/sdcard/TIBSinfo/oldIP.txt");
			FileInputStream fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int)file.length()];
			fin.read(fileContent);
			byte[] tmp = fileContent;
			//byte[] tmp = MainActivity.aese.decrypt(fileContent);
			myIP = new String(tmp);
			Log.d("TxtShare", "load oldIP: " + myIP);
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		
		
		//Log.d("TxtShare", "my IP: " + myIP);
		/*if((newIP.compareTo("44")==0 || newIP.compareTo("45")==0) && (myIP.compareTo("44")!=0 && myIP.compareTo("45")!=0)){
			if(!shouldRegisterWithHaggle)
				//ps.getHaggleHandle().sendNodeDescription();
			ps.finiHaggle();
			shouldRegisterWithHaggle = true;
			//Log.d("temp", "finihaggle");
		}else if((newIP.compareTo("44")==0 && myIP.compareTo("45")==0) || (newIP.compareTo("45")==0 && myIP.compareTo("44")==0)){
			if(!shouldRegisterWithHaggle)
				//ps.getHaggleHandle().sendNodeDescription();
			ps.finiHaggle();
			shouldRegisterWithHaggle = true;
			//Log.d("temp", "finihaggle");
		}*/
			
			
		//Saving New IP
		/*FileWriter fw4 = null;
		try {
			fw4 = new FileWriter("/sdcard/TIBSinfo/oldIP.txt", false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter bw4 = new BufferedWriter(fw4);
		try {
			byte[] tmp = newIP.getBytes();
			//byte[] tmp = aese.encrypt(id_number.getBytes());
			FileOutputStream fos = new FileOutputStream("/sdcard/TIBSinfo/oldIP.txt");
			fos.write(tmp);
			fos.flush();
			fos.close();
			//bw.write(tmp.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			bw4.flush();
			bw4.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		
		//myIP = newIP;
		//ps.finiHaggle();
		//Log.d("TxtShare", "finihaggle");
    	
    	if (shouldRegisterWithHaggle) {
    		dialog.show();
    		
    		new Thread(new Runnable() {
				@Override
				public void run() {
					final int ret = ps.initHaggle();
					Log.d("temp", "inithaggle OK");
					start_ok = true;

					dialog.dismiss();
					
		    		if (ret != DocShare.STATUS_OK) {
		    			
		    			Log.d(DocShare.LOG_TAG, "Registration failed, showing alert dialog");
		    			
		    			MainView.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								String errorMsg = "Unknown error.";
				    			
				    			if (ret == DocShare.STATUS_SPAWN_DAEMON_FAILED) {
				    				errorMsg = "DocShare could not start Haggle daemon.";
				    			} else if (ret == DocShare.STATUS_REGISTRATION_FAILED) {
				    				errorMsg = "DocShare could not connect to Haggle.";
				    			}
				    			AlertDialog.Builder builder = new AlertDialog.Builder(MainView.this);
								builder.setMessage(errorMsg)
				    			.setCancelable(false)
				    			.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
				    				public void onClick(DialogInterface dialog, int id) {
				    					dialog.cancel();
				    					finish();
				    				}
				    			});
				    			AlertDialog alert = builder.create();
				    			alert.show();
							}
		    			});
		    		} else {
		    			Log.d(DocShare.LOG_TAG, "Registration with Haggle successful");
		    	    	shouldRegisterWithHaggle = false;
		    		}
				}
    			
    		}).start();
    		
    	}
    	
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	Log.d(DocShare.LOG_TAG, "MainView:onResume()");
    	
    	TextView textview = (TextView)findViewById(R.id.show_domain);
    	
    	try {
    		File file = new File("/sdcard/TIBSinfo/domain.txt");
			FileInputStream fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int)file.length()];
			fin.read(fileContent);
			byte[] tmp = fileContent;
			domain_name = new String(tmp);
			Log.d("TxtShare", domain_name);
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if(domain_name.compareTo("")!=0)
        	textview.setText("  " + domain_name);
        
        //======================================================
        
        TextView textview2 = (TextView)findViewById(R.id.show_person);
        
        try {
    		File file = new File("/sdcard/TIBSinfo/person.txt");
			FileInputStream fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int)file.length()];
			fin.read(fileContent);
			byte[] tmp = fileContent;
			person_name = new String(tmp);
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if(person_name.compareTo("")!=0)
        	textview2.setText("  " + person_name);
        
        //======================================================
        
        TextView textview3 = (TextView)findViewById(R.id.show_idType);
        
        try {
    		File file = new File("/sdcard/TIBSinfo/idType.txt");
			FileInputStream fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int)file.length()];
			fin.read(fileContent);
			byte[] tmp = fileContent;
			id_type = new String(tmp);
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if(id_type.compareTo("")!=0)
        	textview3.setText("  " + id_type);
        
        //======================================================
        
        TextView textview4 = (TextView)findViewById(R.id.show_idNumber);
        
        try {
    		File file = new File("/sdcard/TIBSinfo/idNumber.txt");
			FileInputStream fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int)file.length()];
			fin.read(fileContent);
			byte[] tmp = fileContent;
			id_number = new String(tmp);
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if(id_number.compareTo("")!=0)
        	textview4.setText("  " + id_number);
        
        //======================================================
        
        TextView textview5 = (TextView)findViewById(R.id.show_deviceName);
        
        try {
    		File file = new File("/sdcard/TIBSinfo/deviceName.txt");
			FileInputStream fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int)file.length()];
			fin.read(fileContent);
			byte[] tmp = fileContent;
			//byte[] tmp = MainActivity.aese.decrypt(fileContent);
			device_name = new String(tmp);
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if(device_name.compareTo("")!=0)
        	textview5.setText("  " + device_name);     
        
	}
	@Override
	protected void onPause() {
    	super.onPause();
    	Log.d(DocShare.LOG_TAG, "MainView:onPause()");

 	}
    @Override
    protected void onStop() {
    	super.onStop();
    	Log.d(DocShare.LOG_TAG, "MainView:onStop()");
    	
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Log.d(DocShare.LOG_TAG, "MainView:onDestroy()");
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	switch (keyCode) {
    	case KeyEvent.KEYCODE_BACK:
    		break;
    	case KeyEvent.KEYCODE_HOME:
    		break;
    	}
    	
		return super.onKeyDown(keyCode, event);
	}
    
    @Override
    protected void onPrepareDialog(int id, Dialog d) {
    	
    }
    
    public void onClick(DialogInterface dialog, int which) {
    	Log.d(DocShare.LOG_TAG,"onClick: call finish()");
        finish();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
    	case DocShare.STATUS_REGISTRATION_FAILED:
    		return new AlertDialog.Builder(this)
    		.setTitle(R.string.haggle_dialog_title)
    		.setIcon(android.R.drawable.ic_dialog_alert)
    		.setMessage(R.string.registration_failed)
    		.setPositiveButton(android.R.string.ok, this)
    		.setCancelable(false)
    		.create();
    	case DocShare.STATUS_SPAWN_DAEMON_FAILED:
    		return new AlertDialog.Builder(this)
    		.setTitle(R.string.haggle_dialog_title)
    		.setIcon(android.R.drawable.ic_dialog_alert)
    		.setMessage(R.string.spawn_daemon_failed)
    		.setPositiveButton(android.R.string.ok, this)
    		.setCancelable(false)
    		.create();
    	}

    	return null;
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_INTERESTS, 0, R.string.menu_interests).setIcon(android.R.drawable.ic_menu_search);
        menu.add(0, MENU_SHUTDOWN_HAGGLE, 0, R.string.menu_shutdown_haggle).setIcon(android.R.drawable.ic_lock_power_off);

        return true;
	}
   

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	final Intent i = new Intent();
    	
    	switch (item.getItemId()) {
    		
    	case MENU_INTERESTS:
        	i.setClass(getApplicationContext(), InterestView.class);
        	this.startActivityForResult(i, DocShare.ADD_INTEREST_REQUEST);
    		return true;
    	case MENU_SHUTDOWN_HAGGLE:
    		shouldRegisterWithHaggle = true;
    		ps.shutdownHaggle();
    		return true;
			
    	}
    	return false;
    }

	private void onAddInterestResult(int resultCode, Intent data) {
		
		Log.d(DocShare.LOG_TAG, "Start onAddInterestResult");
		
		String[] deletedInterests = data.getStringArrayExtra("deleted");
		String[] addedInterests = data.getStringArrayExtra("added");
		
		if (addedInterests != null && addedInterests.length != 0) {
			Log.d(DocShare.LOG_TAG, "AddInterest != NULL");
			Attribute[] aa = new Attribute[addedInterests.length];
			for (int i = 0; i < addedInterests.length; i++) {
				aa[i] = new Attribute("JSON", addedInterests[i], 1);
				Log.d(DocShare.LOG_TAG, "Added interest " + addedInterests[i]);
			}
			ps.getHaggleHandle().registerInterests(aa);
			
			// Call dispose to free native data before GC
			for (int i = 0; i < aa.length; i++) {
				aa[i].dispose();
			}
		}

		if (deletedInterests != null && deletedInterests.length != 0) {
			Attribute[] aa = new Attribute[deletedInterests.length];
			for (int i = 0; i < deletedInterests.length; i++) {
				aa[i] = new Attribute("JSON", deletedInterests[i], 1);
				Log.d(DocShare.LOG_TAG, "Deleted interest " + deletedInterests[i]);
			}
			ps.getHaggleHandle().unregisterInterests(aa);

			// Call dispose to free native data before GC
			for (int i = 0; i < aa.length; i++) {
				aa[i].dispose();
			}
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
    	
		switch (requestCode) {
		case DocShare.ADD_INTEREST_REQUEST:
			onAddInterestResult(resultCode, data);
			break;
		default:
			Log.d(DocShare.LOG_TAG, "Unknown activity result");
		}
    }

    public class DataUpdater implements Runnable {
    	int type;
    	DataObject dObj = null;
    	Node[] neighbors = null;
    	
    	public DataUpdater(DataObject dObj)
    	{
    		this.type = org.haggle.EventHandler.EVENT_NEW_DATAOBJECT;
    		this.dObj = dObj;
    	}
    	public DataUpdater(Node[] neighbors)
    	{
    		this.type = org.haggle.EventHandler.EVENT_NEIGHBOR_UPDATE;
    		if (this.neighbors != null) {
    			for (int i = 0; i < this.neighbors.length; i++) {
    				this.neighbors[i].dispose();
    			}
    		}
    		this.neighbors = neighbors;
    	}
        public void run() {
    		Log.d(DocShare.LOG_TAG, "Running data updater, thread id=" + Thread.currentThread().getId());
        	switch(type) {
        	case org.haggle.EventHandler.EVENT_NEIGHBOR_UPDATE:
        		Log.d(DocShare.LOG_TAG, "Event neighbor update");
        		nodeAdpt.updateNeighbors(neighbors);
        		break;
        	case org.haggle.EventHandler.EVENT_NEW_DATAOBJECT:
        		Log.d(DocShare.LOG_TAG, "Event new data object");
		        imgAdpt.updatePictures(dObj);
        		break;
        	}
    		Log.d(DocShare.LOG_TAG, "data updater done");
        }
    }
    
    public void copyFolder(String oldPath, String newPath) {     
    	
	    try{
		    (new File(newPath)).mkdirs();
		    File a=new File(oldPath);
		    String[] file=a.list();
		    File temp=null;
		    for (int i = 0; i < file.length; i++) {
			    if(oldPath.endsWith(File.separator)){
			    	temp=new File(oldPath+file[i]);
			    }
			    else{
			    	temp=new File(oldPath+File.separator+file[i]);
			    }
			    
			    if(temp.isFile()){
			    	
				    FileInputStream input = new FileInputStream(temp);
				    FileOutputStream output = new FileOutputStream(newPath + "/"+ System.currentTimeMillis() + "--" +
				    (temp.getName()).toString());
				    byte[] b = new byte[1024 * 5];
				    int len;
				    while ( (len = input.read(b)) != -1) {
				    	output.write(b, 0, len);
				    }
				    output.flush();
				    output.close();
				    input.close();
			   }
			    if(temp.isDirectory()){
			    	copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]);
			    }
		    }
	    }
	    catch(Exception e) {
		    System.out.println("Copy Direction error");
		    e.printStackTrace();
	    }
    }
    
    public void pushToPi(DataObject dObj){
    	
    	String oldPath = dObj.getFilePath();
    	
    	copyFile(oldPath);
    	
    	ps.getHaggleHandle().publishDataObject(dObj);
    	
    	new File("/sdcard/Share/temp.txt").renameTo(new File(oldPath));
    }
    
    public void powerOffPush(DataObject dObj){
    	
    	String oldPath = dObj.getFilePath();
    	
    	copyFile(oldPath);
    	
    	ps.getHaggleHandle().publishDataObject(dObj);
    	
    	dObj.dispose();
    	
    	new File("/sdcard/Share/temp.txt").renameTo(new File(oldPath));
    }
    
    public void pushToDevice(DataObject dObj){
    	
    	String oldPath = dObj.getFilePath();
    	
    	copydocFile(oldPath);
    	
    	ps.getHaggleHandle().publishDataObject(dObj);
    	
    	dObj.dispose();
    	
    	new File("/sdcard/Share/temp2.txt").renameTo(new File(oldPath));
    }
    
    public void copyFile(String oldPath) {
		try {
			File file = new File(oldPath);
			
			String newPath = "/sdcard/Share/temp.txt";
			
			File file2 = new File(newPath);
			
			if(!file2.exists())
			{
			    try {
			        file2.createNewFile();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }
			}
			
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ( (byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					Log.d("temp", "" + bytesum);
					fs.write(buffer, 0, byteread);
				}
					inStream.close();
					fs.flush();
					fs.close();
			}
		}
		catch(Exception e) {
			Log.d("temp", "Copy Direction error");
			e.printStackTrace();
		}
	}
    
    public void copydocFile(String oldPath) {
		try {
			File file = new File(oldPath);
			
			String newPath = "/sdcard/Share/temp2.txt";
			
			Log.d("temp", "copy to INFO: " + newPath);
			
			File file2 = new File(newPath);
			
			if(!file2.exists())
			{
			    try {
			        file2.createNewFile();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }
			}
			
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ( (byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					Log.d("temp", "" + bytesum);
					fs.write(buffer, 0, byteread);
				}
					inStream.close();
					fs.flush();
					fs.close();
			}
		}
		catch(Exception e) {
			Log.d("temp", "Copy Direction error");
			e.printStackTrace();
		}
	}
    
    public void copyFile(String oldPath, String newPath) {
		try {
			File file = new File(oldPath);
			
			Log.d("temp", "copy to TIBS: " + newPath);
			
			File file2 = new File(newPath);
			
			if(!file2.exists())
			{
			    try {
			        file2.createNewFile();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }
			}
			
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ( (byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					Log.d("temp", "" + bytesum);
					fs.write(buffer, 0, byteread);
				}
					inStream.close();
					fs.flush();
					fs.close();
			}
		}
		catch(Exception e) {
			Log.d("temp", "Copy Direction error");
			e.printStackTrace();
		}
	}
    
    private Button.OnClickListener getDataBySelf = new Button.OnClickListener()
    {
          public void onClick(View v)
          {
        	  Intent intent = new Intent();
        	  intent.setClass(MainView.this, GetDataBySelf.class);
        	  startActivity(intent);
        	  
          }
    };
    
    private Button.OnClickListener setDomain = new Button.OnClickListener()
    {
    	
    	public void onClick(View v)
        {
      	  Intent intent = new Intent();
      	  intent.setClass(MainView.this, SetDomain.class);
      	  startActivity(intent);
      	  
        }
    
    };
    
    private Button.OnClickListener powerOff = new Button.OnClickListener()
    {
    	
    	public void onClick(View v)
        {
    		
    		String have_to_pub_tag_list_filepath = "/sdcard/TIBSinfo/not_pub_tag.txt";
    		
    		File shareFilePath = new File(Environment.getExternalStorageDirectory() + "/notPubTagDoc");
    		
    		FilenameFilter tagfilter = new FilenameFilter(){
            	@Override
            	public boolean accept(File dir, String filename){
            		if(filename.startsWith("tag"))
            			return true;
            		return false;
            	}
            };
            
            File[] shareTagList = shareFilePath.listFiles(tagfilter);
            
            long timestamp = System.currentTimeMillis();
        	
        	String newPath = "/sdcard/Share/offload_" + timestamp + "taglist.txt";
        	
        	boolean flag = false;
        	
        	if(shareTagList.length>0){
        		flag = true;
        	}
    		
        	Model model_union = ModelFactory.createDefaultModel();
        	
			
			//Tag Aggregation
    		for(int i=0;i<shareTagList.length;i++){
    			String filepath = shareTagList[i].getPath();
    			String filename = shareTagList[i].getName();
    			
    			Model model = ModelFactory.createDefaultModel();
    			InputStream in = FileManager.get().open(filepath);
    			if (in == null) {
    			    throw new IllegalArgumentException(
    			                                 "File: " + filepath + " not found");
    			}
    			model.read(in, null, "N-TRIPLES");
    			
    			try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    			model_union.add(model);			
    			
    			new File(filepath).delete();
    			
    		} 
    		
    		
    		
    		if(flag){
    			
    			StringWriter out = new StringWriter();
    			model_union.write(out, "N-TRIPLE");
    			
    			String new_uuid = "" + UUID.randomUUID();
    			String new_tag_path = "/sdcard/Share/tag_" + new_uuid + ".rdf";
        		
        		File file2 = new File(new_tag_path);
        		
        		try {
					if(file2.createNewFile())
						Log.d("temp", "create tag file: " + new_tag_path);
					
					FileWriter fw = new FileWriter(new_tag_path);
	        		
	        		fw.write(out.toString());
	        		
	        		fw.flush();
	        		
	        		fw.close();
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		
        		try {
					FileWriter fw2 = new FileWriter(newPath, true);
					BufferedWriter bw = new BufferedWriter(fw2);
					bw.write("tag_" + new_uuid + ".rdf");
					bw.newLine();
					bw.flush();
					bw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		
        		
    			//Publish Aggre Tag
	    		try {
					DataObject tagObj = new DataObject();
					tagObj = new DataObject(new_tag_path);
					tagObj.addAttribute("JSON", "offloadTAG", 1);
					
					powerOffPush(tagObj);
					
					Log.d("temp", "publish taglist");
				} catch (DataObjectException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	    		try {
					DataObject tagObj2 = new DataObject();
					tagObj2 = new DataObject(newPath);
					tagObj2.addAttribute("JSON", "offloadTAG", 1);
					
					powerOffPush(tagObj2);
					
					Log.d("temp", "publish taglist");
				} catch (DataObjectException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		
    		flag = false;
      	  
        }
    	
    };
     
    private Button.OnClickListener removeRefuge = new Button.OnClickListener()
    {
    	
    	public void onClick(View v)
        {
    		
    		File FilePath1 = new File(Environment.getExternalStorageDirectory() + "/TIBS/Refuge_ntu.json");
    		File FilePath2 = new File(Environment.getExternalStorageDirectory() + "/TIBS/Refuge_dahu.json");
    		
    		FilePath1.delete();
    		FilePath2.delete();
      	  
        }
    	
    };  
    
}
