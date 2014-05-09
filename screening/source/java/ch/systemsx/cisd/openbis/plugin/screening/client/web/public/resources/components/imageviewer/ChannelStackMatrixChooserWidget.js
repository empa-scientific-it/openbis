define([ "jquery", "bootstrap", "bootstrap-slider", "components/imageviewer/AbstractView", "components/imageviewer/AbstractWidget",
		"components/imageviewer/ChannelStackManager", "components/imageviewer/MovieButtonsWidget" ], function($, bootstrap, bootstrapSlider,
		AbstractView, AbstractWidget, ChannelStackManager, MovieButtonsWidget) {

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

			var slidersRow = $("<div>").addClass("row").appendTo(this.panel);
			$("<div>").addClass("col-md-6").append(this.createTimePointWidget()).appendTo(slidersRow);
			$("<div>").addClass("col-md-6").append(this.createDepthWidget()).appendTo(slidersRow);

			var buttonsRow = $("<div>").appendTo(this.panel);
			buttonsRow.append(this.createTimePointButtonsWidget());

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			var time = this.controller.getSelectedTimePoint();
			var timeLabel = this.panel.find(".timePointWidget label");
			var timeInput = this.panel.find(".timePointWidget input");

			if (time != null) {
				var timeCount = this.controller.getTimePoints().length;
				var timeIndex = this.controller.getTimePoints().indexOf(time);

				timeLabel.text("Time: " + time + " sec (" + (timeIndex + 1) + "/" + timeCount + ")");
				timeInput.slider("setValue", time);

				this.timePointButtons.setSelectedFrame(timeIndex);
			}

			var depth = this.controller.getSelectedDepth();
			var depthLabel = this.panel.find(".depthWidget label");
			var depthInput = this.panel.find(".depthWidget input");

			if (depth != null) {
				var depthCount = this.controller.getDepths().length;
				var depthIndex = this.controller.getDepths().indexOf(depth);

				depthLabel.text("Depth: " + depth + " (" + (depthIndex + 1) + "/" + depthCount + ")");
				depthInput.slider("setValue", depthIndex);
			}
		},

		createTimePointWidget : function() {
			var thisView = this;
			var widget = $("<div>").addClass("timePointWidget").addClass("form-group");

			$("<label>").attr("for", "timePointInput").appendTo(widget);

			var timeInput = $("<input>").attr("id", "timePointInput").attr("type", "text").addClass("form-control");

			$("<div>").append(timeInput).appendTo(widget);

			timeInput.slider({
				"min" : 0,
				"max" : this.controller.getTimePoints().length - 1,
				"step" : 1,
				"tooltip" : "hide"
			}).on("slide", function(event) {
				if (!$.isArray(event.value) && !isNaN(event.value)) {
					var timeIndex = parseInt(event.value);
					var time = thisView.controller.getTimePoints()[timeIndex];
					thisView.controller.setSelectedTimePoint(time);
				}
			});

			return widget;
		},

		createDepthWidget : function() {
			var thisView = this;
			var widget = $("<div>").addClass("depthWidget").addClass("form-group");

			$("<label>").attr("for", "depthInput").appendTo(widget);

			var depthInput = $("<input>").attr("id", "depthInput").attr("type", "text").addClass("form-control");

			$("<div>").append(depthInput).appendTo(widget);

			depthInput.slider({
				"min" : 0,
				"max" : this.controller.getDepths().length - 1,
				"step" : 1,
				"tooltip" : "hide"
			}).on("slide", function(event) {
				if (!$.isArray(event.value) && !isNaN(event.value)) {
					var depthIndex = parseInt(event.value);
					var depth = thisView.controller.getDepths()[depthIndex];
					thisView.controller.setSelectedDepth(depth);
				}
			});

			return widget;
		},

		createTimePointButtonsWidget : function() {
			var thisView = this;

			var buttons = new MovieButtonsWidget(this.controller.getTimePoints().length);

			buttons.setFrameContentLoader(function(frameIndex, callback) {
				var timePoint = thisView.controller.getTimePoints()[frameIndex];
				var depth = thisView.controller.getSelectedDepth();
				var channelStack = thisView.controller.getChannelStackByTimePointAndDepth(timePoint, depth);
				thisView.controller.loadChannelStackContent(channelStack, callback);
			});

			buttons.addChangeListener(function() {
				var timePoint = thisView.controller.getTimePoints()[buttons.getSelectedFrame()];
				thisView.controller.setSelectedTimePoint(timePoint);
			});

			this.timePointButtons = buttons;
			return buttons.render();
		}

	});

	//
	// CHANNEL STACK MATRIX CHOOSER
	//

	function ChannelStackMatrixChooserWidget(channelStacks) {
		this.init(channelStacks);
	}

	$.extend(ChannelStackMatrixChooserWidget.prototype, AbstractWidget.prototype, {

		init : function(channelStacks) {
			AbstractWidget.prototype.init.call(this, new ChannelStackMatrixChooserView(this));
			this.channelStackManager = new ChannelStackManager(channelStacks);
		},

		getTimePoints : function() {
			return this.channelStackManager.getTimePoints();
		},

		getDepths : function() {
			return this.channelStackManager.getDepths();
		},

		getChannelStacks : function() {
			return this.channelStackManager.getChannelStacks();
		},

		getChannelStackById : function(channelStackId) {
			return this.channelStackManager.getChannelStackById(channelStackId);
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
			return this.selectedChannelStackId;
		},

		setSelectedChannelStackId : function(channelStackId) {
			if (this.selectedChannelStackId != channelStackId) {
				this.selectedChannelStackId = channelStackId;
				this.refresh();
				this.notifyChangeListeners();
			}
		},

		getSelectedChannelStack : function() {
			var channelStackId = this.getSelectedChannelStackId();

			if (channelStackId != null) {
				return this.channelStackManager.getChannelStackById(channelStackId);
			} else {
				return null;
			}
		},

		setSelectedChannelStack : function(channelStack) {
			if (channelStack != null) {
				this.setSelectedChannelStackId(channelStack.id);
			} else {
				this.setSelectedChannelStackId(null);
			}
		},

		getSelectedTimePoint : function() {
			var channelStack = this.getSelectedChannelStack();
			if (channelStack != null) {
				return channelStack.timePointOrNull;
			} else {
				return null;
			}
		},

		setSelectedTimePoint : function(timePoint) {
			if (timePoint != null && this.getSelectedDepth() != null) {
				var channelStack = this.channelStackManager.getChannelStackByTimePointAndDepth(timePoint, this.getSelectedDepth());
				this.setSelectedChannelStack(channelStack);
			} else {
				this.setSelectedChannelStack(null);
			}
		},

		getSelectedDepth : function() {
			var channelStack = this.getSelectedChannelStack();
			if (channelStack != null) {
				return channelStack.depthOrNull;
			} else {
				return null;
			}
		},

		setSelectedDepth : function(depth) {
			if (depth != null && this.getSelectedTimePoint() != null) {
				var channelStack = this.channelStackManager.getChannelStackByTimePointAndDepth(this.getSelectedTimePoint(), depth);
				this.setSelectedChannelStack(channelStack);
			} else {
				this.setSelectedChannelStack(null);
			}
		}

	});

	return ChannelStackMatrixChooserWidget;

});
