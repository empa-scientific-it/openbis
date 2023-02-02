define([ "jquery", "bootstrap", "bootstrap-slider", "components/imageviewer/AbstractView", "components/imageviewer/FormFieldWidget" ], function($,
		bootstrap, bootstrapSlider, AbstractView, FormFieldWidget) {

	//
	// CHANNEL STACK SERIES CHOOSER VIEW
	//

	function ChannelStackSeriesChooserView(controller) {
		this.init(controller);
	}

	$.extend(ChannelStackSeriesChooserView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("channelStackChooserWidget").addClass("form-group");
		},

		render : function() {
			var thisView = this;

			this.panel.append(this.createSliderWidget());
			this.panel.append(this.controller.getChannelStackButtonsWidget().render());

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			var channelStackId = this.controller.getSelectedChannelStackId();

			if (channelStackId != null) {
				var count = this.controller.getChannelStacks().length;
				var index = this.controller.getChannelStackIndex(channelStackId);
				var channelStack = this.controller.getChannelStacks()[index];
				var formField = this.panel.find(".sliderWidget").data("formField");

				var label = "Series: " + channelStack.seriesNumberOrNull;
				if (channelStack.timePointOrNull != null) {
					label += ", Time: " + channelStack.timePointOrNull + " sec";
				}
				if (channelStack.depthOrNull != null) {
					label += ", Depth: " + channelStack.depthOrNull;
				}

				formField.setLabel(label);
				formField.getWidget().slider("setValue", index);

				var toggle = formField.getButton("toggle");
				toggle.text = this.controller.getChannelStackButtonsWidget().isVisible() ? "Hide Buttons" : "Show Buttons";
				formField.setButton(toggle);

				if (count <= 1) {
					formField.setEnabled(false);
					formField.getWidget().slider("disable");
				} else {
					formField.setEnabled(true);
					formField.getWidget().slider("enable");
				}
			}
		},

		createSliderWidget : function() {
			var thisView = this;

			var input = $("<input>").attr("type", "text").addClass("form-control");
			var formField = new FormFieldWidget();
			formField.setWidget(input);
			formField.setButton({
				"name" : "toggle",
				"action" : function() {
					if (thisView.controller.getChannelStacks().length > 1) {
						var buttonsWidget = thisView.controller.getChannelStackButtonsWidget();
						buttonsWidget.setVisible(!buttonsWidget.isVisible());
					}
				}
			});

			var widget = $("<div>").addClass("sliderWidget").addClass("form-group");
			widget.data("formField", formField);
			widget.append(formField.render());

			input.slider({
				"min" : 0,
				"max" : this.controller.getChannelStacks().length - 1,
				"step" : 1,
				"tooltip" : "hide"
			}).on("slide", function(event) {
				if (!$.isArray(event.value) && !isNaN(event.value)) {
					var index = parseInt(event.value);
					var channelStack = thisView.controller.getChannelStacks()[index];
					thisView.controller.setSelectedChannelStackId(channelStack.id, true);
				}
			});

			return widget;
		}

	});

	return ChannelStackSeriesChooserView;

});
