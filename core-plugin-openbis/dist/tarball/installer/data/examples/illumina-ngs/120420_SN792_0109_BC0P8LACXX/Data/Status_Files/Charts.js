window.onerror = function() {
	parent.location = './Error.htm';
}

var imageOnFocusId = null;
function TileMap(nTiles) {
	var numLanes = 8;
	var numTiles = nTiles;
	var width = 200;
	var height = 750;
	var X0 = 18;
	var Y0 = 26;
	var dL = width / numLanes;
	var numCols = 2;
	var numRows = numTiles / numCols;
	var dX = width / (numCols * numLanes);
	var dY = height / numRows;
	var map = document.getElementById("tileMap");
	var tileMapXML;
	this.changeLayout = function(layoutNode) {
		nCols = layoutNode.getAttribute("ColsPerLane");
		if (nCols > 0) numCols = nCols;
		nRows = layoutNode.getAttribute("RowsPerLane");
		if (nRows > 0) numRows = nRows;
		dX = width / (numCols * numLanes);
		dY = height / numRows;
	}
	this.newToolTip = function(lane, idx) {
		var area = document.createElement("area");
		area.shape = "rect";
		var col = Math.floor((idx - 1) / numRows);
		var row = (idx - 1) % numRows;
		if (idx - numRows <= 0) row = numRows - row - 1;
		var X1 = Math.round(X0 + dX * col + (lane - 1) * dL);
		var Y1 = Math.round(Y0 + dY * row);
		var X2 = Math.round(X1 + dX);
		var Y2 = Math.round(Y1 + dY);
		area.coords = "'" + X1 + "," + Y1 + "," + X2 + "," + Y2 + "'";
		area.title = "L:" + lane + " T:" + idx;
		map.appendChild(area);
	}

	this.newToolTipVal = function(tile, base) {
		var area = document.createElement("area");
		area.shape = "rect";
		key = tile.getAttribute("Key");
		if (base == null) base = "Val";
		val = tile.getAttribute(base);
		lane = key.substring(0, 1);
		idx = key.substring(2);
		var col = Math.floor((idx - 1) / numRows);
		var row = (idx - 1) % numRows;
		if (idx - numRows <= 0) row = numRows - row - 1;
		var X1 = Math.round(X0 + dX * col + (lane - 1) * dL);
		var Y1 = Math.round(Y0 + dY * row);
		var X2 = Math.round(X1 + dX);
		var Y2 = Math.round(Y1 + dY);
		area.coords = "'" + X1 + "," + Y1 + "," + X2 + "," + Y2 + "'";
		area.title = "L:" + lane + " T:" + idx + "\n" + "Val= " + val;
		map.appendChild(area);
	}
	this.resetMap = function(image) {
		if (image.id == imageOnFocusId) return;
		xmlName = image.src;
		xmlName = xmlName.replace("@.png", ".png");
		xmlName = xmlName.substring(0, xmlName.lastIndexOf('.')) + ".xml";
		var base = null;
		if (xmlName.lastIndexOf('_a.xml') > 0) base = 'A';
		else if (xmlName.lastIndexOf('_c.xml') > 0) base = 'C';
		else if (xmlName.lastIndexOf('_g.xml') > 0) base = 'G';
		else if (xmlName.lastIndexOf('_t.xml') > 0) base = 'T';
		if (base != null) xmlName = xmlName.substring(0, xmlName.lastIndexOf('_')) + ".xml";
		try {
			this.tileMapXML = loadXMLDoc(xmlName);
			layout = this.tileMapXML.getElementsByTagName("Layout");
			if (layout != null && layout.length > 0) this.changeLayout(layout[0]);
			tiles = this.tileMapXML.getElementsByTagName("TL");
			while (map.lastChild != null) map.removeChild(map.lastChild);
			for (i = 0; i < tiles.length; i++) tileMap.newToolTipVal(tiles[i], base);
		} catch (e) { this.blankMap(); }
		imageOnFocusId = image.id;
	}


	this.blankMap = function() {
		while (map.lastChild != null) map.removeChild(map.lastChild);
		for (var l = 1; l <= numLanes; l++) {
			for (var i = 1; i <= numTiles; i++) {
				this.newToolTip(l, i);
			}
		}
	}
}

function fillCycles(CyBox, maxCy) {
	for (var cy = 1; cy <= maxCy; cy++) {
		var option = document.createElement('option');
		option.setAttribute('value', cy);
		option.appendChild(document.createTextNode(cy));
		CyBox.appendChild(option);
	}
}

function changeImage(form) {
	var qc = form.QCOptDropDown.value;
	var base = "";
	var cycle = "";
	//alert(form.autoscale.id);
	//return;
	if (form.baseDropDown != null) {
	   if (qc == "ErrorRate" || qc == "NumGT30") {
	      form.baseDropDown.style.display = 'none';
	      document.getElementById(form.autoscale.id.toString() + 'lbl').style.display = 'inline';
	   }
	   else {
	      form.baseDropDown.style.display = 'inline';
	      base = "_" + form.baseDropDown.value.toLowerCase();
	      document.getElementById(form.autoscale.id.toString() + 'lbl').style.display = 'none';

	   }
	}
	if (form.cyDropDown != null) {
		form.cyDropDown.disabled = false;
		if (form.cyDropDown != null && !form.cyDropDown.disabled) {
			cycle = "_" + form.cyDropDown.value.toLowerCase();
			qc = qc + "/";
		}
	} else qc = qc + "_";
	auto = "";
	if (form.autoscale.checked) auto = "@";
	var suff = cycle + base + auto + ".png";
	form.Chart.src = "../reports/" + qc + "Chart" + suff;
	form.Chart.lowsrc = form.Chart.src;
	imageOnFocusId = null;
}

function showBlank(imgId) {
	document.getElementById(imgId).lowsrc = document.getElementById(imgId).src;
	document.getElementById(imgId).src = "../reports/blank.png";
}
