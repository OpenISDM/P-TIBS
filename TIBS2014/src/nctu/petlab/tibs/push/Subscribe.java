package nctu.petlab.tibs.push;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Subscribe {

	String hubUrl = "";
	String mode = "";
	String verify = "";
	String topic = "";
	String callback = "";
	String msg = "";
	int retCode = -1;
	
	public Subscribe(String hubUrl, String mode, String verify, String topic,
			String callback, String method) {
		super();
		this.hubUrl = hubUrl;
		this.mode = mode;
		this.verify = verify;
		this.topic = topic;
		this.callback = callback;
	}
	
	
	
	public String getHubUrl() {
		return hubUrl;
	}



	public void setHubUrl(String hubUrl) {
		this.hubUrl = hubUrl;
	}



	public String getMode() {
		return mode;
	}



	public void setMode(String mode) {
		this.mode = mode;
	}



	public String getVerify() {
		return verify;
	}



	public void setVerify(String verify) {
		this.verify = verify;
	}



	public String getTopic() {
		return topic;
	}



	public void setTopic(String topic) {
		this.topic = topic;
	}



	public String getCallback() {
		return callback;
	}



	public void setCallback(String callback) {
		this.callback = callback;
	}



	public String getMsg() {
		return msg;
	}



	public void setMsg(String msg) {
		this.msg = msg;
	}



	public int getRetCode() {
		return retCode;
	}



	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}



	public int subscribe() throws NumberFormatException, IOException{
		String[] urlComp = genUrl(hubUrl);
		if(urlComp!=null){
			System.out.println("hubUrl : protocol = "+urlComp[0]+", host = "+urlComp[2]+", port = "+urlComp[1]+", path = "+urlComp[3]);
			URL url = new URL(urlComp[0],urlComp[2],Integer.parseInt(urlComp[1]),urlComp[3]);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			String params = "hub.mode="+(mode.equals("subscribe")?"subscribe":mode.equals("unsubscribe")?"unsubscribe":"")+
							"&hub.verify="+(verify.equals("sync")?"sync":verify.equals("async")?"async":"")+"&hub.topic="+
							URLEncoder.encode(topic,"UTF-8")+"&hub.callback="+URLEncoder.encode(callback,"UTF-8");
			System.out.println("params = "+params);
			OutputStream os = conn.getOutputStream();
			os.write(params.getBytes("UTF-8"));
			retCode = conn.getResponseCode();
			if(retCode==HttpURLConnection.HTTP_OK){
				InputStream is = conn.getInputStream();
				BufferedReader buf = new BufferedReader(new InputStreamReader(is,"UTF-8"));
				String tmp;
				StringBuilder sb = new StringBuilder();
				while((tmp=buf.readLine())!=null){
					sb.append(tmp);
				}
				is.close();
				msg = sb.toString();
			}
		}
		return retCode;
	}
	
	/**
	 * genUrl
	 * @param urlStr
	 * @return [protocol,port,host,path]
	 */
	private String[] genUrl(String urlStr){
		String[] ret = new String[4];
		if(urlStr.startsWith("https://")){
			ret[0] = "https";
			ret[1] = "443";
			if(urlStr.replaceFirst("https://", "").split("/")[0].contains(":")){
				ret[1] = String.valueOf(Integer.parseInt(urlStr.replaceFirst("https://", "").split("/")[0].split(":")[1]));
				ret[2] = urlStr.replaceFirst("https://", "").split("/")[0].split(":")[0];
				ret[3] = urlStr.replaceFirst("https://"+ret[2]+":"+ret[1], "");
			}else{
				ret[2] = urlStr.replaceFirst("https://", "").split("/")[0];
				ret[3] = urlStr.replaceFirst("https://"+ret[2], "");
			}
		}else if(urlStr.startsWith("http://")){
			ret[0] = "http";
			ret[1] = "80";
			if(urlStr.replaceFirst("http://", "").split("/")[0].contains(":")){
				ret[1] = String.valueOf(Integer.parseInt(urlStr.replaceFirst("http://", "").split("/")[0].split(":")[1]));
				ret[2] = urlStr.replaceFirst("http://", "").split("/")[0].split(":")[0];
				ret[3] = urlStr.replaceFirst("http://"+ret[2]+":"+ret[1], "");
			}else{
				ret[2] = urlStr.replaceFirst("http://", "").split("/")[0];
				ret[3] = urlStr.replaceFirst("http://"+ret[2], "");
			}
		}else{
			ret = null;
		}
		return ret;
	}
	
	
	
}
