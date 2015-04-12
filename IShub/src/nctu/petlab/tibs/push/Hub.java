package nctu.petlab.tibs.push;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import net.spy.memcached.MemcachedClient;


@Path("hub/")
public class Hub {

	int mErrCode = 0;
	String mErrMsg = "";
	String SERVER_IP = "localhost";
	String SUB_LIST_NAME = "subList";
	
	@Context private UriInfo uriInfo;
	@Context private javax.servlet.http.HttpServletRequest hsr;
	
	@GET
	@Path("")
	public Response showIndex(){
		System.out.println("showIndex...");
		String content = "<html><head><title></title></head><body>PET Lab - PubSubHubBub<p>"+
						"<li><a href='subscribe'>Subscribe</a><li><a href='publish'>Publish</a>"+
						"</body></html>";
		ResponseBuilder builder = Response.ok(content);
		return builder.build();
	}
	
	@GET
	@Path("publish")
	public Response showPubPage(){
		System.out.println("showPubPage...");
		String content = "<html><head><title></title></head><body>PET Lab - Publish<p>"+
						"<form method='POST' action='publish'><table><tr><td>topic</td>"+
						"<td><input type='text' name='topic'/></td></tr>"+
						"<tr><td colspan=2><input type='submit' value='submit'/></td></tr></table></form>"+
						"</body></html>";
		ResponseBuilder builder = Response.ok(content);
		return builder.build();
	}
	
	@POST
	@Path("publish")
	public Response subscribe(@FormParam("topic") String topic) throws IOException{
		System.out.println("publish...");
		MemcachedClient mcc = new MemcachedClient(new InetSocketAddress(SERVER_IP,11211));
		if(mcc.get(SUB_LIST_NAME)!=null){
			HashMap<String, Vector<String>> map = (HashMap<String, Vector<String>>)mcc.get(SUB_LIST_NAME);
			if(map.get(topic)!=null){
				update(topic,map.get(topic));
			}
		}
		String content = "<html><head><title></title></head><body>PET Lab - Publish<p>"+
						"get pub info:<br>topic = "+topic+
						"<p>subscribe done.</body></html>";
		ResponseBuilder builder = Response.ok(content);
		return builder.build();
	}

	@GET
	@Path("subscribe")
	public Response showSubPage(){
		System.out.println("showSubPage...");
		String content = "<html><head><title></title></head><body>PET Lab - Subscribe<p>"+
						"<form method='POST' action='subscribe'><table><tr><td>mode</td><td><input type='text' name='mode'/></td></tr>"+
						"<tr><td>topic</td><td><input type='text' name='topic'/></td></tr>"+
						"<tr><td>callback</td><td><input type='text' name='callback'/></td></tr>"+
						"<tr><td colspan=2><input type='submit' value='submit'/></td></tr></table></form>"+
						"</body></html>";
		ResponseBuilder builder = Response.ok(content);
		return builder.build();
	}
	
	@POST
	@Path("subscribe")
	public Response subscribe(@FormParam("mode") String mode, @FormParam("topic") String topic, @FormParam("callback") String callback) throws IOException{
		System.out.println("subscribe...");
		MemcachedClient mcc = new MemcachedClient(new InetSocketAddress(SERVER_IP,11211));
		if(mcc.get(SUB_LIST_NAME)==null){
			if(mode.equals("subscribe")){
				HashMap<String,Vector<String>> map = new HashMap<String,Vector<String>>();
				Vector<String> list = new Vector<String>();
				list.add(callback);
				map.put(topic, list);
				System.out.println(mcc.add(SUB_LIST_NAME, 0, map).getStatus().isSuccess());
			}
		}else{
			HashMap<String,Vector<String>> map = (HashMap<String,Vector<String>>)mcc.get(SUB_LIST_NAME);
			if(map.get(topic)==null){
				if(mode.equals("subscribe")){
					Vector<String> list = new Vector<String>();
					list.add(callback);
					map.put(topic, list);
					System.out.println(mcc.replace(SUB_LIST_NAME, 0, map).getStatus().isSuccess());
				}
			}else{
				if(mode.equals("subscribe")){
					Vector<String> list = map.get(topic);
					if(!list.contains(callback)){
						list.add(callback);
						map.put(topic, list);
						System.out.println(mcc.replace(SUB_LIST_NAME, 0, map).getStatus().isSuccess());
					}
				}else if(mode.equals("unsubscribe")){
					Vector<String> list = map.get(topic);
					if(list.contains(callback)){
						list.remove(callback);
						map.put(topic, list);
						System.out.println(mcc.replace(SUB_LIST_NAME, 0, map).getStatus().isSuccess());
					}
				}				
			}
		}
		String content = "<html><head><title></title></head><body>PET Lab - Subscribe<p>"+
						"get sub info:<br>mode = "+mode+"<br>topic = "+topic+"<br>callback = "+callback+
						"<p>subscribe done.</body></html>";
		ResponseBuilder builder = Response.ok(content);
		return builder.build();
	}
	
	private void update(String topic, Vector<String> subscribers) throws NumberFormatException, UnsupportedEncodingException, IOException{
		if(subscribers.size()>0){
			if(checkUri(topic)[0].equals("ok")){
				String content = null;
				String[] url = checkUri(topic);
				URL topicUrl = new URL(url[1],url[2],Integer.parseInt(url[3]),url[4]);
				HttpURLConnection conn = (HttpURLConnection)topicUrl.openConnection();
				conn.setDoInput(true);
				conn.setDoOutput(false);
				conn.setRequestMethod("GET");
				if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
					InputStream is = conn.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
					String tmp;
					StringBuilder sb = new StringBuilder();
					while((tmp=reader.readLine())!=null)
						sb.append(tmp);
					is.close();
					content = sb.toString();
				}else{
					System.out.println("retCode = "+conn.getResponseCode());
				}
				conn.disconnect();
				for(String subscriber:subscribers){
					String[] url1 = checkUri(subscriber);
					if(url1[0].equals("ok")){
						URL callbackUrl = new URL(url1[1],url1[2],Integer.parseInt(url1[3]),url1[4]);
						HttpURLConnection callback = (HttpURLConnection)callbackUrl.openConnection();
						callback.setDoOutput(true);
						callback.setDoInput(true);
						callback.setRequestMethod("POST");
						OutputStream os = callback.getOutputStream();
						os.write(content.getBytes("utf-8"));
						os.flush();
						os.close();
						System.out.println("response code = "+callback.getResponseCode());
						callback.disconnect();
					}
				}
			}else{
				//bad uri;
			}
		}
	}
	
	private String[] checkUri(String urlStr){
		String[] rslt = new String[5];
		int port = -1;
		String host = null;
		String protocol = null;
		String path = null;
		if(urlStr.startsWith("https://")){
			protocol = "https";
			port = 443;
			if(urlStr.replaceFirst("https://", "").split("/")[0].contains(":")){
				port = Integer.parseInt(urlStr.replaceFirst("https://", "").split("/")[0].split(":")[1]);
				host = urlStr.replaceFirst("https://", "").split("/")[0].split(":")[0];
				path = urlStr.replaceFirst("https://"+host+":"+port, "");
			}else{
				host = urlStr.replaceFirst("https://", "").split("/")[0];
				path = urlStr.replaceFirst("https://"+host, "");
			}
		}else if(urlStr.startsWith("http://")){
			protocol = "http";
			port = 80;
			if(urlStr.replaceFirst("http://", "").split("/")[0].contains(":")){
				port = Integer.parseInt(urlStr.replaceFirst("http://", "").split("/")[0].split(":")[1]);
				host = urlStr.replaceFirst("http://", "").split("/")[0].split(":")[0];
				path = urlStr.replaceFirst("http://"+host+":"+port, "");
			}else{
				host = urlStr.replaceFirst("http://", "").split("/")[0];
				path = urlStr.replaceFirst("http://"+host, "");
			}
		}
		System.out.println("checkUri : protocol = "+protocol+", host = "+host+", port = "+port+", path = "+path);
		if(port==-1||host==null||!(protocol.equals("http")||protocol.equals("https"))){
			rslt[0] = "bad";
		}else{
			rslt[0] = "ok";
			rslt[1] = protocol;
			rslt[2] = host;
			rslt[3] = String.valueOf(port);
			rslt[4] = path;
		}
		return rslt;
	}
}
