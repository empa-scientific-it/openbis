window.onerror = function() {
	parent.location = './Error.htm';
}

function loadClustersByLane() {
	name = name0 = '../reports/NumClusters By Lane';
	if (document.getElementById('PFOnlyBox').checked) {
		name = name0 + " PF";
		try {
			xmlDoc = loadXMLDoc(name + '.xml');
		} catch (e) { xmlDoc = null; };
		if (xmlDoc == null || xmlDoc.getElementsByTagName('Data') == null) {
			name = name0;
			alert("No data available!");
			document.getElementById('PFOnlyBox').checked = false;
		}
	}
	document.getElementById('NumClustersImg').src = name + ".png";
	imgRefresh('NumClustersImg');
	loadNumClustersTable(name + '.xml', 'NumClustersTbl', 'Lane');
}

function loadNumClustersTable(xmlName, tblName, tagsName) {
	tbl = document.getElementById(tblName);
	xmlDoc = loadXMLDoc(xmlName);
	if (xmlDoc == null) return;
	xmlRows = xmlDoc.getElementsByTagName(tagsName);
	if (xmlRows == null) return;
	while (tbl.rows.length > 0) tbl.deleteRow(tbl.rows.length - 1);
	if (xmlRows.length > 0 && xmlRows[0].attributes.length > 0) {
		tbl.insertRow(0);
		for (j = xmlRows[0].attributes.length - 1; j >= 1; j--)
			tbl.insertRow(0);
		for (i = xmlRows.length - 1; i >= 0; i--) {
			for (j = xmlRows[i].attributes.length - 1; j >= 1; j--) {
				tbl.rows[j].insertCell(0);
				val = new Number(xmlRows[i].attributes[j].value);
				if (val < 1000) strVal = val.toFixed(0);
				else strVal = (val / 1000).toFixed(0) + "K";
				tbl.rows[j].cells[0].innerHTML = strVal;
			}
		}
		for (j = xmlRows[0].attributes.length - 1; j >= 1; j--) {
			tbl.rows[j].insertCell(0);
			tbl.rows[j].cells[0].innerHTML = xmlRows[0].attributes[j].name.toUpperCase();
			//tbl.rows[j].insertCell(0); tbl.rows[j].insertCell(0);
		}
		for (i = xmlRows.length - 1; i >= 0; i--) {
			tbl.rows[0].insertCell(0);
			tbl.rows[j].cells[0].innerHTML = tagsName + " " + xmlRows[i].attributes[0].value;
		}
		tbl.rows[0].insertCell(0); //tbl.rows[0].insertCell(0); tbl.rows[0].insertCell(0);
	}
}

