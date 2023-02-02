define([ "jquery", "components/imageviewer/AbstractView", "components/imageviewer/LoadingWidget" ], function($, AbstractView, LoadingWidget) {

	//
	// IMAGE VIEW
	//

	function ImageView(controller) {
		this.init(controller);
	}

	$.extend(ImageView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("imageWidget");
		},

		render : function() {
			$("<div>").addClass("imageContainer").appendTo(this.panel);

			this.loading = new LoadingWidget();
			this.panel.append(this.loading.render());

			this.refresh();
			return this.panel;
		},

		refresh : function() {
			var thisView = this;

			if (this.controller.getImageData()) {
				var imageContainer = this.panel.find(".imageContainer");
				this.loading.setLoading(true);

				this.controller.loadImage(this.controller.getImageData(), function(image) {
					imageContainer.empty().append(image);
					thisView.loading.setLoading(false);
				});
			} else {
				this.panel.empty();
			}
		}

	});

	return ImageView;

});