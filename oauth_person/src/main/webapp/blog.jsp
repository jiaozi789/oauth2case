<%@ page language="java"  
    pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Insert title here</title>
<script type="text/javascript" src="${pageContext.request.contextPath}/jquery-3.1.0.min.js">
</script>
</head>
<body>
<!-- 获取授权和token都跳转到该页面 code！=null就是用户授权会跳到这里 -->
<c:choose>
	<c:when test="${param.code!=null}">
	   <script type="text/javascript">
	        //回跳到这里 说明 授权码 到手 可以发送请求去获取token了 
	   		var tokenurl="http://localhost:8080/access_token";
	        var resourceurl="http://localhost:8888/queryBlogByToken?client_id=blog_s6BhdRkqt3&client_secret=bb32136b-e5eb-11e7-be1a-34de1adabc76&access_token=";
	        //获取授权码后 请求token
	   		$.ajax({
	        	url:tokenurl,
	        	type:"post",
	        	dataType:'json',
	        	data:'client_id=blog_s6BhdRkqt3&redirect_uri=http://localhost:8088/blog.jsp&code=${param.code}&grant_type=authorization_code&client_secret=bb32136b-e5eb-11e7-be1a-34de1adabc76',
	        	success:function(h){
	        		//出错的json格式{"error_description":"不合法的客户端异常 授权码无效","error":"invalid_client"}
	        		if(h.error){
	        			alert(h.error_description);
	        			window.location="index.jsp";
	        			return;
	        		}
	        		var token=h.access_token;
	        		var rurl=resourceurl+token;
	        		//获取token后请求资源
	        		$.ajax({
	        			url:rurl,
	        			dataType:'text',
	        			success:function(tt){
	        				debugger
	        				$("#showDiv").text(tt);
	        			}
	        		})
	        	}
	        })
	        
	   </script>
	   <div id="showDiv"></div>
	</c:when>
	<c:otherwise>非法的请求
	    <Meta http-equiv="Refresh" Content="2; Url=index.jsp">
	</c:otherwise>
</c:choose>

</body>
</html>