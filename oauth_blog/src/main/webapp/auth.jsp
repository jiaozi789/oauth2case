<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link rel="icon" href="data:image/ico;base64,aWNv">
<script type="text/javascript">
function clickToAcceptCode(){
	window.location="http://localhost:8080/authorizeCode?client_id=${param.client_id}&redirect_uri=${param.redirect_uri}&response_type=${param.response_type}&client_secret=${param.client_secret}&user_id=${userInfo.id}";
}
</script>
</head>
<%
response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
response.setHeader("Access-Control-Max-Age", "3600");
response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
response.setHeader("Access-Control-Allow-Credentials","true"); 
%>
<body>
<c:choose>
	<c:when test="${param.client_id==null || param.redirect_uri==null || param.response_type==null  || param.client_secret==null }">
	非法请求 请重试
	</c:when>
	<c:otherwise>
	<div style="border-radius: 25px;border: 2px solid #8AC007;width: 200px;height: 150px; background: linear-gradient(LightGreen, white);">
  <p/>
  &nbsp;<span style="border-radius: 25px;background-color: gray;">请允许 XX 进行以下操作</span><br/><p/>
     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;读取博客文章<br/>
     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;读取用户信息<br/><p/>
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="取消" style="border-radius: 25px;background-color: #FFDAB9"> <input type="button" value="授权" onclick="clickToAcceptCode()" style="border-radius: 25px;background-color: #FFDAB9"> 
</div>
	</c:otherwise>
</c:choose>
   
</body>
</html>