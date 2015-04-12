<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="net.spy.memcached.MemcachedClient" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
mc<p>
<%

String SERVER_IP = "localhost";
String SUB_LIST_NAME = "subList";
MemcachedClient mcc = new MemcachedClient(new InetSocketAddress(SERVER_IP,11211));
if(mcc.get(SUB_LIST_NAME)!=null){
	HashMap<String,Vector<String>> map = (HashMap<String,Vector<String>>)mcc.get(SUB_LIST_NAME);
	for(String key:map.keySet()){
		out.print("topic = "+key+"<br>");
		for(String callback:map.get(key)){
			out.print(" callback = "+callback+"<br>");
		}
	}
}else{
	out.println("null");
}

%>
</body>
</html>