<%@ page language="java" contentType="text/html; charset=BIG5"
    pageEncoding="BIG5"%>
<%@ page import="nctu.petlab.tibs.rdf.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=BIG5">
<title>RDF Test Page</title>
</head>
<body>
RDF Test:<br>
<%
	RDFTest test = new RDFTest();
	test.test();
%>
<p>
readRdfFile:<br>
<%
	//test.readRdfFile();
%>
</body>
</html>