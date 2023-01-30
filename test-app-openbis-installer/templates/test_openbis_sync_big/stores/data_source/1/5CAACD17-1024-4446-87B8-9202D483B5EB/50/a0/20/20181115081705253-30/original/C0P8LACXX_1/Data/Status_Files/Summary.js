window.onerror = function() {
	parent.location = './Error.htm';
}

function loadSumTable() {
    for (i = 1; i < 12; i++)
        loadXSLTable('../reports/Summary/read' + i + '.xml', './Summary.xsl', 'SumTbl' + i);
}