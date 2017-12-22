<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
     <form action="${pageContext.request.contextPath}/queryBlog" method="post">
     內容 :<input type="text" name="name" value="${param.name }"/>
 <input type="submit"  value ="搜索"/><input type="button"  value ="新增" onclick="window.location='add.jsp'"/>
  </form>
  <a href="${pageContext.request.contextPath}/loginout">退出登录</a> 
  
  <BR/>
    <c:forEach var="blogTmp" items="${blogList}">
       <b>${blogTmp.title }</b><br/>
      ${blogTmp.content }
      <hr>
    </c:forEach>
</body>
</html>