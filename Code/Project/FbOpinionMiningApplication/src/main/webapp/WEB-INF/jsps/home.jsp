<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" type="text/css" href="/static/css/home.css">

<script type="text/javascript">
	function checkpostid() {

		var postId = document.getElementById("postid").value;
		if (postId == '') {
			document.getElementById("error_msg_label").innerHTML = "Post ID cannot be empty.";
			alert('Enter a post id to continue');
		} else {
			var fd = new FormData();
			fd.append("postid", postId);
			var xhr = new XMLHttpRequest();
			xhr.onreadystatechange = function() {
				

				document.getElementById("loading").innerHTML = '<img src="/static/images/giphy.gif" width="42" height="42"/>';
				document.getElementById("error_msg_label").innerHTML = "";
				
				if (xhr.readyState == 4 && xhr.status == 200) {
					document.getElementById("result").innerHTML = xhr.responseText;
					document.getElementById("back").style.visibility = 'visible';
					
					document.getElementById("loading").innerHTML = '';

				}
				
				else if(xhr.readyState == 4 && xhr.status != 200){
					document.getElementById("error_msg_label").innerHTML = "Error occurred while retreiving the comments.";
					document.getElementById("loading").innerHTML = '';			
				}
			};
			xhr.open("POST", "/retreivecomments");
			xhr.send(fd);
		}
	}

	function formBack() {
		window.location = '/home';
	}
</script>
</head>
<body>

	<div class="container">

		<header>
			<h1>
				<span id="back" class="backspan"><a
					href="javascript:formBack()">Back</a></span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Opinion
					Mining Application</span><span class="logoutspan"><jsp:include
						page="logout.jsp"></jsp:include></span>
			</h1>
		</header>

		<div class="resultdiv" id="result">
			<form action="/retreivecomments" method="post" name="comments"
				id="comments">


				<div class="divlabel">

					<div>
						<label class="error_msg_label" id="error_msg_label"></label>
					</div>
					
					<div class="" id="loading"></div>
					
					<div class="msg_display_block"></div>
					<label><b>Please provide the post id for opinion mining
							process.</b></label></br> </br> </br> <input style="" type="text"
						placeholder="Enter Post Id" id="postid" name="postid" required>
					</br> </br> </br> <input class="button_class" type="button"
						onclick="checkpostid()" value="Start">
				</div>


			</form>


		</div>




		<footer>Copyright © OpinionMining.com</footer>

	</div>

</body>
</html>

