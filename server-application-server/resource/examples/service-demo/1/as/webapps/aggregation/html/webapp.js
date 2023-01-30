/// The openbisServer we use for our data
openbisServer = new openbis('https://localhost:8443/openbis/openbis', 'https://localhost:8444/datastore_server');

function getAppHeight(){
	return Math.max($(window).height() - 50, getVisibleLeafsCountForNode(root) * 30);
}

function getAppWidth(){
	return $(window).width();
}


var didCreateVis = false;

/// The visualization, referenced by functions that display content
var vis;

/**
 * Create the DOM elements to store the visualization (tree + inspectors)
 */
function createVis()
{ 
	if (didCreateVis) return;
	
	// Create a div to house the tree visualization and the inspectors
	vis = d3.select("#main").append("div").attr("id", "vis");

	didCreateVis = true;
}


/**
 * Display the samples returned by the server
 */
function displayReturnedTable(data)
{
	if (data.error) {
		console.log(data.error);
		vis.append("p").text("Could not retrieve data.");
		return;
	}
	
	// This will show the object in the log -- helpful for debugging
	// console.log(data.result);
	var dataToShow = data.result;
	
	// Pick all div elements of the visualization
	vis.selectAll("div").data(dataToShow.rows)
		// Code under enter is run if there is no HTML element for a data element
		.enter()
	.append("div")
		.text(function(row) { return row[0].value + " : " + row[1].value })
	
	console.log(data);
}

/**
 * Request samples matching some criteria from the server and show them in the Page.
 */
function callAggregationService()
{
	var parameters = 
	{
		hello : "world",
		param1 : "Param1",
		foo : "Some text"
	};

	openbisServer.createReportFromAggregationService("DSS1", "example-aggregation-service", parameters, displayReturnedTable);
}


function enterApp(data)
{
	if(data.result == null){
		alert("Login or password incorrect");
		$("#username").focus();
		return;
	}
	
	$("#login-form-div").hide();
	$("#main").show();
	
	$('#openbis-logo').height(30);
	
	createVis()
	callAggregationService();
}