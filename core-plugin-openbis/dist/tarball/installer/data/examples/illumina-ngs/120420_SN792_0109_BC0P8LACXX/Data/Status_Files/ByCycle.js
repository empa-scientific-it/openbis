
window.onerror = function() {
	parent.location = './Error.htm';
}

function changeFrameSrc(form) {
	var imgName = null;
	var base = "";
	var qc = form.QCOptDropDown.value;
	if (qc == 'QScore' || qc == 'NumGT30' || qc == 'ErrRate') {
		form.baseDropDown.style.display = 'none';
	} else {
		form.baseDropDown.style.display = 'inline';
		base = form.baseDropDown.value.toLowerCase();
	};
	var intensityURL = "../reports/IntensityFrame.htm?";

	imgName = "../reports/ByCycle/" + form.QCOptDropDown.value + "_" + form.lanesDropDown.value + base + ".png";
 	if (window.frames[form.name + "Fr"] != null) window.frames[form.name + "Fr"].location = "./ByCycleFrame.htm?" + imgName;
	if (form.imgPlot != null) form.imgPlot.src = "../reports/" + imgName;
}


