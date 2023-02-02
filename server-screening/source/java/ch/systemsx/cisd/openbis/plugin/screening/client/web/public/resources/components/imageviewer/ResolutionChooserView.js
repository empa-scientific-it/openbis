define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

	//
	// RESOLUTION CHOOSER VIEW
	//

	function ResolutionChooserView(controller) {
		this.init(controller);
	}

	$.extend(ResolutionChooserView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("resolutionChooserWidget").addClass("form-group");
		},

		render : function() {
			var thisView = this;

			$("<label>").text("Resolution").attr("for", "resolutionChooserSelect").appendTo(this.panel);

			var select = $("<select>").attr("id", "resolutionChooserSelect").addClass("form-control").appendTo(this.panel);

			this.controller.getResolutionsCodes().forEach(function(resolution) {
				$("<option>").attr("value", resolution).text(resolution).appendTo(select);
			});

			select.change(function() {
				if (select.val() == "") {
					thisView.controller.setSelectedResolution(null);
				} else {
					thisView.controller.setSelectedResolution(select.val());
				}
			});

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			var select = this.panel.find("select");

			if (this.controller.getSelectedResolution() != null) {
				select.val(this.controller.getSelectedResolution());
			} else {
				select.val("");
			}
		}

	});

	return ResolutionChooserView;

});