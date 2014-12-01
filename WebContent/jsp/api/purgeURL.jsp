<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="icon" type="image/ico" href="images/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="css/style.css"/>
<title>HM | Home</title>
<script type="text/javascript">

</script>
</head>
<style type="text/css">

body {
    margin:50px 0px; padding:0px;
    text-align:center;
    align:center;
}
</style>
<body>
	<center>
	<form name="purgeURL" method="post" action="./../../processForm">
		<input type="hidden" name="event" value="purgeURLConfirm"/>
		<input type="hidden"  name="isApi" value="true"/>
		<table>
		
			<tr>
				PURGE URL
			</tr><br>
			<tr>
				<td> DOMAIN NAME: </td>
				<td> <input type="text" name="domainName" size="50"/></td>
			</tr><br>
			<tr>
				<td> Site Name: </td>
				<td> <input type="text" name="siteName" size="50"/></td>
			</tr><br>
			<tr>
				<td> URL: </td>
				<td> <input type="text" name="url" size="50"/></td>
			</tr><br>
			<tr>
				<td></td>
				<td> <input type="submit" name="purgeURL" value="purgeURL" size="25"/></td>
			</tr>
		</table>	
	
	</form>	
	</center>
</body>
</html>