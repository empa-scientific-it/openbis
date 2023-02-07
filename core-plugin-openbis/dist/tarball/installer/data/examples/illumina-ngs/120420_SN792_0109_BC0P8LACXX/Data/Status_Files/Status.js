window.onerror = function() {
	parent.location = "./Status_Files/StatusError.htm"
}

//Reading XML file
var xmlhttp = false;
/* running locally on IE5.5, IE6, IE7 */; /*@cc_on
if (location.protocol == "file:") {
   if (!xmlhttp) try { xmlhttp = new ActiveXObject("MSXML2.XMLHTTP"); } catch (e) { xmlhttp = false; }
   if (!xmlhttp) try { xmlhttp = new ActiveXObject("Microsoft.XMLHTTP"); } catch (e) { xmlhttp = false; }
}
/* IE7, Firefox, Safari, Opera...  */
if (!xmlhttp) try { xmlhttp = new XMLHttpRequest(); } catch (e) { xmlhttp = false; }
/* IE6 */
if (typeof ActiveXObject != "undefined") {
   if (!xmlhttp) try { xmlhttp = new ActiveXObject("MSXML2.XMLHTTP"); } catch (e) { xmlhttp = false; }
   if (!xmlhttp) try { xmlhttp = new ActiveXObject("Microsoft.XMLHTTP"); } catch (e) { xmlhttp = false; }
}
/* IceBrowser */
if (!xmlhttp) try { xmlhttp = createRequest(); } catch (e) { xmlhttp = false; }

function loadXMLDoc(dname) {
   var xmlDoc = null;
	try {
		if (!xmlhttp) return "Your browser doesn't seem to support XMLHttpRequests.";
		xmlhttp.open("GET", dname, false); //make sure open appears before onreadystatechange lest IE will encounter issues beyond the first request
		xmlhttp.send(null);
		var parser = false;
		try {
			parser = new DOMParser();
			xmlDoc = parser.parseFromString(xmlhttp.responseText, "text/xml");
		} catch (e) { parser = false }
		if (!parser) {
			xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
			//xmlDoc = new ActiveXObject("Msxml2.DOMDocument.6.0");
			xmlDoc.async = "false";
			xmlDoc.loadXML(xmlhttp.responseText);
		}
		if(xmlDoc.childNodes.length == 0)xmlDoc= null;
	} catch(e){};
   return xmlDoc;
}
//End of reading XML file


function errorPrint(err) {
   document.writeln(err.description);
   document.writeln("<p>There was a problem loading the file.</p>");
   document.writeln("<p>Please click a link below to try again</p>");
   document.writeln("<p><a href='Status.htm'>Status.htm</a></p>");
  }

function imgRefresh(imgId) {
   var s = document.getElementById(imgId).src;
   if (s.indexOf('?') > 0) s = s.substring(0, s.indexOf('?'));
   document.getElementById(imgId).src = s + "?" + (new Date().getMilliseconds());
}

function imgReload(imgId) {
   document.getElementById(imgId).src = document.getElementById(imgId).lowsrc;
}

function getNodeText(node) {
   var txt = "";
   if (node.textContent != undefined) txt = node.textContent;
   else if (node.text != undefined) txt = node.text;
   return txt;
}

//  Cookie functions
function setCookie(name, value, expires, path, domain, secure) {
   document.cookie = name + "=" + escape(value) +
        ((expires) ? "; expires=" + expires.toGMTString() : "") +
        ((path) ? "; path=" + path : "") +
        ((domain) ? "; domain=" + domain : "") +
        ((secure) ? "; secure" : "");
}

function getCookie(name) {
   var dc = document.cookie;
   var prefix = name + "=";
   var begin = dc.indexOf("; " + prefix);
   if (begin == -1) {
      begin = dc.indexOf(prefix);
      if (begin != 0) return null;
   } else {
      begin += 2;
   }
   var end = document.cookie.indexOf(";", begin);
   if (end == -1) {
      end = dc.length;
   }
   return unescape(dc.substring(begin + prefix.length, end));
}

function deleteCookie(name, path, domain) {
   if (getCookie(name)) {
      document.cookie = name + "=" +
            ((path) ? "; path=" + path : "") +
            ((domain) ? "; domain=" + domain : "") +
            "; expires=Thu, 01-Jan-70 00:00:01 GMT";
   }
}

function loadNumClustersTableByLane(xmlName, tblName, tagsName) {
   tbl = document.getElementById(tblName);
   xmlDoc = loadXMLDoc(xmlName);
   if (xmlDoc == null) return;
   xmlRows = xmlDoc.getElementsByTagName(tagsName);
   while (tbl.rows.length > 0) tbl.deleteRow(tbl.rows.length - 1);
   if (xmlRows.length > 0 && xmlRows[0].attributes.length > 0) {
      for (i = 0; i < xmlRows.length; i++) {
         tbl.insertRow(0);
         for (j = xmlRows[i].attributes.length - 1; j >= 0; j--) {
            tbl.rows[0].insertCell(0);
            tbl.rows[0].cells[0].innerHTML = xmlRows[i].attributes[j].value;
         }
         //create column titles
         tbl.insertRow(0);
      }
      for (j = xmlRows[0].attributes.length - 1; j >= 0; j--) {
         tbl.rows[0].insertCell(0);
         tbl.rows[0].cells[0].innerHTML = xmlRows[0].attributes[j].name;
      }
      tbl.rows[0].cells[0].innerHTML = tagsName + "#"
   }
}

function loadXSLTable(xmlName, xslName, tblName) {
	tbl = document.getElementById(tblName);
	xmlDoc = loadXMLDoc(xmlName);
	xslDoc = loadXMLDoc(xslName);
	if (xmlDoc == null || xslDoc == null) return false;
	// code for IE
	if (window.ActiveXObject) {
		ex = xmlDoc.transformNode(xslDoc);
		tbl.innerHTML = ex;
	}
	// code for Mozilla, Firefox, Opera, etc.
	else if (document.implementation && document.implementation.createDocument) {
		xsltProcessor = new XSLTProcessor();
		xsltProcessor.importStylesheet(xslDoc);
		resultDocument = xsltProcessor.transformToFragment(xmlDoc, document);
		if ( tbl.hasChildNodes() )
			while ( tbl.childNodes.length >= 1 ) 
				tbl.removeChild( tbl.firstChild );
		tbl.appendChild(resultDocument);
		return true;
	}
}

function xslTransform(xmlDoc, xslDoc) {
	resultDocument = null;
	if (xmlDoc != null && xslDoc != null) {
		// code for IE
		if (window.ActiveXObject) {
			ex = xmlDoc.transformNode(xslDoc);
			resultDocument = ex;
		}
		// code for Mozilla, Firefox, Opera, etc.
		else if (document.implementation && document.implementation.createDocument) {
			xsltProcessor = new XSLTProcessor();
			xsltProcessor.importStylesheet(xslDoc);
			resultDocument = xsltProcessor.transformToFragment(xmlDoc, document).textContent;
		}
	}
	return resultDocument;
}
