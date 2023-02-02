define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

	//
	// IMAGE VIEWER VIEW
	//

	function ImageViewerView(controller) {
		this.init(controller);
	}

	$.extend(ImageViewerView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("imageViewer");
		},

		render : function() {
			var table = $("<table>").appendTo(this.panel);
			var row = $("<tr>").appendTo(table);

			var formCell = $("<td>").addClass("formCell").appendTo(row);
			var formPanel = $("<div>").addClass("formPanel").appendTo(formCell);

			var chooserContainer = $("<div>");
			var parametersContainer = $("<div>").addClass("imageParametersWidgetContainer");

			formPanel.append(chooserContainer);
			formPanel.append(parametersContainer);

			this.controller.getDataSetChooserWidget().then(function(chooserWidget) {
				chooserContainer.append(chooserWidget.render());
			});

			var imageCell = $("<td>").addClass("imageCell").appendTo(row);
			var imagePanel = $("<div>").addClass("imagePanel").appendTo(imageCell);

			this.controller.getImageWidget().then(function(imageWidget) {
				imagePanel.append(imageWidget.render());
			});

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			this.refreshImageParametersWidget();
		},

		refreshImageParametersWidget : function() {
			var thisView = this;
			var container = this.panel.find(".imageParametersWidgetContainer");

			this.controller.getSelectedDataSetCode().then(function(dataSetCode) {
				thisView.controller.getImageParametersWidget(dataSetCode).then(function(parametersWidget) {
					container.children().detach();
					container.append(parametersWidget.render());
				});
			});
		}

	});

	return ImageViewerView;

});