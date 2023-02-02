define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/ChannelStackChooserView",
		"components/imageviewer/ChannelStackMatrixChooserWidget", "components/imageviewer/ChannelStackSeriesChooserWidget",
		"components/imageviewer/ChannelStackManager" ], function($, AbstractWidget, ChannelStackChooserView, ChannelStackMatrixChooserWidget,
		ChannelStackSeriesChooserWidget, ChannelStackManager) {

	//
	// CHANNEL STACK CHOOSER WIDGET
	//

	function ChannelStackChooserWidget(channelStacks) {
		this.init(channelStacks);
	}

	$.extend(ChannelStackChooserWidget.prototype, AbstractWidget.prototype, {

		init : function(channelStacks) {
			AbstractWidget.prototype.init.call(this, new ChannelStackChooserView(this));
			this.channelStackManager = new ChannelStackManager(channelStacks);

			if (this.channelStackManager.isMatrix()) {
				this.widget = new ChannelStackMatrixChooserWidget(channelStacks);
			} else {
				this.widget = new ChannelStackSeriesChooserWidget(channelStacks);
			}

			var thisWidget = this;

			this.widget.addChangeListener(function() {
				thisWidget.notifyChangeListeners();
			});
		},

		getState : function() {
			return this.widget.getState();
		},

		setState : function(state) {
			this.widget.setState(state);
		},

		getChannelStackContentLoader : function() {
			return this.widget.getChannelStackContentLoader();
		},

		setChannelStackContentLoader : function(channelStackContentLoader) {
			this.widget.setChannelStackContentLoader(channelStackContentLoader);
		},

		getSelectedChannelStackId : function() {
			return this.widget.getSelectedChannelStackId();
		},

		setSelectedChannelStackId : function(channelStackId) {
			this.widget.setSelectedChannelStackId(channelStackId);
		},

		getWidget : function() {
			return this.widget;
		}

	});

	return ChannelStackChooserWidget;

});
