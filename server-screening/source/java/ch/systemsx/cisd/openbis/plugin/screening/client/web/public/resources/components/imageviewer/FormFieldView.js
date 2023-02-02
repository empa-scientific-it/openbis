define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

	//
	// FORM FIELD VIEW
	//

	function FormFieldView(controller) {
		this.init(controller);
	}

	$.extend(FormFieldView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("formFieldWidget").addClass("form-group");
		},

		render : function() {
			var table = $("<table>").addClass("mainTable").appendTo(this.panel);
			var row = $("<tr>").appendTo(table);

			var labelContainer = $("<td>").addClass("labelContainer").appendTo(row);
			var buttonsContainer = $("<td>").addClass("buttonsContainer").appendTo(row);
			var widgetContainer = $("<div>").addClass("widgetContainer").appendTo(this.panel);

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			if (this.controller.isEnabled()) {
				this.panel.removeClass("disbaled");
			} else {
				this.panel.addClass("disabled");
			}

			var labelContainer = this.panel.find(".labelContainer");
			var buttonsContainer = this.panel.find(".buttonsContainer");
			var widgetContainer = this.panel.find(".widgetContainer");

			this.renderOrRefreshLabel(labelContainer);
			this.renderOrRefreshButtons(buttonsContainer);
			this.renderOrRefreshWidget(widgetContainer);
		},

		renderOrRefreshLabel : function(container) {
			var label = this.controller.getLabel();

			if (this.currentLabel == undefined || this.currentLabel != label) {
				container.empty().append($("<label>").text(label));
				this.currentLabel = label;
			}
		},

		renderOrRefreshButtons : function(container) {
			container.empty().append(this.renderButtons());
		},

		renderButtons : function() {
			var table = $("<table>");
			var row = $("<tr>").appendTo(table);

			var map = this.controller.getButtonsMap();
			for (name in map) {
				var button = map[name];
				var cell = $("<td>").appendTo(row);
				var link = $("<a>").text(button.text).click(button.action).appendTo(cell);

				if (button.tooltip) {
					link.attr("title", button.tooltip);
				}
			}

			return table;
		},

		renderOrRefreshWidget : function(container) {
			var widget = this.controller.getWidget();

			if (this.currentWidget == undefined || this.currentWidget != widget) {
				container.empty().append(widget);
				this.currentWidget = widget;
			}
		}

	});

	return FormFieldView;

});