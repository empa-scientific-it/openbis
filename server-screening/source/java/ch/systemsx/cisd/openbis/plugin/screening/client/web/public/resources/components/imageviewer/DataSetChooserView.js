define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

	//
	// DATA SET CHOOSER VIEW
	//

	function DataSetChooserView(controller) {
		this.init(controller);
	}

	$.extend(DataSetChooserView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("dataSetChooser");
		},

		render : function() {
			var thisView = this;
			var widget = $("<div>").addClass("form-group").appendTo(this.panel);

			$("<label>").text("Data set").attr("for", "dataSetChooserSelect").appendTo(widget);

			var select = $("<select>").attr("id", "dataSetChooserSelect").addClass("form-control").appendTo(widget);

			this.controller.getDataSetCodes().forEach(function(dataSetCode) {
				var text = thisView.getDataSetText(dataSetCode);
				$("<option>").attr("value", dataSetCode).text(text).appendTo(select);
			});

			select.change(function() {
				thisView.controller.setSelectedDataSetCode(select.val());
			});

			return this.panel;
		},

		getDataSetText : function(dataSetCode) {
			return dataSetCode;
		},

		refresh : function() {
			var select = this.panel.find("select");

			if (this.controller.getSelectedDataSetCode() != null) {
				select.val(this.controller.getSelectedDataSetCode());
			}
		}

	});

	return DataSetChooserView;

});