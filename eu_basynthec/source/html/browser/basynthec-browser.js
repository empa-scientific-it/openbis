var w = 500;

// The strain visualization
var strainVis;

// a map holding data sets by strain
var dataSetsByStrain = { };

// prefixes of strain names to be grouped togehter
var STRAIN_GROUP_PREFIXES = [ "JJS-DIN", "JJS-MGP" ];

// The names to show the user for the strain groups
var STRAIN_GROUP_PREFIXES_DISPLAY_NAME = {"JJS-DIN" : "JJS-DIn", "JJS-MGP" : "JJS-MGP" };

// Groups of strains to be displayed  
var strainGroups = [];

// The data set type visualization
var dataSetTypeVis, od600View, metabolomicsView, transcriptomicsView, proteomicsView;

// A map holding data sets by type
var dataSetsByType = { };

// Variables for determining if a particular object is a data set or a strain
var typeDataSet = "DATA_SET", typeStrain = "STRAIN";

var IGNORED_DATASET_TYPES = [ "EXCEL_ORIGINAL", "TSV_EXPORT", "UNKNOWN" ];

//The inspected strains
var inspected = [];

//The node inspectors
var inspectors;

/** Hides the explanation and shows the element to display the explanation again */
function hideExplanation() {
	$('#explanation').hide();
	$('#explanation-show').show();	
}

/** Display the explanation again */
function showExplanation() {
	$('#explanation-show').hide();		
	$('#explanation').show();
}

/** Compute the dataSetsByType variable */
function initializeDataSetsByType() {
	// Group data sets by type
	dataSetsByType = basynthec.dataSetList.reduce(
		function(result, dataSet) {
			var listForType = result[dataSet.dataSetTypeCode];
			if (listForType == null) {
				listForType = [];
				result[dataSet.dataSetTypeCode] = listForType;
			}
			listForType.push(new DataSetWrapper(dataSet));
			return result;
		}, {});
}

/** Compute the dataSetsByStrain variable */
function initializeDataSetsByStrain() {
	// group dataSets
	dataSetsByStrain = basynthec.dataSetList.reduce(
		function(result, dataSet) { 
			var uniqueStrains = uniqueElements(basynthec.getStrains(dataSet).sort());
			
			uniqueStrains.forEach(function(strain) {
					if (!result[strain]) {
						result[strain] = new StrainWrapper(strain);
					}
					result[strain].dataSets.push(dataSet);
			});
			return result;
		}, {});
}

/**
 * Shows the list of data sets retrieved from the openBIS server.
 */
function showDataSets(bisDataSets) {
	if (null == bisDataSets) return;
	
	basynthec.dataSetList = bisDataSets.filter(function(dataSet) { 
		return IGNORED_DATASET_TYPES.indexOf(dataSet.dataSetTypeCode) == -1;
  });
	
	// sort data sets
	var sortByTypeAndRegistration = function(a, b) {
		if (a.dataSetTypeCode == b.dataSetTypeCode) {
			return b.registrationDetails.registrationDate - a.registrationDetails.registrationDate;
		}
		return (a.dataSetTypeCode < b.dataSetTypeCode) ? -1 : 1;
	};
	
	basynthec.dataSetList.sort(sortByTypeAndRegistration);
	
	initializeDataSetsByType();
	initializeDataSetsByStrain();
	
	refreshDataSetTypeTables();
	refreshStrainTables();
}

function createStrainGroups(strains) {
	
	var groups = STRAIN_GROUP_PREFIXES.map(
			function(strainPrefix) {
				var filtered = strains.filter(function(strain) { 
			    return strain.indexOf(strainPrefix) >= 0
			  });
				var groupStrains = filtered.map(function(strain) {
					return { name : strain, label : strain.substring(strainPrefix.length)};
			  });
				
				return {groupName : STRAIN_GROUP_PREFIXES_DISPLAY_NAME[strainPrefix], strains : groupStrains};
	});
	
	var otherStrains = strains.filter(function(strain) {
      return false == STRAIN_GROUP_PREFIXES.some(function(prefix) { return strain.indexOf(prefix) >=0; } );
	});
	otherStrains = otherStrains.map(function(strain) { return {name:strain, label:strain}});
	groups.push({groupName : "Other strains", strains : otherStrains});
	
	var sortFunction = sortByProp("name")
	groups.forEach(function(group) { group.strains.sort(sortFunction); });

	// only return groups that have strains
	return groups.filter(function(group) { return group.strains.length > 0 });
}

function refreshStrainTables() {
	var strains = []
	for (strainName in dataSetsByStrain) {
		strains.push(strainName)
	}
	strainGroups = createStrainGroups(strains);
	
  createVis();
 	updateStrainDiagram(1000);
}

function refreshDataSetTypeTables() {
  createVis();
 	updateDataSetTypeDiagram(1000);
}

function createDataSetSummaryView(group, type, id)
{
	var container, result;
	container = group.append("div");

	container.append("h2").attr("class", "datasetsummarytable").text(type);
	
	result =  
		container.append("div")
			.attr("id", id)
			.attr("class", "datasummaryview");
			
	return result;
}

var didCreateVis = false;

function createVis()
{
	if (didCreateVis) return;
	
	var top = d3.select("#main");
	var tableRoot = top.append("div").attr("id", "table-root").style("width", w + 5 + "px").style("float", "left");
	
	
	// An element for the inspectors.
	inspectors = top.append("span")
		.style("width", "500px")
		.style("position", "relative")
		.style("overflow", "auto")
		.style("float", "left")
		.style("left", "20px");

	// Create the dataSetType visualization
	dataSetTypeVis = tableRoot.append("div");
	dataSetTypeVis.style("width", w + "px");
	od600View = createDataSetSummaryView(dataSetTypeVis, "OD600", "od600");
	metabolomicsView = createDataSetSummaryView (dataSetTypeVis, "Metabolomics", "metabolomics");
	transcriptomicsView = createDataSetSummaryView (dataSetTypeVis, "Transcriptomics", "transcriptomics");
	proteomicsView = createDataSetSummaryView (dataSetTypeVis, "Proteomics", "proteomics");
	
	// Initially hide the strain view -- it is activated by the radio button
	strainVis = tableRoot.append("div").style("display", "none");
	strainVis.style("width", w + "px");
	
	
	didCreateVis = true;
}

function updateDataSetTypeDiagram(duration)
{
	// Update the OD600 View
	updateDataSetTypeView(od600View, "OD600");
	updateDataSetTypeView(metabolomicsView, "METABOLITE_INTENSITIES");
	updateDataSetTypeView(transcriptomicsView, "TRANSCRIPTOMICS");
	updateDataSetTypeView(proteomicsView, "PROTEIN_QUANTIFICATIONS");
}


function updateStrainDiagram(duration)
{
	var strainDiv = strainVis.selectAll("div.strains").data(strainGroups)
		.enter()
	.append("div")
		.attr("class", "strains");

	strainDiv
		.append("h2")
			.text(function(d) { return d.groupName });
	strainDiv
		.append("table")
			.selectAll("tr").data(function(d) { 
					// Group the different sets of strains differently
					if (d.groupName.indexOf("Other") == 0) return d.strains.reduce(groupBy(3), []);
					if (d.groupName.indexOf("JJS-MGP") == 0) return d.strains.reduce(groupBy(10), []);
				
					// Group the JJS-DIn strains by runs
					return d.strains.reduce(groupByRuns(10), []) })
				.enter()
			.append("tr")
			.selectAll("td").data(function(d) { return d })
				.enter()
			.append("td")
			.on("click", toggle_inspected)
			.text(function(d) { return d.label });
}

function updateDataSetTypeView(aView, type)
{
	var dataSetsForType = dataSetsByType[type];
	
	if (dataSetsForType == null) {
		aView.selectAll("p")
			.data(["No Data"])
		.enter()
			.append("p")
			.text("No Data");
		return;
	}
	
	aView.selectAll("table")
		.data([dataSetsForType])
	.enter()
		.append("table")
		.attr("class", "datasetsummarytable")
		.selectAll("tr")
			.data(function (d) { return d})
		.enter()
			.append("tr")
			.on("click", toggle_inspected)
				.selectAll("td")
					.data(function (d) { return [d.dateString, d.userEmail, d.strainString] })
				.enter()
					.append("td")
					.style("width", "33%")
					.text(function (d) { return d});
}

/** Utility function to gracefully switch from one visualization to another */
function toggleDisplayedVisualizations(visToShow, visToHide)
{
	visToShow
	.style("display", "inline")
		.transition()
	.duration(1000)
	.style("opacity", 1);
	
	visToHide
		.transition()
	.duration(1000)
	.style("opacity", 0)
	.style("display", "none");
}

/** Show the data sets grouped by type */
function switchToDataSetTypeView()
{
	hideExplanation();
	toggleDisplayedVisualizations(dataSetTypeVis, strainVis);
}

/** Show the data sets by strain*/
function switchToStrainView()
{
	hideExplanation();
	toggleDisplayedVisualizations(strainVis, dataSetTypeVis);
}

/**
 * Draw / update node inspectors
 */
function updateInspectors(duration)
{	
	var inspector = inspectors.selectAll("div.inspector").data(inspected, function (d) { return d.name });
	
	var box = inspector.enter().append("div")
		.attr("class", "inspector")
		.text(function(d) { return d.name });

	box.append("span")
		.attr("class", "close")
		.on("click", toggle_inspected)
		.text("x");
	
	var dataSetList = inspector.selectAll("ul").data(function (d) { return [d] });
	dataSetList.enter()
	  .append("ul")
	  .attr('class', 'dataSets');
	
	
	var dataSetElt = dataSetList.selectAll("li").data(function (d) { return d.dataSets });
	dataSetElt.enter()
	  .append("li")
	  .text(function(d) { return dataSetLabel(d) });
	
	var dataSetDetailsElt = dataSetElt.selectAll("div.dataSetDetails").data(function(d) { return [d]; });
	dataSetDetailsElt
	  .enter()
	    .append("div")
	      .attr("class", "dataSetDetails"); 
	
	var propsTable = dataSetDetailsElt.selectAll("table.properties").data(function(d) {return [d]});
	
	propsTable.enter()
	  .append("table")
	  .attr("class", "properties");
	
	propsTable.selectAll("tr").data(function(d) { return props_to_pairs(d.bis.properties) })
		.enter()
			.append("tr")
			.selectAll("td").data(function(d) { return d } ).enter()
				.append("td")
				.attr("class", function(d, i) { return (i == 0) ? "propkey" : "propvalue"})
				.style("opacity", "0")
				.text(function(d) { return d })
			.transition()
				.style("opacity", "1");
	
	var downloadTable = dataSetDetailsElt.selectAll("table.downloads").data(function(d) { return [d] });
	
	downloadTable
		.enter()
			.append("table")
				.attr("class", "downloads")
			
	// Add a caption, but make sure there is just one (this does not work with select())
	downloadTable.selectAll("caption").data(["Files"])
		.enter()
			.append("caption").text(function(d) { return d; });
			
	// We just want to see non-directories here
	var downloadTableRow = downloadTable.selectAll("tr").data(filesForDataSet, function(d) { return d.pathInDataSet });
	downloadTableRow
		.enter()
			.append("tr")
				.append("td")
				.on("click", downloadTableFile)
				.text(function(d) { return d.pathInListing });
	downloadTableRow
		.exit()
			.transition()
				.duration(duration)
				.style("opacity", "0")
				.remove();
				
	var height = 200, width = 200;
	var dataDisplay = dataSetDetailsElt.selectAll("svg").data(od600DataForDataSet);
	dataDisplay
		.enter()
	.append("svg:svg")
		.attr("height", height)
		.attr("width", width);
	// Reinitialize the variable
	dataDisplay = dataSetDetailsElt.selectAll("svg").data(od600DataForDataSet);
	var aCurve = dataDisplay.selectAll("g").data(function(d) { return [d[1]]; })
				.enter()
			.append("svg:g");
	// Reinitialize the variable
	aCurve = dataDisplay.selectAll("g").data(curveData);
		// The first two columns of data are the strain name and human-readable desc
	aCurve.selectAll("line").data(lineData)
		.enter()
	.append("svg:line")
		.attr("x1", function(d, i) { return (i / (this.parentNode.__data__.length)) * width; })
		.attr("y1", function(d, i) { return height - (d[0] * height); })
		.attr("x2", function(d, i) { return ((i + 1) / (this.parentNode.__data__.length)) * width;})
		.attr("y2", function(d) { return height - (d[1] * height); })
		.style("stroke", "rgb(0,0,0)")
		.style("stroke-width", "1");
	
	inspector.exit().transition()
		.duration(duration)
		.style("opacity", "0")
		.remove();
}

function downloadTableFile(d)
{
	// If there is no dataset, this is just a marker for loading
	if (!d.dataset) return;
	
	var action = function(data) { 
		try {
			document.location.href = data.result
		} catch (err) {
			// just ignore errors		
		} 
	};
	basynthec.server.getDownloadUrlForFileForDataSet(d.dataset.bis.code, d.pathInDataSet, action);
}

function dataSetLabel(d) {
	return d.bis.dataSetTypeCode + " registered on " + timeformat(new Date(d.bis.registrationDetails.registrationDate)); 
}

function classForNode(d) { 
	return  (d.inspected) ? "inspected" : "";
}

function toggle_inspected(d) {
	hideExplanation();

	if (d.inspected) {
		var index = inspected.indexOf(d) 
		if (index > -1)	inspected.splice(index, 1);
		d.inspected = false;
	} else {
		d.inspected = true;
		d.strainNode = this;
		inspected.push(d);
		if (d.type === typeDataSet) {
			d.dataSets = [{ bis : d.dataSet }];
			retrieveFilesForDataSets(d.dataSets);
		} else if (!d.dataSets) {
			d.dataSets = dataSetsByStrain[d.name].dataSets.map(function(ds){ return {bis : ds} }); 
			retrieveFilesForDataSets(d.dataSets);
		}
	}
	
	d3.select(d.strainNode).attr("class", classForNode(d))
  updateInspectors(500);
}

function filesForDataSet(d)
{
	if (d.loadingFiles) return [{ pathInListing : "Loading..." }];
	
	var fileFilter = function(file) {
		if (!file.isDirectory) {
			if (endsWith(file.pathInDataSet, "xls")) {
				return true;
			}
			if (endsWith(file.pathInDataSet, "xls.tsv")) {
				return true;
			}
		}
		return false;
	};
	
	return (d.files) ? d.files.filter(fileFilter) : [];
}

function retrieveFilesForDataSets(dataSets)
{
	dataSets.forEach(function(ds) {
		   retrieveFilesForDataSet(ds);
	});	
}

function retrieveFilesForDataSet(ds)
{
	if (ds.files) {
		// already retrieved
		return;
	}
	
	ds.loadingFiles = true;
	ds.files = [];

	basynthec.server.listFilesForDataSet(ds.bis.code, "/", true, function(data) {					
		if (!data.result) { 
			return;
		}
		data.result.forEach(function (file) { file.dataset = ds });
		ds.files = ds.files.concat(data.result);
		
		ds.loadingFiles = false; 
		updateInspectors(500);
		
		if (isOd600DataSet(ds)) {
			retrieveOd600DataForDataSet(ds)
		}
				
	});
}

function isOd600DataSet(d) { return "OD600" == d.bis.dataSetTypeCode}

function od600DataForDataSet(d)
{
	if (!isOd600DataSet(d)) return [];

	if (undefined == d.od600) return [[]];
	
	return [d.od600.slice(1)];
//		return [d.od600[1]];
}

/** 
 * Load the OD600 data from the server. This function assumes that the files are already known.
 */
function retrieveOd600DataForDataSet(ds)
{
	if (ds.od600) {
		// already retrieved
		return;
	}
	
	ds.loadingOd600 = true;
	ds.od600 = [];
	
	// Figure out the path to the multistrain TSV file -- this path ends with "xls.tsv".
	var tsvPathInDataSet = "";
	ds.files.forEach(function (file) { if (endsWith(file.pathInDataSet, "xls.tsv")) tsvPathInDataSet = file.pathInDataSet});
		
	var tsvUrl = dssUrl + "/" + ds.bis.code + "/" + tsvPathInDataSet + "?sessionID=" + basynthec.server.sessionToken;

	d3.text(tsvUrl, "text/tsv", function(text) {
		var rows = d3.tsv.parseRows(text, "\t");
		
		ds.od600 = rows;
		ds.loadingOd600 = false; 
		updateInspectors(500);
	});	
}

function curveData(d)
{
	if (!d) return [];
	if (d.length < 2) return [];
	var data = d[1].slice(2);
	return [{length : data.length, max : d3.max(data), values: data}]
}

function lineData(d)
{
	if (!d) return [];
	
	var data = d.values;
	// convert the data into pairs
	var pairs = data.reduce(function(sum, elt) {
		// initialization
		if (sum.length < 1) {
			sum.push([elt / d.max]);
			return sum;
		}
		
		// add the current elt as the second in the last pair and the first in the new pair
		sum[sum.length - 1].push(elt / d.max);
		// don't add the very last element
		if (sum.length < data.length - 1) sum.push([elt / d.max]);
		return sum;
	}, []);
	
	return pairs;
}

function shouldRenderProperty(prop, value) {
	if (prop == STRAIN_PROP_NAME) {
		// strain properties are dealt with separately
		return false;
	}
	if (!value) {
		// do not show properties with no values
		return false;
	}
	return true;
}

/**
 * Convert properties to pairs
 */
function props_to_pairs(d)
{
	var pairs = [];
	
	var dataSetStrains = basynthec.getStrains({properties:d});
	var strainGroups = createStrainGroups(dataSetStrains);
	
	strainGroups.forEach(function(group) {
		var shortedStrains = group.strains.map(function(elt) { return elt.label; });
		shortedStrains = uniqueElements(shortedStrains.sort())
		var pair = [ group.groupName, shortedStrains.join(" ") ];
		pairs.push(pair)
	});
	
	for (var prop in d) {
		if (shouldRenderProperty(prop, d[prop])) {
			var pair = [prop, d[prop]];
			pairs.push(pair);
	  }
	}
	pairs.sort(function(a, b) { 
		if (a[0] == b[0]) return 0;
		// Sort in reverse lexicographical
		return (a[0] < b[0]) ? -1 : 1;
	});
	return pairs;
}

function enterApp()
{
	$("#login-form-div").hide();
	$("#main").show();
	basynthec.listAllDataSets(function(data) { 
		showDataSets(data.result); 
	});
	
	$('#openbis-logo').height(50)
	
}

function groupBy(numElts)
{
	var groupBy = function(groups, elt) {
		if (groups.length < 1) {
			groups.push([elt]);
			return groups;
		}
		
		var lastGrp = groups[groups.length - 1];
		if (lastGrp.length < numElts) {
			lastGrp.push(elt);
		} else {
			groups.push([elt]);
		}
	
		return groups;
	}
	return groupBy;
}

/**
 * Check if the current elt number = last elt number + 1
 */
function isRun(lastElt, currentElt) {
	var lastNumber = Number(lastElt.label);
	var currentNumber = Number(currentElt.label);
	// Assume that non numeric values are runs
	if (lastNumber == NaN || currentNumber == NaN) return true;
	
	return currentNumber == (lastNumber + 1);
}

function groupByRuns(maxNumEltsPerGroup)
{
  var lastSeen = "";
	var groupBy = function(groups, elt) {
		// Initialize the groups
		if (groups.length < 1) { groups.push([elt]); return groups; }

		// Check if we should append to the last group or create a new one
		var lastGrp = groups[groups.length - 1];
		var createNewGroup = false;
		if (lastGrp.length >= maxNumEltsPerGroup) {
			// We've reached the size limit of the group
			createNewGroup = true;
		} else {
			// See if this is a run, if not create a new group
			createNewGroup = !isRun(lastGrp[lastGrp.length - 1], elt);
		}

		(createNewGroup) ? groups.push([elt]) : lastGrp.push(elt);
	
		return groups;
	}
	return groupBy;
}