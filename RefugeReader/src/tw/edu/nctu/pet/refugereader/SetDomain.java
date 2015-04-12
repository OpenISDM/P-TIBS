package tw.edu.nctu.pet.madreader;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class SetDomain extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.set_domain_view);
        
        Button button = (Button)findViewById(R.id.set_domain_ok_button);
        button.setOnClickListener(setDomain);
	}
	
	private OnClickListener setDomain = new OnClickListener() {
		public void onClick(View v){
			
			AESEncrypter aese = MainActivity.getAESE();
			
			EditText edittext = (EditText) findViewById(R.id.domainname);
			String domain = edittext.getText().toString();
			FileWriter fw = null;
			try {
				fw = new FileWriter("/sdcard/TIBSinfo/domain.txt", false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedWriter bw = new BufferedWriter(fw);
			try {
				byte[] tmp = domain.getBytes();
				//byte[] tmp = aese.encrypt(domain.getBytes());
				FileOutputStream fos = new FileOutputStream("/sdcard/TIBSinfo/domain.txt");
				fos.write(tmp);
				fos.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*try {
				byte[] tmp = aese.encrypt(domain.getBytes());
				bw.write(new String(tmp));
				
				Log.d("temp", new String(aese.decrypt(tmp)));
				Log.d("temp", new String(aese.decrypt(new String(tmp).getBytes())));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//====================================================
			
			EditText edittext2 = (EditText) findViewById(R.id.username);
			String person = edittext2.getText().toString();
			FileWriter fw2 = null;
			try {
				fw2 = new FileWriter("/sdcard/TIBSinfo/person.txt", false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedWriter bw2 = new BufferedWriter(fw2);
			try {
				byte[] tmp = person.getBytes();
				//byte[] tmp = aese.encrypt(person.getBytes());
				FileOutputStream fos = new FileOutputStream("/sdcard/TIBSinfo/person.txt");
				fos.write(tmp);
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
				bw2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//====================================================
			
			EditText edittext3 = (EditText) findViewById(R.id.idType);
			String id_type = edittext3.getText().toString();
			FileWriter fw3 = null;
			try {
				fw3 = new FileWriter("/sdcard/TIBSinfo/idType.txt", false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedWriter bw3 = new BufferedWriter(fw3);
			try {
				byte[] tmp = id_type.getBytes();
				//byte[] tmp = aese.encrypt(id_type.getBytes());
				FileOutputStream fos = new FileOutputStream("/sdcard/TIBSinfo/idType.txt");
				fos.write(tmp);
				fos.close();
				//bw.write(tmp.toString());
				//bw3.write(id_type);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				bw3.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//====================================================
			
			EditText edittext4 = (EditText) findViewById(R.id.idNumber);
			String id_number = edittext4.getText().toString();
			FileWriter fw4 = null;
			try {
				fw4 = new FileWriter("/sdcard/TIBSinfo/idNumber.txt", false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedWriter bw4 = new BufferedWriter(fw4);
			try {
				byte[] tmp = id_number.getBytes();
				//byte[] tmp = aese.encrypt(id_number.getBytes());
				FileOutputStream fos = new FileOutputStream("/sdcard/TIBSinfo/idNumber.txt");
				fos.write(tmp);
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
				bw4.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//====================================================
			
			EditText edittext5 = (EditText) findViewById(R.id.deviceName);
			String device_name = edittext5.getText().toString();
			FileWriter fw5 = null;
			try {
				fw5 = new FileWriter("/sdcard/TIBSinfo/deviceName.txt", false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedWriter bw5 = new BufferedWriter(fw5);
			try {
				byte[] tmp = device_name.getBytes();
				//byte[] tmp = aese.encrypt(device_name.getBytes());
				FileOutputStream fos = new FileOutputStream("/sdcard/TIBSinfo/deviceName.txt");
				fos.write(tmp);
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
				bw5.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	};
}
