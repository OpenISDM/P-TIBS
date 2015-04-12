package tw.edu.nctu.pet.madreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import org.haggle.Attribute;

import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity{
	
	public static AESEncrypter aese = null;
	
	public static String domain_name = "";
	public static String person_name = "";
	public static String id_type = "";
	public static String id_number = "";
	public static String device_name = "";
	
	private  PhotoShare ps = null;
	private boolean shouldRegisterWithHaggle = true;
	
	public static AESEncrypter getAESE(){
		return aese;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, MainView.class);
        startActivity(intent);
        
        try {
			MainActivity.aese = new AESEncrypter();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        File file = new File(Environment.getExternalStorageDirectory() + "/Share");
        if(!file.exists()){
				file.mkdir();
        }
        
        File file2 = new File(Environment.getExternalStorageDirectory() + "/TIBSinfo");
        if(!file2.exists()){
			file2.mkdir();
        }
        
        Button button3 = (Button)findViewById(R.id.by_self);
        Button button5 = (Button)findViewById(R.id.set_domain_button);
        button3.setOnClickListener(getDataBySelf);
        button5.setOnClickListener(setDomain);
    }
    
    @Override
    public void onResume() {
    	
    	super.onResume();
    	
    	TextView textview = (TextView)findViewById(R.id.show_domain);
    	
    	try {
    		File file = new File("/sdcard/TIBSinfo/domain.txt");
			FileInputStream fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int)file.length()];
			fin.read(fileContent);
			byte[] tmp = fileContent;
			domain_name = new String(tmp);
			
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
    
    private Button.OnClickListener getDataBySelf = new Button.OnClickListener()
    {
          public void onClick(View v)
          {
        	  Intent intent = new Intent();
        	  intent.setClass(MainActivity.this, GetDataBySelf.class);
        	  startActivity(intent);
        	  
          }
    };
    
    private Button.OnClickListener setDomain = new Button.OnClickListener()
    {
    	
    	public void onClick(View v)
        {
      	  Intent intent = new Intent();
      	  intent.setClass(MainActivity.this, SetDomain.class);
      	  startActivity(intent);
      	  
        }
    };
    
}