define([ "jquery", "components/common/Logger", "components/common/CallbackManager", "components/imageviewer/AbstractWidget", "components/imageviewer/ImageViewerView",
		"components/imageviewer/DataSetChooserWidget", "components/imageviewer/ImageParametersWidget", "components/imageviewer/ImageWidget", "components/imageviewer/ImageLoader",
		"components/imageviewer/ImageData", "components/imageviewer/OpenbisFacade" ], function($, Logger, CallbackManager, AbstractWidget, ImageViewerView, DataSetChooserWidget,
		ImageParametersWidget, ImageWidget, ImageLoader, ImageData, OpenbisFacade) {

	//
	// IMAGE VIEWER WIDGET
	//

	function ImageViewerWidget(openbis, dataSetCodes) {
		this.init(openbis, dataSetCodes);
	}

	$.extend(ImageViewerWidget.prototype, AbstractWidget.prototype, {
		init : function(openbis, dataSetCodes) {
			AbstractWidget.prototype.init.call(this, new ImageViewerView(this));
			this.facade = new OpenbisFacade(openbis);
			this.dataSetCodes = dataSetCodes;
			this.dataSetCodeToDataStoreUrlMap = {};
			this.dataSetCodeToImageInfoMap = {};
			this.dataSetCodeToImageResolutionsMap = {};
			this.imageViewerMap = {};
		},

		getSessionToken : function() {
			var dfd = $.Deferred();
			dfd.resolve(this.facade.getSession());
			return dfd.promise();
		},

		getImageLoader : function() {
			var dfd = $.Deferred();
			if (this.imageLoader == null) {
				this.imageLoader = new ImageLoader();
			}
			dfd.resolve(this.imageLoader);
			return dfd.promise();
		},

		getImageInfo : function(dataSetCode) {
			var thisWidget = this;
			var getter = function(dataSetCode) {
				return thisWidget.dataSetCodeToImageInfoMap[dataSetCode];
			};
			var setter = function(dataSetCode, value) {
				thisWidget.dataSetCodeToImageInfoMap[dataSetCode] = value;
			};
			var loader = function(dataSetCode, callback) {
				thisWidget.facade.getImageInfo(dataSetCode, callback);
			};
			return this.getOrLoadValue(dataSetCode, getter, setter, loader);
		},

		getImageResolutions : function(dataSetCode) {
			var thisWidget = this;
			var getter = function(dataSetCode) {
				return thisWidget.dataSetCodeToImageResolutionsMap[dataSetCode];
			};
			var setter = function(dataSetCode, value) {
				thisWidget.dataSetCodeToImageResolutionsMap[dataSetCode] = value;
			};
			var loader = function(dataSetCode, callback) {
				thisWidget.facade.getImageResolutions(dataSetCode, callback);
			};
			return this.getOrLoadValue(dataSetCode, getter, setter, loader);
		},

		getDataStoreUrl : function(dataSetCode) {
			var thisWidget = this;
			var getter = function(dataSetCode) {
				return thisWidget.dataSetCodeToDataStoreUrlMap[dataSetCode];
			};
			var setter = function(dataSetCode, value) {
				thisWidget.dataSetCodeToDataStoreUrlMap[dataSetCode] = value;
			};
			var loader = function(dataSetCode, callback) {
				thisWidget.facade.getDataStoreBaseURL(dataSetCode, callback);
			};
			return this.getOrLoadValue(dataSetCode, getter, setter, loader);
		},

		getDataSetCodes : function() {
			var dfd = $.Deferred();
			dfd.resolve(this.dataSetCodes);
			return dfd.promise();
		},

		getSelectedDataSetCode : function() {
			return this.getDataSetChooserWidget().then(function(widget) {
				return widget.getSelectedDataSetCode();
			});
		},

		setSelectedDataSetCode : function(dataSetCode) {
			return this.getDataSetChooserWidget().then(function(widget) {
				widget.setSelectedDataSetCode(dataSetCode);
			});
		},

		getSelectedImageParametersWidget : function() {
			var thisWidget = this;
			return this.getSelectedDataSetCode().then(function(dataSetCode) {
				return thisWidget.getImageParametersWidget(dataSetCode);
			});
		},

		getSelectedChannel : function() {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				return viewer.getChannelChooserWidget().getSelectedChannel()
			});
		},

		setSelectedChannel : function(channel) {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				viewer.getChannelChooserWidget().setSelectedChannel(channel);
			});
		},

		getSelectedMergedChannels : function() {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				return viewer.getChannelChooserWidget().getSelectedMergedChannels();
			});
		},

		setSelectedMergedChannels : function(channels) {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				viewer.getChannelChooserWidget().setSelectedMergedChannels(channels);
			});
		},

		getSelectedResolution : function() {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				return viewer.getResolutionChooserWidget().getSelectedResolution();
			});
		},

		setSelectedResolution : function(resolution) {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				viewer.getResolutionChooserWidget().setSelectedResolution(resolution);
			});
		},

		getSelectedTransformation : function() {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				return viewer.getTransformationChooserWidget().getSelectedTransformation();
			});
		},

		setSelectedTransformation : function(transformation) {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				viewer.getTransformationChooserWidget().setSelectedTransformation(transformation);
			});
		},

		getUserDefinedTransformationParameters : function(channel) {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				return viewer.getTransformationChooserWidget().getUserDefinedTransformationParameters(channel);
			});
		},

		setUserDefinedTransformationParameters : function(channel, parameters) {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				viewer.getTransformationChooserWidget().setUserDefinedTransformationParameters(channel, parameters);
			});
		},

		getUserDefinedTransformationParametersMap : function() {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				return viewer.getTransformationChooserWidget().getUserDefinedTransformationParametersMap();
			});
		},

		setUserDefinedTransformationParametersMap : function(parametersMap) {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				viewer.getTransformationChooserWidget().setUserDefinedTransformationParametersMap(parametersMap);
			});
		},

		getSelectedChannelStackId : function() {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				return viewer.getChannelStackChooserWidget().getSelectedChannelStackId();
			});
		},

		setSelectedChannelStackId : function(channelStackId) {
			return this.getSelectedImageParametersWidget().then(function(viewer) {
				viewer.getChannelStackChooserWidget().setSelectedChannelStackId(channelStackId);
			});
		},

		getSelectedImageData : function() {
			var thisWidget = this;
			var dfd = $.Deferred();

			$.when(this.getSelectedDataSetCode()).then(
					function(selectedDataSetCode) {
						$.when(thisWidget.getSessionToken(), thisWidget.getSelectedChannelStackId(), thisWidget.getSelectedChannel(), thisWidget.getSelectedMergedChannels(),
								thisWidget.getSelectedResolution(), thisWidget.getSelectedTransformation(), thisWidget.getUserDefinedTransformationParametersMap(),
								thisWidget.getDataStoreUrl(selectedDataSetCode)).then(
								function(sessionToken, selectedChannelStackId, selectedChannel, selectedMergedChannels, selectedResolution, selectedTransformation, userDefinedTransformation,
										dataStoreUrl) {

									var imageData = new ImageData();
									imageData.setSessionToken(sessionToken);
									imageData.setDataSetCode(selectedDataSetCode);
									imageData.setChannelStackId(selectedChannelStackId);

									if (selectedChannel) {
										imageData.setChannels([ selectedChannel ]);
									} else {
										imageData.setChannels(selectedMergedChannels);
									}
									imageData.setResolution(selectedResolution);
									imageData.setTransformation(selectedTransformation);
									imageData.setUserDefinedTransformationParametersMap(userDefinedTransformation);
									imageData.setDataStoreUrl(dataStoreUrl);

									dfd.resolve(imageData);

								});
					});

			return dfd.promise();
		},

		getDataSetChooserWidget : function() {
			var thisWidget = this;
			var getter = function(parameter) {
				return thisWidget.dataSetChooserWidget;
			};
			var setter = function(parameter, value) {
				thisWidget.dataSetChooserWidget = value;
			};
			var loader = function(dataSetCode, callback) {
				thisWidget.getDataSetCodes().then(
						function(dataSetCodes) {
							var widget = new DataSetChooserWidget(dataSetCodes);

							widget.addChangeListener(function(event) {
								$.when(thisWidget.getImageParametersWidget(event.getOldValue()), thisWidget.getImageParametersWidget(event.getNewValue()), thisWidget.getImageWidget()).then(
										function(oldWidget, newWidget, imageWidget) {
											newWidget.setState(oldWidget.getState());
											thisWidget.getSelectedImageData().then(function(imageData) {
												imageWidget.setImageData(imageData);
												thisWidget.refresh();
											});
										});
							});

							callback(widget);
						});
			};
			return this.getOrLoadValue(null, getter, setter, loader);
		},

		getImageParametersWidget : function(dataSetCode) {
			var thisWidget = this;
			var getter = function(dataSetCode) {
				return thisWidget.imageViewerMap[dataSetCode];
			};
			var setter = function(dataSetCode, value) {
				thisWidget.imageViewerMap[dataSetCode] = value;
			};
			var loader = function(dataSetCode, callback) {
				$.when(thisWidget.getImageInfo(dataSetCode), thisWidget.getImageResolutions(dataSetCode)).then(function(info, resolutions) {
					var widget = new ImageParametersWidget(info, resolutions);

					widget.getChannelStackChooserWidget().setChannelStackContentLoader(function(channelStack, callback) {
						$.when(thisWidget.getSelectedImageData(), thisWidget.getImageLoader()).then(function(imageData, imageLoader) {
							imageData.setChannelStackId(channelStack.id);
							imageLoader.loadImage(imageData, callback);
						});
					});

					widget.addChangeListener(function() {
						$.when(thisWidget.getImageWidget(), thisWidget.getSelectedImageData()).then(function(imageWidget, imageData) {
							imageWidget.setImageData(imageData);
						});
					});

					callback(widget);
				});
			};
			return this.getOrLoadValue(dataSetCode, getter, setter, loader);
		},

		getImageWidget : function() {
			var thisWidget = this;
			var getter = function(parameter) {
				return thisWidget.imageWidget;
			};
			var setter = function(parameter, value) {
				thisWidget.imageWidget = value;
			};
			var loader = function(parameter, callback) {
				$.when(thisWidget.getSelectedImageData(), thisWidget.getImageLoader()).then(function(imageData, imageLoader) {
					var widget = new ImageWidget(imageLoader);
					widget.setImageData(imageData);
					callback(widget);
				});
			};
			return this.getOrLoadValue(null, getter, setter, loader);
		},

		getOrLoadValue : function(parameter, getter, setter, loader) {
			var thisWidget = this;
			var dfd = $.Deferred();
			dfd.isDeferred = true;

			var value = getter(parameter);

			if (value) {
				if (value.isDeferred) {
					return value;
				} else {
					dfd.resolve(value);
				}
			} else {
				setter(parameter, dfd);
				loader(parameter, function(value) {
					setter(parameter, value);
					dfd.resolve(value);
				});
			}

			return dfd.promise();
		}

	});

	return ImageViewerWidget;

});