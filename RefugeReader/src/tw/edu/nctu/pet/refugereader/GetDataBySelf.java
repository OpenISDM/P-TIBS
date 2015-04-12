package tw.edu.nctu.pet.madreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.haggle.DataObject;
import org.haggle.DataObject.DataObjectException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.edu.nctu.pet.madreader.PhotoView.myRunnable;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class GetDataBySelf extends Activity {
	
	private TextView view_phone;
	private TextView view_longitude;
	private TextView view_latitude;
	private TextView view_address;
	
	private TextView view_phone_near;
	private TextView view_longitude_near;
	private TextView view_latitude_near;
	private TextView view_address_near;
	
	private Spinner spinner;
	private Spinner spinner2;
	
	/*private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> adapter2;*/
	
	private String itemname[] = new String[10];
	private String itemphone[] = new String[10];
	private double itemlong[] = new double[10];
	private double itemlat[] = new double[10];
	private String itemaddr[] = new String[10];
	
	private String itemname2[] = new String[10];
	private String itemphone2[] = new String[10];
	private double itemlong2[] = new double[10];
	private double itemlat2[] = new double[10];
	private String itemaddr2[] = new String[10];
	
	
	
	String gg = "";
	
	private Handler myHandler01 = new Handler();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("jena", "5566");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list3);
		
		findViews();
		
		new Thread(){
            int count=0;
            public void run() {
              while(true){
            	  
            	String itemname[] = new String[10];
          		final String itemphone[] = new String[10];
          		final double itemlong[] = new double[10];
          		final double itemlat[] = new double[10];
          		final String itemaddr[] = new String[10];
          		
          		String gg = "";
                  
                  String result = "";
                  
                  try{
                  	FileReader fr = new FileReader("/sdcard/TIBS/Refuge_dahu.json");
                  	BufferedReader br = new BufferedReader(fr);
                  	String temp = br.readLine();
                  	while(temp != null){
                  		result += temp;
                  		temp = br.readLine();
                  	}
                  }catch(Exception e){
                  	e.printStackTrace();
                  }
                  
                  gg = result;
                  //Log.d("jena", "" + gg);
                  JSONArray array = new JSONArray();
                  
                  if(gg.compareTo("")!=0){
                  
          	        try {
          				array = new JSONArray(gg);
          			} catch (JSONException e) {
          				// TODO Auto-generated catch block
          				e.printStackTrace();
          			}
          	        for(int i=0; i<array.length()-1; i++){
          	        	JSONObject obj = new JSONObject();
          	        	try {
          					obj = array.getJSONObject(i);
          				} catch (JSONException e) {
          					// TODO Auto-generated catch block
          					e.printStackTrace();
          				}
          	        	try {
          					itemname[i] = obj.getString("name");
          					itemphone[i] = obj.getString("phone");
          	        		itemlong[i] = obj.getDouble("lon");
          	        		itemlat[i] = obj.getDouble("lat");
          	        		itemaddr[i] = obj.getString("str_addr");
          				} catch (JSONException e) {
          					// TODO Auto-generated catch block
          					e.printStackTrace();
          				}
          	    		
          	        }
          	        
                  }
                  
                  
                  
                  
                  String itemname2[] = new String[10];
          		final String itemphone2[] = new String[10];
          		final double itemlong2[] = new double[10];
          		final double itemlat2[] = new double[10];
          		final String itemaddr2[] = new String[10];
          		
          		String gg2 = "";
                  
                  String result2 = "";
                  
                  try{
                  	FileReader fr = new FileReader("/sdcard/TIBS/Refuge_ntu.json");
                  	BufferedReader br = new BufferedReader(fr);
                  	String temp = br.readLine();
                  	while(temp != null){
                  		result2 += temp;
                  		temp = br.readLine();
                  	}
                  }catch(Exception e){
                  	e.printStackTrace();
                  }
                  
                  gg2 = result2;
                  //Log.d("jena", "" + gg2);
                  JSONArray array2 = new JSONArray();
                  
                  if(gg2.compareTo("")!=0){
                  
          	        try {
          				array2 = new JSONArray(gg2);
          			} catch (JSONException e) {
          				// TODO Auto-generated catch block
          				e.printStackTrace();
          			}
          	        for(int i=0; i<array2.length()-1; i++){
          	        	JSONObject obj = new JSONObject();
          	        	try {
          					obj = array2.getJSONObject(i);
          				} catch (JSONException e) {
          					// TODO Auto-generated catch block
          					e.printStackTrace();
          				}
          	        	try {
          					itemname2[i] = obj.getString("name");
          					itemphone2[i] = obj.getString("phone");
          	        		itemlong2[i] = obj.getDouble("lon");
          	        		itemlat2[i] = obj.getDouble("lat");
          	        		itemaddr2[i] = obj.getString("str_addr");
          				} catch (JSONException e) {
          					// TODO Auto-generated catch block
          					e.printStackTrace();
          				}
          	    		
          	        }
                  
                  }
                  
                  
                  if(itemname[0]==null){
  	          		for(int i=0;i<10;i++){
  	          			itemname[i] = "";
  	          		}
  	          	}
  	          	
  	          	if(itemname2[0]==null){
  	          		for(int i=0;i<10;i++){
  	          			itemname2[i] = "";
  	          		}
  	          	}
 
                myHandler01.post(new myRunnable(itemname, itemphone,itemlong,itemlat, itemaddr, itemname2, itemphone2,itemlong2,itemlat2, itemaddr2));
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            }}.start();
		
		
		
		
		/*makeMADList();
		makeMADNearList();
		
		new TreatReceive().execute("");*/
			
        
	}
	
	public class myRunnable implements Runnable {
		String itemname[] = new String[10];
  		String itemphone[] = new String[10];
  		double itemlong[] = new double[10];
  		double itemlat[] = new double[10];
  		String itemaddr[] = new String[10];
  		
  		String itemname2[] = new String[10];
  		String itemphone2[] = new String[10];
  		double itemlong2[] = new double[10];
  		double itemlat2[] = new double[10];
  		String itemaddr2[] = new String[10];
        
        public myRunnable(String[] i11, String[] i12, double[] i13, double[] i14, String[] i15, String[] i21, String[] i22, double[] i23, double[] i24, String[] i25) {
        	this.itemname = i11;
    		this.itemphone = i12;
      		this.itemlong = i13;
      		this.itemlat = i14;
    		this.itemaddr = i15;
    		
    		this.itemname2 = i21;
    		this.itemphone2 = i22;
      		this.itemlong2 = i23;
      		this.itemlat2 = i24;
    		this.itemaddr2 = i25;
    	
        }
        
        @Override
        public void run() {
          // TODO Auto-generated method stub
        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(GetDataBySelf.this,android.R.layout.simple_spinner_item,itemname);
        	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	adapter.notifyDataSetChanged();
	        spinner.setAdapter(adapter);
	        
	        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
	        	/*@Override
	        	public void onItemSelected(AdapterView adapterView, View view, int position, long id){
	        		Toast.makeText(RDFBrowser.this, "您選擇"+adapterView.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
	        	}*/
	
				@Override
				public void onItemSelected(AdapterView arg0, View arg1,
						int arg2, long arg3) {
					
					if(itemname[0]!=""){
					
						// TODO Auto-generated method stub
						DecimalFormat nf = new DecimalFormat("0.0000");
						if(itemphone[arg2].compareTo("0000")==0){
							view_phone.setText("電話未提供");
						}else{
							view_phone.setText("電話: " + itemphone[arg2]);
						}
						view_longitude.setText("經度: " + nf.format(itemlong[arg2]));
						view_latitude.setText("緯度: " + nf.format(itemlat[arg2]));
						view_address.setText("地址: " + itemaddr[arg2]);
						
					}
					
				}
	
				@Override
				public void onNothingSelected(AdapterView arg0) {
					// TODO Auto-generated method stub
					Log.d("jena", "non-click");
				}
	        });
        	
        	
        	
        	
        	ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(GetDataBySelf.this,android.R.layout.simple_spinner_item,itemname2);
        	adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	adapter2.notifyDataSetChanged();
 	        spinner2.setAdapter(adapter2);
 	        
 	        
 	        spinner2.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
 	        	/*@Override
 	        	public void onItemSelected(AdapterView adapterView, View view, int position, long id){
 	        		Toast.makeText(RDFBrowser.this, "您選擇"+adapterView.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
 	        	}*/
 	
 				@Override
 				public void onItemSelected(AdapterView arg0, View arg1,
 						int arg2, long arg3) {
 					
 					if(itemname2[0]!=""){
 						
	 					// TODO Auto-generated method stub
	 					DecimalFormat nf = new DecimalFormat("0.0000");
	 					if(itemphone2[arg2].compareTo("0000")==0){
	 						view_phone_near.setText("電話未提供");
	 					}else{
	 						view_phone_near.setText("電話: " + itemphone2[arg2]);
	 					}
	 					view_longitude_near.setText("經度: " + nf.format(itemlong2[arg2]));
	 					view_latitude_near.setText("緯度: " + nf.format(itemlat2[arg2]));
	 					view_address_near.setText("地址: " + itemaddr2[arg2]);
	 					
 					}
 					
 				}
 	
 				@Override
 				public void onNothingSelected(AdapterView arg0) {
 					// TODO Auto-generated method stub
 					Log.d("jena", "non-click");
 				}
 	        });
        	
        }
        
      }
	
	
	
	
	private void findViews(){
		view_phone = (TextView)findViewById(R.id.phone3);
		view_longitude = (TextView)findViewById(R.id.longitude3);
		view_latitude = (TextView)findViewById(R.id.latitude3);
		view_address = (TextView)findViewById(R.id.address3);
		view_phone_near = (TextView)findViewById(R.id.phone_near3);
		view_longitude_near = (TextView)findViewById(R.id.longitude_near3);
		view_latitude_near = (TextView)findViewById(R.id.latitude_near3);
		view_address_near = (TextView)findViewById(R.id.address_near3);
		
		spinner = (Spinner) findViewById(R.id.spinner3);
		spinner2 = (Spinner) findViewById(R.id.spinner_near3);
		
		/*adapter = new ArrayAdapter<String>(GetDataBySelf.this,android.R.layout.simple_spinner_item,itemname);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	adapter2 = new ArrayAdapter<String>(GetDataBySelf.this,android.R.layout.simple_spinner_item,itemname2);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/
	}	
	
	private void makeMADList() {
		String itemname[] = new String[10];
		final String itemphone[] = new String[10];
		final double itemlong[] = new double[10];
		final double itemlat[] = new double[10];
		final String itemaddr[] = new String[10];
		
		String gg = "";
		
		//Spinner spinner = (Spinner) findViewById(R.id.spinner3);
        
        ArrayAdapter<String> adapter = null;
        
        String result = "";
        
        try{
        	FileReader fr = new FileReader("/sdcard/TIBS/Refuge_dahu.json");
        	BufferedReader br = new BufferedReader(fr);
        	String temp = br.readLine();
        	while(temp != null){
        		result += temp;
        		temp = br.readLine();
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        gg = result;
        Log.d("jena", "" + gg);
        JSONArray array = new JSONArray();
        
        if(gg.compareTo("")!=0){
        
	        try {
				array = new JSONArray(gg);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        for(int i=0; i<array.length()-1; i++){
	        	JSONObject obj = new JSONObject();
	        	try {
					obj = array.getJSONObject(i);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	try {
					itemname[i] = obj.getString("name");
					itemphone[i] = obj.getString("phone");
	        		itemlong[i] = obj.getDouble("lon");
	        		itemlat[i] = obj.getDouble("lat");
	        		itemaddr[i] = obj.getString("str_addr");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	        }
	    		
			adapter = new ArrayAdapter<String>(GetDataBySelf.this,android.R.layout.simple_spinner_item,itemname);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinner.setAdapter(adapter);
	        
	        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
	        	/*@Override
	        	public void onItemSelected(AdapterView adapterView, View view, int position, long id){
	        		Toast.makeText(RDFBrowser.this, "您選擇"+adapterView.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
	        	}*/
	
				@Override
				public void onItemSelected(AdapterView arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					DecimalFormat nf = new DecimalFormat("0.0000");
					if(itemphone[arg2].compareTo("0000")==0){
						view_phone.setText("電話未提供");
					}else{
						view_phone.setText("電話: " + itemphone[arg2]);
					}
					view_longitude.setText("經度: " + nf.format(itemlong[arg2]));
					view_latitude.setText("緯度: " + nf.format(itemlat[arg2]));
					view_address.setText("地址: " + itemaddr[arg2]);
					
				}
	
				@Override
				public void onNothingSelected(AdapterView arg0) {
					// TODO Auto-generated method stub
					Log.d("jena", "non-click");
				}
	        });
	        
		}
    }
	
	
	private void makeMADNearList() {
		String itemname[] = new String[10];
		final String itemphone[] = new String[10];
		final double itemlong[] = new double[10];
		final double itemlat[] = new double[10];
		final String itemaddr[] = new String[10];
		
		String gg = "";
		
		//Spinner spinner = (Spinner) findViewById(R.id.spinner_near3);
        
        ArrayAdapter<String> adapter = null;
        
        String result = "";
        
        try{
        	FileReader fr = new FileReader("/sdcard/TIBS/Refuge_ntu.json");
        	BufferedReader br = new BufferedReader(fr);
        	String temp = br.readLine();
        	while(temp != null){
        		result += temp;
        		temp = br.readLine();
        	}
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        gg = result;
        Log.d("jena", "" + gg);
        JSONArray array = new JSONArray();
        
        if(gg.compareTo("")!=0){
        
	        try {
				array = new JSONArray(gg);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        for(int i=0; i<array.length()-1; i++){
	        	JSONObject obj = new JSONObject();
	        	try {
					obj = array.getJSONObject(i);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	try {
					itemname[i] = obj.getString("name");
					itemphone[i] = obj.getString("phone");
	        		itemlong[i] = obj.getDouble("lon");
	        		itemlat[i] = obj.getDouble("lat");
	        		itemaddr[i] = obj.getString("str_addr");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	        }
	    		
			adapter = new ArrayAdapter<String>(GetDataBySelf.this,android.R.layout.simple_spinner_item,itemname);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinner2.setAdapter(adapter);
	        
	        spinner2.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
	        	/*@Override
	        	public void onItemSelected(AdapterView adapterView, View view, int position, long id){
	        		Toast.makeText(RDFBrowser.this, "您選擇"+adapterView.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
	        	}*/
	
				@Override
				public void onItemSelected(AdapterView arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					DecimalFormat nf = new DecimalFormat("0.0000");
					if(itemphone[arg2].compareTo("0000")==0){
						view_phone_near.setText("電話未提供");
					}else{
						view_phone_near.setText("電話: " + itemphone[arg2]);
					}
					view_longitude_near.setText("經度: " + nf.format(itemlong[arg2]));
					view_latitude_near.setText("緯度: " + nf.format(itemlat[arg2]));
					view_address_near.setText("地址: " + itemaddr[arg2]);
					
				}
	
				@Override
				public void onNothingSelected(AdapterView arg0) {
					// TODO Auto-generated method stub
					Log.d("jena", "non-click");
				}
	        });
	        
        }
    }
	
private class TreatReceive extends AsyncTask<String, Void, String[]> {
		
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
		}
		
        @Override
        protected String[] doInBackground(String... urls) {
        	
        	while(true){
      		
	      		String gg = "";
	              
	              String result = "";
	              
	              try{
	              	FileReader fr = new FileReader("/sdcard/TIBS/Refuge_dahu.json");
	              	BufferedReader br = new BufferedReader(fr);
	              	String temp = br.readLine();
	              	while(temp != null){
	              		result += temp;
	              		temp = br.readLine();
	              	}
	              }catch(Exception e){
	              	e.printStackTrace();
	              }
	              
	              gg = result;
	              Log.d("jena", "" + gg);
	              JSONArray array = new JSONArray();
	              
	              if(gg.compareTo("")!=0){
	              
	      	        try {
	      				array = new JSONArray(gg);
	      			} catch (JSONException e) {
	      				// TODO Auto-generated catch block
	      				e.printStackTrace();
	      			}
	      	        for(int i=0; i<array.length()-1; i++){
	      	        	JSONObject obj = new JSONObject();
	      	        	try {
	      					obj = array.getJSONObject(i);
	      				} catch (JSONException e) {
	      					// TODO Auto-generated catch block
	      					e.printStackTrace();
	      				}
	      	        	try {
	      					itemname[i] = obj.getString("name");
	      					itemphone[i] = obj.getString("phone");
	      	        		itemlong[i] = obj.getDouble("lon");
	      	        		itemlat[i] = obj.getDouble("lat");
	      	        		itemaddr[i] = obj.getString("str_addr");
	      				} catch (JSONException e) {
	      					// TODO Auto-generated catch block
	      					e.printStackTrace();
	      				}
	      	    		
	      	        }
	      	        
	              }
	              
	              String gg2 = "";
	              
	              String result2 = "";
	              
	              try{
	              	FileReader fr = new FileReader("/sdcard/TIBS/Refuge_ntu.json");
	              	BufferedReader br = new BufferedReader(fr);
	              	String temp = br.readLine();
	              	while(temp != null){
	              		result2 += temp;
	              		temp = br.readLine();
	              	}
	              }catch(Exception e){
	              	e.printStackTrace();
	              }
	              
	              gg2 = result2;
	              Log.d("jena", "" + gg2);
	              JSONArray array2 = new JSONArray();
	              
	              if(gg2.compareTo("")!=0){
	              
	      	        try {
	      				array2 = new JSONArray(gg2);
	      			} catch (JSONException e) {
	      				// TODO Auto-generated catch block
	      				e.printStackTrace();
	      			}
	      	        for(int i=0; i<array2.length()-1; i++){
	      	        	JSONObject obj = new JSONObject();
	      	        	try {
	      					obj = array2.getJSONObject(i);
	      				} catch (JSONException e) {
	      					// TODO Auto-generated catch block
	      					e.printStackTrace();
	      				}
	      	        	try {
	      					itemname2[i] = obj.getString("name");
	      					itemphone2[i] = obj.getString("phone");
	      	        		itemlong2[i] = obj.getDouble("lon");
	      	        		itemlat2[i] = obj.getDouble("lat");
	      	        		itemaddr2[i] = obj.getString("str_addr");
	      				} catch (JSONException e) {
	      					// TODO Auto-generated catch block
	      					e.printStackTrace();
	      				}
	      	    		
	      	        }
	      	        
	              }
	              
	              Log.d("jena", "inp0: " + itemname[0]);
	              
	              
	              Log.d("jena", "inp: " + itemname[0]);
	          	if(itemname[0]==null){
	          		for(int i=0;i<10;i++){
	          			itemname[i] = "";
	          		}
	          	}
	          	
	          	if(itemname2[0]==null){
	          		for(int i=0;i<10;i++){
	          			itemname2[i] = "";
	          		}
	          	}
	              
	              
        	}
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String[] result) {
        	Log.d("jena", "inp: " + result[0]);
        	if(result[0]==null){
        		for(int i=0;i<10;i++){
        			result[i] = "";
        		}
        	}
        	
        	if(itemname2[0]==null){
        		for(int i=0;i<10;i++){
        			itemname2[i] = "";
        		}
        	}
        	
        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(GetDataBySelf.this,android.R.layout.simple_spinner_item,result);
        	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	//adapter.notifyDataSetChanged();
	        spinner.setAdapter(adapter);
	        
	        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(GetDataBySelf.this,android.R.layout.simple_spinner_item,itemname2);
        	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	//adapter.notifyDataSetChanged();
	        spinner.setAdapter(adapter);
 	        
        }
    }
	
}
