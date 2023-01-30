/// The openbisServer we use for our data
openbisServer = new openbis('https://localhost:8443/openbis/openbis', 'https://localhost:8444/datastore_server');

function getAppHeight(){
	return Math.max($(window).height() - 50, getVisibleLeafsCountForNode(root) * 30);
}

function getAppWidth(){
	return $(window).width();
}


var didCreateVis = false;


/**
 * The model that manages state and implements the operations.
 */
function CrudModel() {
	this.clearModel();
}

CrudModel.prototype.clearModel = function() {
	this.operation = "INSERT";
	this.infotext = "";
	this.identifier = "";
}

CrudModel.prototype.callOperation = function() {
	var parameters = {};
	parameters["info"] = this.infotext;
	parameters["id"] = this.identifier;
	parameters["operation"] = this.operation;

	showPendingOperation();
	// Call the operation and then call the aggregation service when complete to update the data.
	openbisServer.createReportFromAggregationService("DSS1", "crud-ingestion-service", parameters, displayIngestionResults);
}

/// The model that manages state and implements the operations
var model;
model = new CrudModel();


/// The visualization, referenced by functions that display content
var vis;

/**
 * Create the DOM elements to store the visualization (tree + inspectors)
 */
function createVis()
{ 
	if (didCreateVis) return;
	
	// Create a div to house the tree visualization and the inspectors
	vis = d3.select("#main").insert("div", "#add-edit-form").attr("id", "vis");

	didCreateVis = true;
}

/**
 * Construct the table header.
 */
function showTableHeader(table)
{
	var header = table.selectAll("thead").data(function(d) { return [d.columns] });
	header.enter().append("thead");
	var headerRows = header.selectAll("tr.header").data(function(d) { return [d] });
	headerRows.enter().append("tr").attr("class", "header");
	var headerData = headerRows.selectAll("th.header").data(function(d) { return d; });
	headerData.enter().append("th").attr("class", "header");
	headerData.text(function (d) { return d.title})
	var headerActions = headerRows.selectAll("th.actions").data(["Actions"]);
	headerActions.enter().append("th").attr("class", "actions");
	headerActions.text(function (d) { return d})	
}

function clickRowAction(action, data) {
	// Action 0 is delete, action 1 is edit
	var isDelete = 0 == action;

	// The first element is the id, the second is the text.
	var identifier = parseInt(data[0].value);
	var infotext = data[1].value;

	model.identifier = identifier;
	model.infotext = infotext;

	if (isDelete) {
		model.operation = 'DELETE';
		model.callOperation();
	} else {
		model.operation = 'UPDATE';
		synchronizeUiToModel();
	}
}

/**
 * Construct the table data.
 */
function showTableData(table)
{
	var actions = ["del", "edit"];
	var dataRows = table.selectAll("tr.data").data(function(d) { return d.rows });
	dataRows.enter().append("tr").attr("class", "data");
	dataRows.exit().remove();

	var dataData = dataRows.selectAll("td.data").data(function(d) { return d });
	dataData.enter().append("td").attr("class", "data");
	dataData.text(function (d) { return d.value})
	var dataActions = dataRows.selectAll("td.action").data([actions]);
	dataActions.enter().append("td").attr("class", "action");
	var actionButtons = dataActions.selectAll("button.action").data(function(d) { return d });
	actionButtons.enter()
		.append("button")
		.attr("class", "action")
		// We navigate the DOM hierarchy to get at the data that we want to manage
		.on("click", function(d, i) { clickRowAction(i, this.parentElement.parentElement.__data__)})
		.style("padding", "0px 10px")
	actionButtons.text(function(d) { return d });

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
	var tableData = data.result;
	
	// Display the rows in a table
	var table = vis.selectAll("table").data([tableData]);
	// Code under enter is run if there is no HTML element for a data element	
	table.enter().append("table");
	showTableHeader(table);
	showTableData(table);
}

/**
 * Display the results of the call to ingestion service
 */
function displayIngestionResults(data)
{
	if (data.error) {
		console.log(data.error);
		vis.append("p").text("Could not retrieve data.");
		return;
	}

	showPendingOperationFinished();
	callAggregationService();
}

/**
 * Give user feedback about the operation.
 */
function showPendingOperation() {
	var op = model.operation;
	var status = vis.selectAll("div.status").data([op]);
	status.enter().append("div").attr("class", "status");
	status.text(function(d) { return d + " pending."});
	model.clearModel();	
	synchronizeUiToModel();
}

/**
 * Give user feedback about the operation.
 */
function showPendingOperationFinished() {
	var status = vis.selectAll("div.status").data([]);
	status.exit().remove();
}

/**
 * Take the information in the model and put it into the UI
 */
function synchronizeUiToModel() {
	$('#identifier').val(model.identifier);
	$('#info').val(model.infotext);

	var buttonText = ('INSERT' == model.operation) ? "Add" : "Update";
	$('#insert-update-button').text(buttonText);
}

/**
 * Request samples matching some criteria from the server and show them in the Page.
 */
function callAggregationService()
{
	var parameters = {};

	openbisServer.createReportFromAggregationService("DSS1", "crud-aggregation-service", parameters, displayReturnedTable);
}

function configureAddForm()
{
	$("#info").focus();
	$('#add-edit-form').submit(function() {
		model.infotext = $.trim($("#info").val());
		model.callOperation();
	});
	$('#clear-button').click(function() {
		model.clearModel();
		synchronizeUiToModel();
	});
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
	
	createVis();
	configureAddForm();
	callAggregationService();
}