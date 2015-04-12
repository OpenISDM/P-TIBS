package nctu.petlab.tibs.json;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.google.gson.Gson;

@Path("jsonparse/")
public class JsonParse {

	@GET
	@Path("getData")
	public void getData(){
		
		System.out.println("start getting data");
		
		URL url = null;
		HttpURLConnection conn = null;
		
		try{
			url = new URL("http","140.109.21.188",80,"/facilities.json");
			conn = (HttpURLConnection)url.openConnection();
		
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			if(conn.getResponseCode()==200){
		
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String result = "";
				String tmp = "";
				while((tmp=reader.readLine())!=null){
					result += tmp;
				}
				//System.out.println("result = "+result);
				Gson gson = new Gson();
				Bean[] beans = gson.fromJson(result, Bean[].class);
				for(Bean bean:beans){
					//save to DB 
					System.out.println(bean.getAddr());
				}
			
				reader.close();
			
			}else{
				System.out.println("Error code received: "+conn.getResponseCode());
			}

			conn.disconnect();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
}
