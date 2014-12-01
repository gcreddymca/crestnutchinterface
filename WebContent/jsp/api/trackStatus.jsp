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
<title>HM | Track Status</title>
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
	<form name="trackStatus" method="get" action="./../../processForm">
		<input type="hidden" name="event" value="trackConfirm"/>
		<input type="hidden"  name="isApi" value="true"/>
		<table>
		
			<tr>
				<h1>Track Status</h1>
			</tr>
			<tr>
				<td> Tracker ID : </td>
				<td> <input type="text" name="trackerId" size="25"/></td>
			</tr><br>
			<tr>
				<td></td>
				<td>(Please Enter track ID)</td>
			</tr><br>
			
			<tr>
				<td></td>
				<td> <input type="submit" name="trackStatus" value="GET STATUS" size="25"/></td>
			</tr>
		</table>	
	
	</form>	
	</center>
</body>
</html>