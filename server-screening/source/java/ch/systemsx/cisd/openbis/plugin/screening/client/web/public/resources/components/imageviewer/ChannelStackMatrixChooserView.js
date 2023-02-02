define([ "jquery", "bootstrap", "bootstrap-slider", "components/imageviewer/AbstractView", "components/imageviewer/FormFieldWidget" ], function($,
		bootstrap, bootstrapSlider, AbstractView, FormFieldWidget) {

	//
	// CHANNEL STACK MATRIX CHOOSER VIEW
	//

	function ChannelStackMatrixChooserView(controller) {
		this.init(controller);
	}

	$.extend(ChannelStackMatrixChooserView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("channelStackChooserWidget").addClass("form-group");
		},

		render : function() {
			var thisView = this;

			this.panel.append(this.createTimePointWidget());
			this.panel.append(this.controller.getTimePointButtonsWidget().render());
			this.panel.append(this.createDepthWidget());
			this.panel.append(this.controller.getDepthButtonsWidget().render());

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			var time = this.controller.getSelectedTimePoint();
			var timeField = this.panel.find(".timePointWidget").data("formField");

			if (time != null) {
				var timeCount = this.controller.getTimePoints().length;
				var timeIndex = this.controller.getTimePoints().indexOf(time);

				timeField.setLabel("Time: " + time + " sec (" + (timeIndex + 1) + "/" + timeCount + ")");
				timeField.getWidget().slider("setValue", time);

				var timeToggle = timeField.getButton("toggle");
				timeToggle.text = this.controller.getTimePointButtonsWidget().isVisible() ? "Hide Buttons" : "Show Buttons";
				timeField.setButton(timeToggle);

				if (timeCount <= 1) {
					timeField.setEnabled(false);
					timeField.getWidget().slider("disable");
				} else {
					timeField.setEnabled(true);
					timeField.getWidget().slider("enable");
				}
			}

			var depth = this.controller.getSelectedDepth();
			var depthField = this.panel.find(".depthWidget").data("formField");

			if (depth != null) {
				var depthCount = this.controller.getDepths().length;
				var depthIndex = this.controller.getDepths().indexOf(depth);

				depthField.setLabel("Depth: " + depth + " (" + (depthIndex + 1) + "/" + depthCount + ")");
				depthField.getWidget().slider("setValue", depthIndex);

				var depthToggle = depthField.getButton("toggle");
				depthToggle.text = this.controller.getDepthButtonsWidget().isVisible() ? "Hide Buttons" : "Show Buttons";
				depthField.setButton(depthToggle);

				if (depthCount <= 1) {
					depthField.setEnabled(false);
					depthField.getWidget().slider("disable");
				} else {
					depthField.setEnabled(true);
					depthField.getWidget().slider("enable");
				}
			}
		},

		createTimePointWidget : function() {
			var thisView = this;

			var input = $("<input>").attr("type", "text").addClass("form-control");
			var formField = new FormFieldWidget();
			formField.setWidget(input);
			formField.setButton({
				"name" : "toggle",
				"action" : function() {
					if (thisView.controller.getTimePoints().length > 1) {
						var buttonsWidget = thisView.controller.getTimePointButtonsWidget();
						buttonsWidget.setVisible(!buttonsWidget.isVisible());
					}
				}
			});

			var widget = $("<div>").addClass("timePointWidget").addClass("form-group");
			widget.data("formField", formField);
			widget.append(formField.render());

			input.slider({
				"min" : 0,
				"max" : this.controller.getTimePoints().length - 1,
				"step" : 1,
				"tooltip" : "hide"
			}).on("slide", function(event) {
				if (!$.isArray(event.value) && !isNaN(event.value)) {
					var timeIndex = parseInt(event.value);
					var time = thisView.controller.getTimePoints()[timeIndex];
					thisView.controller.setSelectedTimePoint(time, true);
				}
			});

			return widget;
		},

		createDepthWidget : function() {
			var thisView = this;

			var input = $("<input>").attr("type", "text").addClass("form-control");
			var formField = new FormFieldWidget();
			formField.setWidget(input);
			formField.setButton({
				"name" : "toggle",
				"action" : function() {
					if (thisView.controller.getDepths().length > 1) {
						var buttonsWidget = thisView.controller.getDepthButtonsWidget();
						buttonsWidget.setVisible(!buttonsWidget.isVisible());
					}
				}
			});

			var widget = $("<div>").addClass("depthWidget").addClass("form-group");
			widget.data("formField", formField);
			widget.append(formField.render());

			input.slider({
				"min" : 0,
				"max" : this.controller.getDepths().length - 1,
				"step" : 1,
				"tooltip" : "hide"
			}).on("slide", function(event) {
				if (!$.isArray(event.value) && !isNaN(event.value)) {
					var depthIndex = parseInt(event.value);
					var depth = thisView.controller.getDepths()[depthIndex];
					thisView.controller.setSelectedDepth(depth, true);
				}
			});

			return widget;
		}

	});

	return ChannelStackMatrixChooserView;

});
