define(
		[ "jquery", "components/common/DelayedExecutor", "components/imageviewer/AbstractWidget", "components/imageviewer/TransformationChooserView" ],
		function($, DelayedExecutor, AbstractWidget, TransformationChooserView) {

			//
			// TRANSFORMATION CHOOSER WIDGET
			//

			function TransformationChooserWidget(channels) {
				this.init(channels);
			}

			$.extend(TransformationChooserWidget.prototype, AbstractWidget.prototype, {

				init : function(channels) {
					AbstractWidget.prototype.init.call(this, new TransformationChooserView(this));
					this.delayedExecutor = new DelayedExecutor(500);
					this.setChannels(channels);
				},

				doGetState : function(state) {
					state.selectedTransformation = this.getSelectedTransformation();
					state.userDefinedTransformationParametersMap = this.getUserDefinedTransformationParametersMap();
				},

				doSetState : function(state) {
					this.setSelectedTransformation(state.selectedTransformation);
					this.setUserDefinedTransformationParametersMap(state.userDefinedTransformationParametersMap);
				},

				getSelectedChannels : function() {
					if (this.selectedChannels) {
						return this.selectedChannels;
					} else {
						return [];
					}
				},

				setSelectedChannels : function(channels) {
					var thisWidget = this;

					if (!channels) {
						channels = [];
					}

					channels = channels.filter(function(channel) {
						return $.inArray(channel, thisWidget.getChannelsCodes()) != -1;
					});

					if (this.getSelectedChannels().toString() != channels.toString()) {
						this.selectedChannels = channels;
						this.setSelectedTransformation(this.getSelectedTransformation());
						this.refresh();
						this.notifyChangeListeners();
					}
				},

				getSelectedTransformation : function() {
					if (this.selectedTransformation) {
						return this.selectedTransformation;
					} else {
						return null;
					}
				},

				setSelectedTransformation : function(transformation) {
					var transformations = this.getTransformationsCodes();

					if (transformation == null || $.inArray(transformation, transformations) == -1) {
						transformation = transformations.length > 0 ? transformations[0] : null;
					}

					if (this.getSelectedTransformation() != transformation) {
						this.selectedTransformation = transformation;
						this.refresh();
						this.notifyChangeListeners();
					}
				},

				getUserDefinedTransformationParameters : function(channel) {
					var parameters = this.getUserDefinedTransformationParametersMap()[channel];
					if (parameters) {
						return parameters;
					} else {
						return null;
					}
				},

				setUserDefinedTransformationParameters : function(channel, parameters, delayed) {
					var thisWidget = this;

					var currentParameters = this.getUserDefinedTransformationParameters(channel);

					if (currentParameters.min != parameters.min || currentParameters.max != parameters.max
							|| currentParameters.blackpoint != parameters.blackpoint || currentParameters.whitepoint != parameters.whitepoint) {
						this.getUserDefinedTransformationParametersMap()[channel] = parameters;
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

				getUserDefinedTransformationParametersMap : function() {
					var thisWidget = this;

					if (!this.userDefinedTransformationParametersMap) {
						this.userDefinedTransformationParametersMap = {};
					}

					this.getSelectedChannels().forEach(function(channel) {
						var parameters = thisWidget.userDefinedTransformationParametersMap[channel];
						if (!parameters) {
							parameters = {
								"min" : 0,
								"max" : 255,
								"blackpoint" : 0,
								"whitepoint" : 255
							}
							thisWidget.userDefinedTransformationParametersMap[channel] = parameters;
						}
					});

					return this.userDefinedTransformationParametersMap;
				},

				setUserDefinedTransformationParametersMap : function(map) {
					this.userDefinedTransformationParametersMap = map;
					this.refresh();
					this.notifyChangeListeners();
				},

				getChannels : function() {
					if (this.channels) {
						return this.channels;
					} else {
						return [];
					}
				},

				getChannelsCodes : function() {
					return this.getChannels().map(function(channel) {
						return channel.code;
					});
				},

				setChannels : function(channels) {
					this.channels = channels;
				},

				getChannelsMap : function() {
					if (this.channelMap == null) {
						var map = {};
						this.getChannels().forEach(function(channel) {
							map[channel.code] = channel;
						});
						this.channelMap = map;
					}
					return this.channelMap;
				},

				getTransformations : function() {
					var selectedChannels = this.getSelectedChannels();

					if (selectedChannels.length == 0) {
						return [];
					} else if (selectedChannels.length == 1) {
						var list = [];

						list.push(this.getOptimalTransformation());
						list.push(this.getUserDefinedTransformation());

						var channel = this.getChannelsMap()[selectedChannels[0]];

						if (channel && channel.availableImageTransformations) {
							channel.availableImageTransformations.forEach(function(transformation) {
								list.push(transformation);
							});
						}

						return list;
					} else {
						return [ this.getOptimalTransformation(), this.getUserDefinedTransformation() ];
					}
				},

				getTransformationsCodes : function() {
					return this.getTransformations().map(function(transformation) {
						return transformation.code;
					});
				},

				getOptimalTransformation : function() {
					return {
						code : "$DEFAULT$",
						label : "Optimal image"
					}
				},

				isOptimalTransformation : function() {
					return this.getSelectedTransformation() == this.getOptimalTransformation().code;
				},

				getUserDefinedTransformation : function() {
					return {
						code : "$USER_DEFINED_RESCALING$",
						label : "User defined"
					}
				},

				isUserDefinedTransformation : function() {
					return this.getSelectedTransformation() == this.getUserDefinedTransformation().code;
				},

			});

			return TransformationChooserWidget;

		});