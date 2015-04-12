package tw.edu.nctu.pet.madreader;

import java.security.NoSuchAlgorithmException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class AESEncrypter {

	private SecretKeySpec skeySpec;
	 private Cipher cipher;
	  
	 public AESEncrypter() throws NoSuchAlgorithmException, NoSuchPaddingException{
	    // Get the KeyGenerator
	    KeyGenerator kgen = KeyGenerator.getInstance("AES");
	    kgen.init(128); // 192 and 256 bits may not be available
	  
	  
	    // Generate the secret key specs.
	    SecretKey skey = kgen.generateKey();
	    byte[] raw = skey.getEncoded();
	    this.skeySpec = new SecretKeySpec(raw, "AES");
	  
	  
	    // Instantiate the cipher
	    this.cipher = Cipher.getInstance("AES");
	 }
	  
	 // output encrypted bytes
	 public  byte[] encrypt( byte[] original ) throws Exception{
		 
		 //Log.d("temp", "from: " + original);
	    
	    this.getCipher().init(Cipher.ENCRYPT_MODE, skeySpec);
	    byte[] encrypted = cipher.doFinal(original);
	    
	    //Log.d("temp", "to: " + encrypted);
	     
	    return encrypted;
	 }
	 
	 // output decrypted bytes
	 public  byte[] decrypt( byte[] original ) throws Exception{
	     
		 Log.d("temp", "de1:" + new String(original)); 
		 
	    this.getCipher().init(Cipher.DECRYPT_MODE, skeySpec);
	    Log.d("temp", "de1.5"); 
	    byte[] decrypted = cipher.doFinal(original);
	    
	    Log.d("temp", "de2:" + new String(decrypted));
	     
	    return decrypted;
	 }
	 
	 // getter and setter
	 public SecretKeySpec getSkeySpec() {
	  return skeySpec;
	 }
	 
	 public void setSkeySpec(SecretKeySpec skeySpec) {
	  this.skeySpec = skeySpec;
	 }
	 
	 public Cipher getCipher() {
	  return cipher;
	 }
	 
	 public void setCipher(Cipher cipher) {
	  this.cipher = cipher;
	 }
	
}
