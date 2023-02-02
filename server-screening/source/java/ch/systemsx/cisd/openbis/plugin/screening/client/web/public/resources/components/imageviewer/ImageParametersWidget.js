define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/ImageParametersView",
		"components/imageviewer/ChannelChooserWidget", "components/imageviewer/TransformationChooserWidget",
		"components/imageviewer/ResolutionChooserWidget", "components/imageviewer/ChannelStackChooserWidget" ], function($, AbstractWidget,
		ImageParametersView, ChannelChooserWidget, TransformationChooserWidget, ResolutionChooserWidget, ChannelStackChooserWidget) {

	//
	// IMAGE PARAMETERS WIDGET
	//

	function ImageParametersWidget(imageInfo, imageResolutions) {
		this.init(imageInfo, imageResolutions);
	}

	$.extend(ImageParametersWidget.prototype, AbstractWidget.prototype, {
		init : function(imageInfo, imageResolutions, imageLoader) {
			AbstractWidget.prototype.init.call(this, new ImageParametersView(this));
			this.imageInfo = imageInfo;
			this.imageResolutions = imageResolutions;
		},

		doGetState : function(state) {
			state.channelChooserState = this.getChannelChooserWidget().getState();
			state.transformationChooserState = this.getTransformationChooserWidget().getState();
			state.resolutionChooserState = this.getResolutionChooserWidget().getState();
			state.channelStackChooserState = this.getChannelStackChooserWidget().getState();
		},

		doSetState : function(state) {
			this.getChannelChooserWidget().setState(state.channelChooserState);
			this.getTransformationChooserWidget().setState(state.transformationChooserState);
			this.getResolutionChooserWidget().setState(state.resolutionChooserState);
			this.getChannelStackChooserWidget().setState(state.channelStackChooserState);
		},

		getChannelChooserWidget : function() {
			if (this.channelChooserWidget == null) {
				var thisWidget = this;
				var widget = new ChannelChooserWidget(this.imageInfo.imageDataset.imageDataset.imageParameters.channels);
				widget.addChangeListener(function() {
					thisWidget.getTransformationChooserWidget().setSelectedChannels(widget.getSelectedChannels());
					thisWidget.refresh();
					thisWidget.notifyChangeListeners();
				});
				this.channelChooserWidget = widget;
			}
			return this.channelChooserWidget;
		},

		getTransformationChooserWidget : function() {
			if (this.transformationChooserWidget == null) {
				var thisWidget = this;
				var widget = new TransformationChooserWidget(this.imageInfo.imageDataset.imageDataset.imageParameters.channels);
				widget.setSelectedChannels(this.getChannelChooserWidget().getSelectedChannels());
				widget.addChangeListener(function() {
					thisWidget.refresh();
					thisWidget.notifyChangeListeners();
				});
				this.transformationChooserWidget = widget;
			}
			return this.transformationChooserWidget;
		},

		getResolutionChooserWidget : function() {
			if (this.resolutionChooserWidget == null) {
				var thisWidget = this;
				var widget = new ResolutionChooserWidget(this.imageResolutions);
				widget.addChangeListener(function() {
					thisWidget.refresh();
					thisWidget.notifyChangeListeners();
				});
				this.resolutionChooserWidget = widget;
			}
			return this.resolutionChooserWidget;
		},

		getChannelStackChooserWidget : function() {
			if (this.channelStackChooserWidget == null) {
				var thisWidget = this;
				var widget = new ChannelStackChooserWidget(this.imageInfo.channelStacks);
				widget.addChangeListener(function() {
					thisWidget.refresh();
					thisWidget.notifyChangeListeners();
				});
				this.channelStackChooserWidget = widget;
			}
			return this.channelStackChooserWidget;
		}

	});

	return ImageParametersWidget;

});