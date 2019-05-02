<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<link rel="stylesheet" type="text/css" href="/static/css/home.css">
</head>
<body>
	<h3>&nbsp;&nbsp;Aspect Score Table</h3>
	<div
		style="border: 1px solid #ccc; margin-bottom: 50px; border-radius: 5px; padding-bottom: 10px; padding-top: 10px; padding-left: 10px; width: 95%; height: 75%; overflow-y: scroll; font-size: 14px; margin-top: 25px; align: center;">
		<table style="table-layout: fixed" id="aspectTable">
			<tr>
				<th>Aspect</th>
				<th>Polarity</th>
				<th>Final Score</th>
			</tr>
			<c:forEach var="aspects" items="${aspectTable}">
				<tr>
					<td>${aspects.aspect}</td>
					<td>${aspects.polarity}</td>
					<td>${aspects.finalAspectScore}</td>
				</tr>
			</c:forEach>
		</table>
	</div>

	<h3>&nbsp;&nbsp;Sentence Score Table</h3>
	<div
		style="border: 1px solid #ccc; margin-bottom: 50px; border-radius: 5px; padding-bottom: 10px; padding-top: 10px; padding-left: 10px; width: 95%; height: 75%; overflow-y: scroll; font-size: 14px; align: center;">
		<table style="table-layout: fixed" id="sentenceTable">
			<tr>
				<th>Sentence</th>
				<th>Score</th>
				<th>Polarity</th>
				<th>Rating</th>
			</tr>
			<c:forEach var="sentences" items="${sentenceTable}">
				<tr>
					<td>${sentences.sentence}</td>
					<td>${sentences.score}</td>
					<td>${sentences.polarity}</td>
					<td>${sentences.rating}</td>
				</tr>
			</c:forEach>
		</table>
	</div>

	<h3>&nbsp;&nbsp;Sentence Summary Table</h3>
	<div
		style="border: 1px solid #ccc; margin-bottom: 50px; border-radius: 5px; padding-bottom: 10px; padding-top: 10px; padding-left: 10px; width: 95%; height: 75%; overflow-y: scroll; font-size: 14px; align: center;">
		<table style="table-layout: fixed" id="summaryTable">
				<tr>
					<th>No of Sentences</th>
					<td>${noOfSentences}</td>
				</tr>
					<tr>
					<th>No of Positive Sentences</th>
					<td>${noOfPositiveSentences}</td>
				</tr>
					<tr>
					<th>No of Negative Sentences</th>
					<td>${noOfNegativeSentences}</td>
				</tr>
					<tr>
					<th>No of Neutral Sentences</th>
					<td>${noOfNeutralSentences}</td>
				</tr>
		</table>
	</div>
</body>
</html>