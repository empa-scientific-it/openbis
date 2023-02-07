window.onerror = function() {
	parent.location = './Error.htm';
}

function loadControls() {
   loadXSLTable('../reports/controls.xml', './controls.xsl', 'ControlsTbl');
}