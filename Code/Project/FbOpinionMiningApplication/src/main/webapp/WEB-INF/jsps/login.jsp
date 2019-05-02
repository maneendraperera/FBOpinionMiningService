<!DOCTYPE html>
<html>
<head>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<link rel="stylesheet" type="text/css" href="/static/css/login.css">
</head>
<body>
	<form action="/login" method="post" name="login" id="login">
		<div class="imgcontainer">
			<img src="/static/images/opinion.png" alt="Avatar" class="avatar">
		</div>

		<div class="msg_display_block">
			<c:if test="${not empty error}">
				<div class="login_error">${error}</div>
			</c:if>
			<c:if test="${not empty msg}">
				<div class="login_msg">${msg}</div>
			</c:if>
		</div>

		<div class="container">
			<label><b>Username</b></label> 
			<input type="text"
				placeholder="Enter Username" id="username"  name="username" required oninvalid="this.setCustomValidity('Username is required.')"> 
				<label><b>Password</b></label>
			<input type="password" placeholder="Enter Password" id="password" name="password"
				required oninvalid="this.setCustomValidity('Password is required.')">

			<button type="submit">Login</button>
			<input type="checkbox" checked="checked"> Remember me
		</div>

		<div class="bgcontainer" style="background-color: #f1f1f1">
			<button type="button" class="cancelbtn">Cancel</button>
			<span class="psw">Forgot <a href="#">password?</a>&nbsp&nbsp
			</span>
		</div>
	</form>

</body>
</html>

