<%@page session="true"%>
<html>
<body>
	
	<form>
	<input type="button" value="Back" onClick="history.go(-1);return true;">
	</form>

	<script>
		function formSubmit() {
			document.getElementById("logoutForm").submit();
		}
	</script>

</body>
</html>