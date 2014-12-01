<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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
<body>
	<center>
	<form name="deleteDomain" action="./../../processForm" method="post">
			<input type="hidden" name="event" value="deleteDomainConfirm"/>
			<input type="hidden"  name="isApi" value="true"/>
		<table>
			<tr> <h2>DELETE HTML FILES OF DOMAIN </h2></tr>
			<tr>
				<td> DOMAIN NAME: </td>
				<td> <input type="text" name="domainName" size="25"/></td>
			</tr><br>
			<tr>
				<td></td>
				<td> <input type="submit" name="deleteDomain" value="DELETE DOMAIN FILES" size="25"/></td>
			</tr>
		</table>	
	
	</form>	
	</center>
</body>
</html>