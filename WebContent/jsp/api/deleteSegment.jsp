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

</head>
<body>
	<center>
	<form name="deletesegment" method="post" action="./../../processForm">
		<input type="hidden" name="event" value="deleteSelectedSegmentsConfirm"/>
		<input type="hidden"  name="isApi" value="true"/>
		<table>
			<tr> <h2>DELETE SELECTED SEGMENTS HTML FILES </h2></tr>
			<tr>
				<td> DOMAIN NAME: </td>
				<td> <input type="text" name="domainName" size="50"/></td>
			</tr><br>
			<tr>
				<td> SEGMENTS NAMES: </td>
				<td> <input type="text" name="segmentNames"  size="50"/></td>
			</tr><br>
			<tr>
				<td></td>
				<td> (please enter segment names (comma separated))</td>
			</tr><br>
			<tr>
				<td></td>
				<td> <input type="submit" name="deleteSegments" value="DELETE SEGMENTS FILES" size="25"/></td>
			</tr>
		</table>	
	</center>
	</form>	
</body>
</html>