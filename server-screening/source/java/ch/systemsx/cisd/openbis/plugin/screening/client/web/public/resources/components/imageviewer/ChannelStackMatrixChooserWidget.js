define([ "jquery", "components/common/DelayedExecutor", "components/imageviewer/AbstractWidget", "components/imageviewer/ChannelStackMatrixChooserView", "components/imageviewer/MovieButtonsWidget",
		"components/imageviewer/ChannelStackManager" ], function($, DelayedExecutor, AbstractWidget, ChannelStackMatrixChooserView, MovieButtonsWidget, ChannelStackManager) {

	//
	// CHANNEL STACK MATRIX CHOOSER WIDGET
	//

	function ChannelStackMatrixChooserWidget(channelStacks) {
		this.init(channelStacks);
	}

	$.extend(ChannelStackMatrixChooserWidget.prototype, AbstractWidget.prototype, {

		init : function(channelStacks) {
			AbstractWidget.prototype.init.call(this, new ChannelStackMatrixChooserView(this));
			this.channelStackManager = new ChannelStackManager(channelStacks);
			this.delayedExecutor = new DelayedExecutor(500);

			if (channelStacks && channelStacks.length > 0) {
				this.setSelectedChannelStackId(channelStacks[0].id);
			}
		},

		doGetState : function(state) {
			state.timePointButtonsState = this.getTimePointButtonsWidget().getState();
			state.depthButtonsState = this.getDepthButtonsWidget().getState();
			state.selectedTimePoint = this.getSelectedTimePoint();
			state.selectedDepth = this.getSelectedDepth();
		},

		doSetState : function(state) {
			this.getTimePointButtonsWidget().setState(state.timePointButtonsState);
			this.getDepthButtonsWidget().setState(state.depthButtonsState);
			this.setSelectedTimePoint(state.selectedTimePoint);
			this.setSelectedDepth(state.selectedDepth);
		},

		getTimePoints : function() {
			return this.channelStackManager.getTimePoints();
		},

		getDepths : function() {
			return this.channelStackManager.getDepths();
		},

		getChannelStackByTimePointAndDepth : function(timePoint, depth) {
			return this.channelStackManager.getChannelStackByTimePointAndDepth(timePoint, depth);
		},

		loadChannelStackContent : function(channelStack, callback) {
			this.getChannelStackContentLoader()(channelStack, callback);
		},

		getChannelStackContentLoader : function() {
			if (this.channelStackContentLoader) {
				return this.channelStackContentLoader;
			} else {
				return function(channelStack, callback) {
					callback();
				}
			}
		},

		setChannelStackContentLoader : function(channelStackContentLoader) {
			this.channelStackContentLoader = channelStackContentLoader;
		},

		getSelectedChannelStackId : function() {
			var channelStack = this.getChannelStackByTimePointAndDepth(this.getSelectedTimePoint(), this.getSelectedDepth());
			return channelStack != null ? channelStack.id : null;
		},

		setSelectedChannelStackId : function(channelStackId) {
			var channelStack = this.channelStackManager.getChannelStackById(channelStackId);
			if (channelStack != null) {
				this.setSelectedTimePoint(channelStack.timePointOrNull);
				this.setSelectedDepth(channelStack.depthOrNull);
			}
		},

		getSelectedTimePoint : function() {
			if (this.selectedTimePoint != undefined) {
				return this.selectedTimePoint;
			} else {
				return null;
			}
		},

		setSelectedTimePoint : function(timePoint, delayed) {
			var thisWidget = this;

			if ($.inArray(timePoint, this.getTimePoints()) == -1) {
				timePoint = this.getTimePoints().length > 0 ? this.getTimePoints()[0] : null;
			}

			if (this.getSelectedTimePoint() != timePoint) {
				this.selectedTimePoint = timePoint;
				this.getTimePointButtonsWidget().setSelectedFrame(this.getTimePoints().indexOf(timePoint));
				this.refresh();

				if (delayed) {
					this.delayedExecutor.execute(function() {
						thisWidget.notifyChangeListeners();
					});
				} else {
					thisWidget.notifyChangeListeners();
				}
			}
		},

		getSelectedDepth : function() {
			if (this.selectedDepth != undefined) {
				return this.selectedDepth;
			} else {
				return null;
			}
		},

		setSelectedDepth : function(depth, delayed) {
			var thisWidget = this;

			if ($.inArray(depth, this.getDepths()) == -1) {
				depth = this.getDepths().length > 0 ? this.getDepths()[0] : null;
			}

			if (this.getSelectedDepth() != depth) {
				this.selectedDepth = depth;
				this.getDepthButtonsWidget().setSelectedFrame(this.getDepths().indexOf(depth));
				this.refresh();

				if (delayed) {
					this.delayedExecutor.execute(function() {
						thisWidget.notifyChangeListeners();
					});
				} else {
					thisWidget.notifyChangeListeners();
				}
			}
		},

		getTimePointButtonsWidget : function() {
			if (this.timeButtonsWidget == null) {
				var thisWidget = this;

				var widget = new MovieButtonsWidget(this.getTimePoints().length);

				widget.setFrameContentLoader(function(frameIndex, callback) {
					var timePoint = thisWidget.getTimePoints()[frameIndex];
					var depth = thisWidget.getSelectedDepth();
					var channelStack = thisWidget.getChannelStackByTimePointAndDepth(timePoint, depth);
					thisWidget.loadChannelStackContent(channelStack, callback);
				});

				widget.addChangeListener(function(event) {
					if (event.getField() == "frame") {
						var timePoint = thisWidget.getTimePoints()[widget.getSelectedFrame()];
						thisWidget.setSelectedTimePoint(timePoint);
					} else if (event.getField() == "visible") {
						thisWidget.refresh();
					}
				});

				this.timeButtonsWidget = widget;
			}

			return this.timeButtonsWidget;
		},

		getDepthButtonsWidget : function() {
			if (this.depthButtonsWidget == null) {
				var thisWidget = this;

				var widget = new MovieButtonsWidget(this.getDepths().length);

				widget.setFrameContentLoader(function(frameIndex, callback) {
					var depth = thisWidget.getDepths()[frameIndex];
					var timePoint = thisWidget.getSelectedTimePoint();
					var channelStack = thisWidget.getChannelStackByTimePointAndDepth(timePoint, depth);
					thisWidget.loadChannelStackContent(channelStack, callback);
				});

				widget.addChangeListener(function(event) {
					if (event.getField() == "frame") {
						var depth = thisWidget.getDepths()[widget.getSelectedFrame()];
						thisWidget.setSelectedDepth(depth);
					} else if (event.getField() == "visible") {
						thisWidget.refresh();
					}
				});

				this.depthButtonsWidget = widget;
			}

			return this.depthButtonsWidget;
		}

	});

	return ChannelStackMatrixChooserWidget;

});
