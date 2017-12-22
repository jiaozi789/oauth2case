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
     <form action="${pageContext.request.contextPath}/blog" method="post">
     標題 :<input type="text" name="title" /><br/>
    內容: <textarea name="content" rows="20"></textarea><br/>
 <input type="submit"  value ="保存"/>
  </form>
</body>
</html>