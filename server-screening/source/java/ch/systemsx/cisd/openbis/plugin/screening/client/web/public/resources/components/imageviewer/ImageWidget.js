define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/ImageView" ], function($, AbstractWidget, ImageView) {

	//
	// IMAGE WIDGET
	//

	function ImageWidget(imageLoader) {
		this.init(imageLoader);
	}

	$.extend(ImageWidget.prototype, AbstractWidget.prototype, {

		init : function(imageLoader) {
			AbstractWidget.prototype.init.call(this, new ImageView(this));
			this.imageLoader = imageLoader;
		},

		loadImage : function(imageData, callback) {
			this.imageLoader.loadImage(imageData, callback);
		},

		getImageData : function(imageData) {
			return this.imageData;
		},

		setImageData : function(imageData) {
			this.imageData = imageData;
			this.refresh();
		}

	});

	return ImageWidget;

});