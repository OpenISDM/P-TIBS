package tw.edu.nctu.pet.refugereader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.haggle.Attribute;
import org.haggle.DataObject;
import org.haggle.DataObject.DataObjectException;
import org.haggle.Handle;
import org.haggle.Node;
import org.haggle.LaunchCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hp.hpl.jena.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import java.io.StringWriter;
import java.util.UUID;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class DocShare extends Application implements org.haggle.EventHandler {
	public static final String LOG_TAG = "DocShare";
	
	public static final int STATUS_OK = 0;
	public static final int STATUS_ERROR = -1;
	public static final int STATUS_REGISTRATION_FAILED = -2;
	public static final int STATUS_SPAWN_DAEMON_FAILED = -3;

	static final int ADD_INTEREST_REQUEST = 0;
	static final int IMAGE_CAPTURE_REQUEST = 1;
	static final int ADD_PICTURE_ATTRIBUTES_REQUEST = 2;
	static final int SHARE_MAD = 3;

	private MainView pv = null;
	private org.haggle.Handle hh = null;
	private int status = STATUS_OK;
	private android.os.Vibrator vibrator = null;
	private long lastNeighborVibrateTime = -1, lastDataObjectVibrateTime = -1;
	
	private static String my_device_id = null;
	
	private static ArrayList<String> get_doc_uuid_list = new ArrayList<String>();
	
	public static ArrayList<String> have_to_send_tag_uuid_list = new ArrayList<String>();
	
	public static ArrayList<String> beacon_have_to_send_tag_uuid_list = new ArrayList<String>();
	
	private static String newest_doc_uuid = null;
	
	private static ArrayList<String> wait_doc_info = new ArrayList<String>();
	
	private static ArrayList<String> wait_info_doc = new ArrayList<String>();
	
	private static long newest_ntu_time = 0;
	private static long newest_dahu_time = 0;
	
	private static ArrayList<String> wait_to_offload_tag_list = new ArrayList<String>();
	
	private static ArrayList<String> in_haggle_tag_list = new ArrayList<String>();
	
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	public void onCreate() {
		super.onCreate();
		
		Log.d(DocShare.LOG_TAG, "DocShare:onCreate(), thread id=" + Thread.currentThread().getId());
		
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		my_device_id = info.getMacAddress();
		
		vibrator = (android.os.Vibrator)getSystemService(VIBRATOR_SERVICE);
	}

	public void onLowMemory() {
		super.onLowMemory();
		Log.d(DocShare.LOG_TAG, "DocShare:onLowMemory()");	
	}

	public void onTerminate() {
		super.onTerminate();
		Log.d(DocShare.LOG_TAG, "DocShare:onTerminate()");	
		//finiHaggle();
	}
	public void setMainView(MainView pv) {
		Log.d(DocShare.LOG_TAG, "DocShare: Setting pv");
		this.pv = pv;
	}
	
	
	public MainView getMainView() {
		return pv;
	}
	public Handle getHaggleHandle() {
		return hh;
	}

	public void tryDeregisterHaggleHandle() {
		finiHaggle();
	}
	
	public int initHaggle() {
		
		if (hh != null)
			return STATUS_OK;

		int status = Handle.getDaemonStatus();
		
		if (status == Handle.HAGGLE_DAEMON_NOT_RUNNING || 
				status == Handle.HAGGLE_DAEMON_CRASHED) {
			Log.d(DocShare.LOG_TAG, "Trying to spawn Haggle daemon");

			if (!Handle.spawnDaemon(new LaunchCallback() {
				
				public int callback(long milliseconds) {
					
					Log.d(DocShare.LOG_TAG, "Spawning milliseconds..." + milliseconds);
					
					if (milliseconds == 0) {
						// Daemon launched
					} else if (milliseconds >= 10000) {
						Log.d(DocShare.LOG_TAG, "Spawning failed, giving up");
						return -1;
					}
					return 0;
				}
			})) {
				Log.d(DocShare.LOG_TAG, "Spawning failed...");
				return STATUS_SPAWN_DAEMON_FAILED;
			}
		}
		long pid = Handle.getDaemonPid();

		Log.d(DocShare.LOG_TAG, "Haggle daemon pid is " + pid);

		int tries = 1;
		
		

		while (tries > 0) {
			try {
				hh = new Handle("Share");

			} catch (Handle.RegistrationFailedException e) {
				Log.e(DocShare.LOG_TAG, "Registration failed : " + e.getMessage());

				if (e.getError() == Handle.HAGGLE_BUSY_ERROR) {
					Handle.unregister("Share");
					continue;
				} else if (--tries > 0) 
					continue;

				Log.e(DocShare.LOG_TAG, "Registration failed, giving up");
				return STATUS_REGISTRATION_FAILED;
			}
			break;
		}

		hh.registerEventInterest(EVENT_NEIGHBOR_UPDATE, this);
		hh.registerEventInterest(EVENT_NEW_DATAOBJECT, this);
		hh.registerEventInterest(EVENT_INTEREST_LIST_UPDATE, this);
		hh.registerEventInterest(EVENT_HAGGLE_SHUTDOWN, this);
		
		hh.eventLoopRunAsync(this);
		
		new TreatReceive().execute("");
		
		return STATUS_OK;
	}
	public synchronized void finiHaggle() {
		if (hh != null) {
			hh.eventLoopStop();
			hh.dispose();
			hh = null;
		}
	}
	public int getStatus() {
		return status;
	}
	public void shutdownHaggle() {
		hh.shutdown();
	}
	public boolean registerInterest(Attribute interest) {
		if (hh.registerInterest(interest) == 0) 
			return true;
		return false;
	}
	public void onNewDataObject(DataObject dObj)
	{
		
		if (pv == null){
			return;
		}
		
		Log.d(DocShare.LOG_TAG, "Got new data object, thread id=" + Thread.currentThread().getId());
		
		Log.d(DocShare.LOG_TAG, dObj.getFilePath() + " " + dObj.getAttributes()[0].getName());

		if (dObj.getAttribute("JSON", 0) == null) {
			Log.d(DocShare.LOG_TAG, "DataObject has no Picture attribute");
			return;
		}

		Log.d(DocShare.LOG_TAG, "Getting filepath");
	
	}
	@Override
	public void onNeighborUpdate(Node[] neighbors) {

		if (pv == null)
			return;
		
		Log.d(DocShare.LOG_TAG, "Got neighbor update, thread id=" + 
				Thread.currentThread().getId() + " num_neighbors=" + neighbors.length);

		// Make sure we do not vibrate more than once every 5 secs or so
		long currTime = System.currentTimeMillis();

		if (lastNeighborVibrateTime == -1 || currTime - lastNeighborVibrateTime > 5000) {
			long[] pattern = { 0, 500, 100, 300 };
			
			if (neighbors.length > 0 || lastNeighborVibrateTime != -1)
				vibrator.vibrate(pattern, -1);
			
			lastNeighborVibrateTime = currTime;
		}
		Log.d(DocShare.LOG_TAG, "Updating UI neigh");
		//pv.runOnUiThread(pv.new DataUpdater(neighbors));
	}

	public void onShutdown(int reason) {
		Log.d(DocShare.LOG_TAG, "Shutdown event, reason=" + reason);
		if (hh != null) {
			hh.dispose();
			hh = null;
		} else {
			Log.d(DocShare.LOG_TAG, "Shutdown: handle is null!");
		}
		pv.runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(pv);
    			builder.setMessage("Haggle was shutdown and is no longer running. Press Quit to exit DocShare.")
    			.setCancelable(false)
    			.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int id) {
    					dialog.cancel();
    					pv.finish();
    				}
    			});
    			AlertDialog alert = builder.create();
    			alert.show();
			}
		});
	}
	
	public void onInterestListUpdate(Attribute[] interests) {
		Log.d(DocShare.LOG_TAG, "Setting interests (size=" + interests.length + ")");
		InterestView.setInterests(interests);
	}

	public void onEventLoopStart() {
		Log.d(DocShare.LOG_TAG, "Event loop started.");
		hh.getApplicationInterestsAsync();
	}

	public void onEventLoopStop() {
		Log.d(DocShare.LOG_TAG, "Event loop stopped.");
	}
	
	public void copyFileToTIBS(String oldPath) {
		try {
			File file = new File(oldPath);
			
			String newPath = "/sdcard/TIBS/" + file.getName().split("--")[1];
			
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
	
	private String makeTag(String uuid, String doc_uuid, String domainname, String person, String deviceId, String idType, String idNumber, String deviceName, String deviceIdType) {
		
		TelephonyManager tM=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);	
		
		String prefix = "none";
		String myprefix = "none";
		
		prefix = domainname;
		myprefix = MainView.domain_name;
		
		Model model = ModelFactory.createDefaultModel();
		
		String pre_property = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#";
		String pre_property_person = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Person_";
		String pre_property_device = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Device_";
		String pre_property_domain = "http://pet.cs.nctu.edu.tw/ontology/openisdm/infoflow#Domain_";
		
		Property has_uuid = model.createProperty(pre_property + "hasUUID");
		Property time = model.createProperty(pre_property + "hasOccurAt");
		
		Property title = model.createProperty(pre_property + "hasTitle");
		Property sensitivity_level = model.createProperty(pre_property + "hasSensitivityLevel");
		Property has_version = model.createProperty(pre_property + "hasVersion");
		
		Property has_device_id_type = model.createProperty(pre_property + "hasDeviceIDtype");
		Property has_id = model.createProperty(pre_property + "hasID");
		Property has_alias = model.createProperty(pre_property + "hasAlias");
		
		Property has_domain_name = model.createProperty(pre_property + "hasDomainName");
		
		Property has_person_id_type = model.createProperty(pre_property + "hasPersonIDtype");
		Property has_legal_name = model.createProperty(pre_property + "hasLegalName");
		
		Property doc = model.createProperty(pre_property + "isTransportSessionOf");
		Property doc_re = model.createProperty(pre_property + "hasTransportSession");
		Property from = model.createProperty(pre_property + "hasSource");
		Property to = model.createProperty(pre_property + "hasDestination");

		Property belong_to = model.createProperty(pre_property + "belongsTo"); 
		Property has_owner = model.createProperty(pre_property + "ownedBy");
		Property write_by = model.createProperty(pre_property + "hasAuthor");
		
		Resource record_tag = model.createResource(pre_property + "TransportSession_" + uuid).addProperty(has_uuid, uuid);
		Resource docRes = model.createResource(pre_property + "Document_" + doc_uuid);
		
		record_tag.addProperty(doc, docRes);
		
		Resource person_from = model.createResource(pre_property_person + prefix + person).addProperty(has_person_id_type, idType).addProperty(has_id, idNumber)
						.addProperty(has_legal_name, person).addProperty(belong_to, model.createResource(pre_property_domain + prefix).addProperty(has_domain_name, domainname.split(".nctu")[0].substring(7)));
		
		Resource person_to = model.createResource(pre_property_person + myprefix + MainView.person_name).addProperty(has_person_id_type, MainView.id_type).addProperty(has_id, MainView.id_number)
						.addProperty(has_legal_name, MainView.person_name).addProperty(belong_to, model.createResource(pre_property_domain + myprefix).addProperty(has_domain_name, MainView.domain_name.split(".nctu")[0].substring(7)));
		
		Log.d("temp", "person to ok");
		
		record_tag.addProperty(from, model.createResource(pre_property_device + deviceId).addProperty(has_device_id_type, deviceIdType).addProperty(has_id, deviceId).addProperty(has_alias, deviceName)
									.addProperty(has_owner, person_from)
									.addProperty(belong_to, model.createResource(pre_property_domain + prefix).addProperty(has_domain_name, domainname.split(".nctu")[0].substring(7))));
		
		record_tag.addProperty(to, model.createResource(pre_property_device + my_device_id).addProperty(has_device_id_type, "Mac address").addProperty(has_id, device_id).addProperty(has_alias, MainView.device_name)
									.addProperty(has_owner, person_to)
									.addProperty(belong_to, model.createResource(pre_property_domain + myprefix).addProperty(has_domain_name, MainView.domain_name.split(".nctu")[0].substring(7))));

		long timestamp = System.currentTimeMillis() - 28800;
		record_tag.addProperty(time, "" + timestamp);
		
		docRes.addProperty(doc_re, record_tag);
		
		StringWriter out = new StringWriter();
		
		model.write(out, "N-TRIPLES");
		
		String str = out.toString();
		
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return str;
		
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
	
	private class TreatReceive extends AsyncTask<String, Void, String> {
		
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
		}
		
        @Override
        protected String doInBackground(String... urls) {
        	
    		FilenameFilter docfilter = new FilenameFilter(){
            	@Override
            	public boolean accept(File dir, String filename){
            		if(filename.indexOf("--") == 13 && filename.lastIndexOf("Beacon.json")==-1)
            			return true;
            		return false;
            	}
            };
            
            FilenameFilter dahu_docfilter = new FilenameFilter(){
            	@Override
            	public boolean accept(File dir, String filename){
            		if(filename.indexOf("--") == 13 && filename.lastIndexOf("Refuge_dahu.json")!=-1)
            			return true;
            		return false;
            	}
            };
            
            FilenameFilter ntu_docfilter = new FilenameFilter(){
            	@Override
            	public boolean accept(File dir, String filename){
            		if(filename.indexOf("--") == 13 && filename.lastIndexOf("Refuge_ntu.json")!=-1)
            			return true;
            		return false;
            	}
            };
            
            FilenameFilter infofilter = new FilenameFilter(){
            	@Override
            	public boolean accept(File dir, String filename){
            		if(filename.startsWith("info"))
            			return true;
            		return false;
            	}
            };
            
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
            
            FilenameFilter offloadfilter = new FilenameFilter(){
            	@Override
            	public boolean accept(File dir, String filename){
            		if(filename.startsWith("offload"))
            			return true;
            		return false;
            	}
            };
            
            File filePath = new File(Environment.getExternalStorageDirectory() + "/haggle");
            File shareFilePath = new File(Environment.getExternalStorageDirectory() + "/Share");
            File notPubTagFilePath = new File(Environment.getExternalStorageDirectory() + "/notPubTagDoc");
            File[] docList = filePath.listFiles(docfilter);
            File[] infoList = filePath.listFiles(infofilter);
            File[] beaconList = filePath.listFiles(beaconfilter);
            File[] tagList = filePath.listFiles(tagfilter);
            File[] offloadList = filePath.listFiles(offloadfilter);
            int doc_count = docList.length;
            int info_count = infoList.length;
            int beacon_count = beaconList.length;
            int tag_count = tagList.length;
            int offload_count = offloadList.length;
            
            boolean in_doc = false;
            
            boolean wait_offload = false;
            
            long wait_offload_start_time = 0;
            
        	while(hh != null){
        		docList = filePath.listFiles(docfilter);
                infoList = filePath.listFiles(infofilter);
                beaconList = filePath.listFiles(beaconfilter);
                tagList = filePath.listFiles(tagfilter);
                offloadList = filePath.listFiles(offloadfilter);
                
                int new_doc_count = docList.length;
                int new_info_count = infoList.length;
                int new_beacon_count = beaconList.length;
                int new_tag_count = tagList.length;
                int new_offload_count = offloadList.length;
                
                if(new_doc_count>doc_count){
                	
                	int get_doc_count = new_doc_count - doc_count;
                	
                	doc_count = new_doc_count;
                	long newestfiletime = 0;
                	int newestfileindex = -1;
                	
                	ArrayList<Integer> had_add_index = new ArrayList<Integer>();
                	
                	for(int j=0;j<get_doc_count;j++){
	                	for(int i=0;i<docList.length;i++){
	                		if(!had_add_index.contains(i)){
		                		File doc = docList[i];
		                		long filetime = new Date(doc.lastModified()).getTime();
		                		if(filetime>newestfiletime){
		                			newestfiletime = filetime;
		                			newestfileindex = i;
		                		}
	                		}
	                	}
	                	had_add_index.add(newestfileindex);
	                	newestfiletime = 0;
	                	newestfileindex = -1;
                	}
                	if(!had_add_index.isEmpty()){
                		
                		for(int i=0;i<had_add_index.size();i++){
	                		
	                		String result = "";
	                		
	                		String filepath = docList[had_add_index.get(i)].getPath();
	                		
	                		String fileName = docList[had_add_index.get(i)].getName();
	                		
	                		String realFileName = fileName.split("--")[1];
	                		
	                		long fileTimeStamp = Long.parseLong(fileName.split("--")[0]);
	                		
	                		long timestamp = System.currentTimeMillis();
	                		
	                		if(fileTimeStamp == timestamp){
	                			timestamp++;
	                		}
	                		
	                		if(filepath.indexOf(".rdf")!=-1){
	                			continue;
	                		}
	                		
	                		String version_check = null;
	                		
	                		if(realFileName.indexOf("dahu")!=-1){
	                			version_check = "dahu";
	            			}else if(realFileName.indexOf("ntu")!=-1){
	            				version_check = "ntu";
	            			}else{
							
	            			}
	                		
							//If get Document but doesn't get the source device information, WAIT THE DEVICE INFORMATION
	                		if(!wait_doc_info.contains(fileName)){
	                			
	                			try{
		            	        	FileReader fr = new FileReader(filepath);
		            	        	BufferedReader br = new BufferedReader(fr);
		            	        	String temp = br.readLine();
		            	        	while(temp != null){
		            	        		result += temp;
		            	        		temp = br.readLine();
		            	        	}
		            	        }catch(Exception e){
		            	        	e.printStackTrace();
		            	        }
		            	        
		            	        String gg = result;
		            	        JSONArray array = new JSONArray();
		            	        
		            	        if(gg.compareTo("")!=0){
		            	        
		            		        try {
		            					array = new JSONArray(gg);
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            		        
		            		        if(array.length()==11){
		            		        	continue;
		            		        }
		            		        
		            	        	JSONObject obj = new JSONObject();
		            	        	String doc_uuid = null;
		            	        	try {
		            					obj = array.getJSONObject(array.length()-2);
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            	        	try {
		            	        		doc_uuid = obj.getString("document");
		            	        		newest_doc_uuid = doc_uuid;
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            	        	
		            	        	
		            	        	JSONObject obj2 = new JSONObject();
		            	        	String new_version = null;
		            	        	try {
		            					obj2 = array.getJSONObject(array.length()-1);
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            	        	try {
		            	        		Log.d("temp", "check version");
		            	        		new_version = obj2.getString("version");
		            	        		Log.d("temp", "version: " + new_version);
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            	        	
		            	        	File last_dahu_version_file = new File("/sdcard/TIBSinfo/version_" + version_check + ".txt");
				        			
				        			String last_dahu_version = null;
				        			
				        			if(last_dahu_version_file.exists()){
				        			
					        			try {
					        				FileReader fr = new FileReader(last_dahu_version_file);
					        				BufferedReader br = new BufferedReader(fr);
					        				last_dahu_version = br.readLine();
					        				br.close();
					        				fr.close();
					        				
					        			} catch (FileNotFoundException e) {
					        				// TODO Auto-generated catch block
					        				e.printStackTrace();
					        			} catch (IOException e) {
					        				// TODO Auto-generated catch block
					        				e.printStackTrace();
					        			}
					        			
				        			}else{
				        				last_dahu_version = "00.00.00.00";
				        			}
				        			
				        			int[] version = new int[4];
			            			int[] old_version = new int[4];
			            			
			            			boolean update = false;
			            			
			            			String[] vv = new_version.split("\\.");
	                		    	for(int j=0;j<vv.length;j++){
	                		    		version[j] = Integer.parseInt(vv[j]);
	                		    	}
	         
	        	    		    	if(last_dahu_version==null){
	        	    		    		last_dahu_version = "00.00.00.00";
	        	    		    	}
	        						String[] vvo = last_dahu_version.split("\\.");
	        						for(int j=0;j<vvo.length;j++){
	        	    		    		old_version[j] = Integer.parseInt(vvo[j]);
	        						}
	        						
	        						for(int j=0;j<4;j++){
			        		    		if(version[j]>old_version[j]){
			        		    			update = true;
			        		    			
			        		    			break;
			        		    		}
			        		    	}
	        						
	        						if(!update){
	        							continue;
	        						}
	                			
	                			wait_info_doc.add(fileName);
	                			
		            	        }
	                		}else{
		            			
		            			try{
		            	        	FileReader fr = new FileReader(filepath);
		            	        	BufferedReader br = new BufferedReader(fr);
		            	        	String temp = br.readLine();
		            	        	while(temp != null){
		            	        		result += temp;
		            	        		temp = br.readLine();
		            	        	}
		            	        }catch(Exception e){
		            	        	e.printStackTrace();
		            	        }
		            	        
		            	        String gg = result;
		            	        Log.d("jena", "" + gg);
		            	        JSONArray array = new JSONArray();
		            	        
		            	        if(gg.compareTo("")!=0){
		            	        
		            		        try {
		            					array = new JSONArray(gg);
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            		        
		            		        if(array.length()==11){
		            		        	wait_doc_info.remove(fileName);
	        							continue;
		            		        }
		            		        
		            	        	JSONObject obj = new JSONObject();
		            	        	String doc_uuid = null;
		            	        	try {
		            					obj = array.getJSONObject(array.length()-2);
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            	        	try {
		            	        		doc_uuid = obj.getString("document");
		            	        		newest_doc_uuid = doc_uuid;
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            	        	
		            	        	
		            	        	JSONObject obj2 = new JSONObject();
		            	        	String new_version = null;
		            	        	try {
		            					obj2 = array.getJSONObject(array.length()-1);
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            	        	try {
		            	        		new_version = obj2.getString("version");
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            	        	
		            	        	File last_dahu_version_file = new File("/sdcard/TIBSinfo/version_" + version_check + ".txt");
				        			
				        			String last_dahu_version = null;
				        			
				        			if(last_dahu_version_file.exists()){
				        			
					        			try {
					        				FileReader fr = new FileReader(last_dahu_version_file);
					        				BufferedReader br = new BufferedReader(fr);
					        				last_dahu_version = br.readLine();
					        				br.close();
					        				fr.close();
					        				
					        			} catch (FileNotFoundException e) {
					        				// TODO Auto-generated catch block
					        				e.printStackTrace();
					        			} catch (IOException e) {
					        				// TODO Auto-generated catch block
					        				e.printStackTrace();
					        			}
					        			
				        			}else{
				        				last_dahu_version = "00.00.00.00";
				        			}
				        			
				        			int[] version = new int[4];
			            			int[] old_version = new int[4];
			            			
			            			boolean update = false;
			            			
			            			String[] vv = new_version.split("\\.");
	                		    	for(int j=0;j<vv.length;j++){
	                		    		version[j] = Integer.parseInt(vv[j]);
	                		    	}
	         
	                		    	if(last_dahu_version==null){
	        	    		    		last_dahu_version = "00.00.00.00";
	        	    		    	}
	        						String[] vvo = last_dahu_version.split("\\.");
	        						for(int j=0;j<vvo.length;j++){
	        	    		    		old_version[j] = Integer.parseInt(vvo[j]);
	        						}
	        						
	        						for(int j=0;j<4;j++){
			        		    		if(version[j]>old_version[j]){
			        		    			update = true;
			        		    			break;
			        		    		}
			        		    	}
	        						
	        						if(!update){
	        							wait_doc_info.remove(fileName);
	        							continue;
	        						}
		
		            	        //Make Tag
	            	        	String info_filepath = "/sdcard/haggle/info_" + fileTimeStamp + "--" + realFileName;
		                		String info_fileName = "info_" + fileTimeStamp + "--" + realFileName;

		            	       if(!startMakeTagInDevice(info_filepath, info_fileName))
		            	    	   wait_doc_info.remove(fileName);
		            	       else{ 	
		            	    	   
		            	    	   copyFile(filepath, "/sdcard/Share/" + timestamp + "--" + realFileName);
		            	    	   
		            	    	   copyFileToTIBS(filepath);
		            	        	
			            	        //Publish Document and Info to another device
			            	        String pub_filepath = timestamp + "--" + realFileName;
			            	        
			            	        publishDocToAnotherDevice(pub_filepath, realFileName, doc_uuid, timestamp);
			            	        
			            			
			            			if(realFileName.indexOf("dahu")!=-1){
			            				newest_dahu_time = timestamp;
			            			}else if(realFileName.indexOf("ntu")!=-1){
			            				newest_ntu_time = timestamp;
			            			}else{
			            			}
			            			
			            			try {
	        							FileWriter fw = new FileWriter("/sdcard/TIBSinfo/version_" + version_check + ".txt", false);
	        							BufferedWriter bw = new BufferedWriter(fw);
	        							bw.write(new_version);
	        							bw.flush();
	        							fw.flush();
	        							bw.close();
	        							fw.close();
	        						} catch (IOException e) {
	        							// TODO Auto-generated catch block
	        							e.printStackTrace();
	        						}

			            	        wait_doc_info.remove(fileName);
		            	       }
		            			
		            		}
		            		
		            		Log.d(DocShare.LOG_TAG, "Filepath=" + filepath);
		
		            		Log.d(DocShare.LOG_TAG, "Updating UI dobj");
		                	}
	                		
                		}
                		
                	}
                		
                }
                
                if(new_info_count>info_count){
                	
                	int get_info_count = new_info_count - info_count;
                	
                	Log.d("temp", "get document info!!!"  +  "count: " + (new_info_count-info_count));
                	info_count = new_info_count;
                	long newestinfofiletime = 0;
                	int newestinfofileindex = -1;
                	
                	ArrayList<Integer> had_add_index = new ArrayList<Integer>();
                	
                	for(int j=0;j<get_info_count;j++){
	                	for(int i=0;i<infoList.length;i++){
	                		if(!had_add_index.contains(i)){
		                		File info = infoList[i];
		                		long infofiletime = new Date(info.lastModified()).getTime();
		                		if(infofiletime>newestinfofiletime){
		                			newestinfofiletime = infofiletime;
		                			newestinfofileindex = i;
		                		}
	                		}
	                	}
	                	had_add_index.add(newestinfofileindex);
	                	newestinfofiletime = 0;
	                	newestinfofileindex = -1;
                	}
                	
                	if(!had_add_index.isEmpty()){
                		
                		for(int i=0;i<had_add_index.size();i++){
                		
                			String result = "";
                			
	                		String info_filepath = infoList[had_add_index.get(i)].getPath();
	                		String info_filename = infoList[had_add_index.get(i)].getName();
	                		
	                		String filename = info_filename.substring(5);
	                		String filepath = "/sdcard/haggle/" + filename;
	                		String realFileName = filename.split("--")[1];
	                		
	                		
	                		long fileTimeStamp = Long.parseLong(filename.split("--")[0]);
	                		
	                		long timestamp = System.currentTimeMillis();
	                		
	                		if(fileTimeStamp == timestamp){
	                			timestamp++;
	                		}
	                		
	                		if(!wait_info_doc.contains(filename)){
	                			wait_doc_info.add(filename);
	                		}else{      		
		            			
		            			try{
		            	        	FileReader fr = new FileReader(filepath);
		            	        	BufferedReader br = new BufferedReader(fr);
		            	        	String temp = br.readLine();
		            	        	while(temp != null){
		            	        		result += temp;
		            	        		temp = br.readLine();
		            	        	}
		            	        }catch(Exception e){
		            	        	e.printStackTrace();
		            	        }
		            	        
		            	        String gg = result;
		            	        JSONArray array = new JSONArray();
		            	        
		            	        if(gg.compareTo("")!=0){
		            	        
		            		        try {
		            					array = new JSONArray(gg);
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            		        
		            		        if(array.length()==11){
		            		        	wait_info_doc.remove(filename);
		            		        	continue;
		            		        }
		            		        
		            	        	JSONObject obj = new JSONObject();
		            	        	String doc_uuid = null;
		            	        	try {
		            					obj = array.getJSONObject(array.length()-2);
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            	        	try {
		            	        		doc_uuid = obj.getString("document");
		            	        		newest_doc_uuid = doc_uuid;
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            	        	
		            	        	JSONObject obj2 = new JSONObject();
		            	        	String new_version = null;
		            	        	try {
		            					obj2 = array.getJSONObject(array.length()-1);
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		            	        	try {
		            	        		new_version = obj2.getString("version");
		            				} catch (JSONException e) {
		            					// TODO Auto-generated catch block
		            					e.printStackTrace();
		            				}
		
		            	        //Make Tag
		            	        if(!startMakeTagInDevice(info_filepath, info_filename))
		            	        	wait_info_doc.remove(filename);
		            	        else{
		            	        	
		            	        	copyFile(filepath, "/sdcard/Share/" + timestamp + "--" + realFileName);
		            	        	
		            	        	copyFileToTIBS(filepath);
		            	        	
			            	        //Publish Document and Info to another device
			            	        String pub_filepath = timestamp + "--" + realFileName;
			            	        
			            	        publishDocToAnotherDevice(pub_filepath, realFileName, doc_uuid, timestamp);
			            	        
			            	        String version_check = null;
			            	        
			            	        if(realFileName.indexOf("dahu")!=-1){
			            				newest_dahu_time = timestamp;
			            				version_check = "dahu";
			            			}else if(realFileName.indexOf("ntu")!=-1){
			            				newest_ntu_time = timestamp;
			            				version_check = "ntu";
			            			}else{
			            			}
			            	        
			            	        try {
	        							FileWriter fw = new FileWriter("/sdcard/TIBSinfo/version_" + version_check + ".txt", false);
	        							BufferedWriter bw = new BufferedWriter(fw);
	        							bw.write(new_version);
	        							bw.flush();
	        							fw.flush();
	        							bw.close();
	        							fw.close();
	        						} catch (IOException e) {
	        							// TODO Auto-generated catch block
	        							e.printStackTrace();
	        						}
			            	        
			            	        wait_info_doc.remove(filename);
		            	        }
		            			
		            		}
     		
	                	}
	                		
                		}
                	
                	}
                	
                }
				
				
                //If receive BEACON, PUSH tag to POS
                if(new_beacon_count>beacon_count){
                	
                	int local_ip[] = new int[4];
            		
            		local_ip = getMyIp();
                	
                	beacon_count = new_beacon_count;
                	long newestfiletime = 0;
                	int newestfileindex = -1;
                	for(int i=0;i<beaconList.length;i++){
                		File beacon = beaconList[i];
                		long filetime = new Date(beacon.lastModified()).getTime();
                		if(filetime>newestfiletime){
                			newestfiletime = filetime;
                			newestfileindex = i;
                		}
                	}      				
                		
					File[] shareTagList = notPubTagFilePath.listFiles(tagfilter);
					
					Model model_union = ModelFactory.createDefaultModel();
					
					for(int i=0;i<shareTagList.length;i++){
						String filepath = shareTagList[i].getPath();
						
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
						DataObject tagObj = new DataObject();
						tagObj = new DataObject(new_tag_path);
						tagObj.addAttribute("RDF", "TAG", 1);
						
						pv.pushToPi(tagObj);
						
						//hh.publishDataObject(tagObj);
						Log.d("temp", "publish taglist");
					} catch (DataObjectException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					String have_to_pub_tag_list_filepath = "/sdcard/TIBSinfo/not_pub_tag.txt";
					
					new File(have_to_pub_tag_list_filepath).delete();
        				
                }
                
				//If receive tag from another device, add them to pub buffer
                if(new_tag_count>tag_count){
                	
                	int get_tag_count = new_tag_count - tag_count;
                	
                	tag_count = new_tag_count;
                	long newestfiletime = 0;
                	int newestfileindex = -1;
                	
                	ArrayList<Integer> had_add_index = new ArrayList<Integer>();
                	
                	for(int j=0;j<get_tag_count;j++){
	                	for(int i=0;i<tagList.length;i++){
	                		if(!had_add_index.contains(i)){
		                		File tag = tagList[i];
		                		long filetime = new Date(tag.lastModified()).getTime();
		                		if(filetime>newestfiletime){
		                			newestfiletime = filetime;
		                			newestfileindex = i;
		                		}
	                		}
	                	}
	                	had_add_index.add(newestfileindex);
	                	newestfiletime = 0;
	                	newestfileindex = -1;
                	}
                	if(!had_add_index.isEmpty()){
        				
                		for(int i=0;i<had_add_index.size();i++){
                		
	                		String filepath = tagList[had_add_index.get(i)].getPath();
	                		String filename = tagList[had_add_index.get(i)].getName();
	                		
							//Check the tag are from offlaod device
	                		if(wait_offload && wait_to_offload_tag_list.contains(filename)){
	                		
		                		beacon_have_to_send_tag_uuid_list.add(filepath);
		                		
								//Add it to buffer
		                		copyFile(filepath, "/sdcard/notPubTagDoc/" +  filename);
		        				
		        				try {
		        					FileWriter fw2 = new FileWriter("/sdcard/TIBSinfo/not_pub_tag.txt", true);
			        				BufferedWriter bw = new BufferedWriter(fw2);
									bw.write(filename);
									bw.newLine();
			        				bw.flush();
			        				bw.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		        				
		                		
	                		}else{
	                			in_haggle_tag_list.add(filename);
	                		}
	                		
                		}
                		
                	}
                }
                
                if(wait_offload){
                	if(wait_offload_start_time - System.currentTimeMillis() < -60000 ){		//offload time MAX : 1min
                		wait_offload = false;
                		wait_to_offload_tag_list.clear();
                	}
                }
                
                //Receive OFFLOAD information
                if(new_offload_count>offload_count){
                	
                	int get_offload_count = new_offload_count - offload_count;
                	
                	offload_count = new_offload_count;
                	long newestfiletime = 0;
                	int newestfileindex = -1;
                	
                	ArrayList<Integer> had_add_index = new ArrayList<Integer>();
                	
                	for(int j=0;j<get_offload_count;j++){
	                	for(int i=0;i<offloadList.length;i++){
	                		if(!had_add_index.contains(i)){
		                		File offload = offloadList[i];
		                		long filetime = new Date(offload.lastModified()).getTime();
		                		if(filetime>newestfiletime){
		                			newestfiletime = filetime;
		                			newestfileindex = i;
		                		}
	                		}
	                	}
	                	had_add_index.add(newestfileindex);
	                	newestfiletime = 0;
	                	newestfileindex = -1;
                	}
                	if(!had_add_index.isEmpty()){
                		
                		for(int i=0;i<had_add_index.size();i++){
                			
                			String filepath = offloadList[had_add_index.get(i)].getPath();
	                		String filename = offloadList[had_add_index.get(i)].getName();
	                		
	                		long offload_timestamp = Long.parseLong(filename.substring(8, 21));
	                		
	                		long now_timestamp = System.currentTimeMillis();
	                		
	                		if(now_timestamp - offload_timestamp < 60000){
	                			
	                			wait_offload = true;
	                			
	                			try{
		            	        	FileReader fr = new FileReader(filepath);
		            	        	BufferedReader br = new BufferedReader(fr);
		            	        	String temp = br.readLine();
		            	        	while(temp != null){
		            	        		wait_to_offload_tag_list.add(temp);
		            	        		Log.d("temp", "wait_to_offload_tag_list add: " + temp);
		            	        		temp = br.readLine();
		            	        	}
		            	        }catch(Exception e){
		            	        	e.printStackTrace();
		            	        }
	                			
	                			for(int j=0;j<in_haggle_tag_list.size();j++){
	                				if(wait_to_offload_tag_list.contains(in_haggle_tag_list.get(j))){
	                					
	                					copyFile("/sdcard/haggle/" +  in_haggle_tag_list.get(j), "/sdcard/notPubTagDoc/" +  in_haggle_tag_list.get(j));
	                				
	                				}
	                			}
	                			
	                		}
                			
                		}
                		
                	}
                		
                }
                
                
                
                
        	}
        	
        	return "";
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        	
        }
    }
	
	private boolean startMakeTagInDevice(String filepath, String fileName){
		
		try{
			FileReader fr = new FileReader(filepath);
			BufferedReader br = new BufferedReader(fr);
			
			String domain = br.readLine();
			String person = br.readLine();
			String deviceid = br.readLine();
			String idType = br.readLine();
			String idNumber = br.readLine();
			String deviceName = br.readLine();
			String doc_uuid = br.readLine();
			String deviceIdType = br.readLine();
			
			while(deviceIdType == null){
				FileReader fr2 = new FileReader(filepath);
				BufferedReader br2 = new BufferedReader(fr2);
				
				domain = br2.readLine();
				person = br2.readLine();
				deviceid = br2.readLine();
				idType = br2.readLine();
				idNumber = br2.readLine();
				deviceName = br2.readLine();
				doc_uuid = br2.readLine();
				deviceIdType = br2.readLine();
				
				br2.close();
				fr2.close();
			}
			
			
			br.close();
			fr.close();
			
			boolean in_doc = false;
			
			//Receive Same Document
			if(get_doc_uuid_list.contains(doc_uuid)){
				in_doc = true;
				return false;
			}
			
			if(!in_doc){
				
				get_doc_uuid_list.add(doc_uuid);
				
				String uuid = "" + UUID.randomUUID();
				
				File file2 = new File("/sdcard/notPubTagDoc/" +  "tag_" + uuid + ".rdf");
				
				if(file2.createNewFile())
					Log.d("temp", "create tag file");
				
				FileWriter fw = new FileWriter("/sdcard/notPubTagDoc/" +  "tag_" + uuid + ".rdf");
				
				fw.write(makeTag(uuid, doc_uuid, domain, person, deviceid, idType, idNumber, deviceName, deviceIdType));
				
				fw.flush();
				
				fw.close();
				
				FileWriter fw2 = new FileWriter("/sdcard/TIBSinfo/not_pub_tag.txt", true);
				BufferedWriter bw = new BufferedWriter(fw2);
				
				bw.write("tag_" + uuid + ".rdf");
				bw.newLine();
				bw.flush();
				bw.close();
			
			}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return true;
		
	}
	
	//Add this device information then publish
	private void publishDocToAnotherDevice(String takenFilepath, String realFileName, String doc_uuid, long timestamp){
		
		try {
			DataObject dObj2 = new DataObject();
			DataObject infoObj = new DataObject();
			
			String intrest = "";
			
			if(takenFilepath.indexOf("dahu")!=-1){
				intrest = "Refuge_dahu";
			}else if(takenFilepath.indexOf("ntu")!=-1){
				intrest = "Refuge_ntu";
			}else{
			}
			
			try{
				FileWriter fw = new FileWriter("/sdcard/Share/" + "info_" + timestamp + "--" + realFileName , false);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(MainActivity.domain_name);
				bw.newLine();
				bw.write(MainActivity.person_name);
				bw.newLine();
				bw.write(my_device_id);
				bw.newLine();
				bw.write(MainActivity.id_type);
				bw.newLine();
				bw.write(MainActivity.id_number);
				bw.newLine();
				bw.write(MainActivity.device_name);
				bw.newLine();
				bw.write(doc_uuid);
				bw.newLine();
				bw.write("Mac address");
				bw.newLine();
				bw.flush();
				fw.flush();
				bw.close();
				fw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			copyFile("/sdcard/haggle/" + takenFilepath, "/sdcard/Share/" + timestamp + "--" + realFileName);
		
			dObj2 = new DataObject("/sdcard/Share/" + timestamp + "--" + realFileName);
			infoObj = new DataObject("/sdcard/Share/" + "info_" + timestamp + "--" + realFileName);

			Log.d(DocShare.LOG_TAG, "Picture has attribute " +  timestamp + "--" + realFileName);
			dObj2.addAttribute("JSON", intrest, 1);
			infoObj.addAttribute("JSON", intrest, 1);
			
			pv.pushToDevice(dObj2);
			pv.pushToDevice(infoObj);
					
			ArrayList<Attribute> aa = new ArrayList<Attribute>();


		}catch (DataObjectException e) {
			// TODO Auto-generated catch block
			Log.d(DocShare.LOG_TAG, "Could not create data object for "
					+ takenFilepath);
		}
		
	}
	
}
