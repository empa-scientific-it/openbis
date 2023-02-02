define([ "jquery" ], function($) {

	//
	// IMAGE LOADER
	//

	function ImageLoader() {
		this.init();
	}

	$.extend(ImageLoader.prototype, {

		init : function() {
			this.callbacks = [];
		},

		loadImage : function(imageData, callback) {
			var thisLoader = this;

			var url = imageData.dataStoreUrl + "/datastore_server_screening";
			url += "?sessionID=" + imageData.sessionToken;
			url += "&dataset=" + imageData.dataSetCode;
			url += "&channelStackId=" + imageData.channelStackId;

			imageData.channels.forEach(function(channel) {
				url += "&channel=" + channel;
			});

			var width = 480;
			var height = 480;

			if (imageData.resolution) {
				var index = imageData.resolution.indexOf("x");
				width = imageData.resolution.substring(0, index);
				height = imageData.resolution.substring(index + 1);
			}

			url += "&mode=thumbnail" + width + "x" + height;

			if (imageData.transformation) {

				if ("$USER_DEFINED_RESCALING$" == imageData.transformation) {
					var parametersMap = imageData.userDefinedTransformationParametersMap;
					var multipleChannels = imageData.channels.length > 1;

					for (channel in parametersMap) {
						if ($.inArray(channel, imageData.channels) != -1) {
							var blackPoint = parametersMap[channel].blackpoint;
							var whitePoint = parametersMap[channel].whitepoint;
							url += "&transformation";
							if (multipleChannels) {
								url += channel;
							}
							url += "=" + encodeURIComponent(imageData.transformation + "(" + blackPoint + "," + whitePoint + ")");
						}
					}

				} else {
					url += "&transformation=" + encodeURIComponent(imageData.transformation);
				}
			}

			if (callback) {
				this.callbacks.push(callback);
			}

			if (this.timeoutConfig) {
				clearTimeout(this.timeoutConfig.id);
			}

			var timeoutConfig = {};
			var timeout = function() {

				$("<img>").attr("width", width).attr("src", timeoutConfig.url).on("error load", function(e) {
					if (thisLoader.timeoutConfig && thisLoader.timeoutConfig.id === timeoutConfig.id) {
						var thisImage = this;

						thisLoader.callbacks.forEach(function(callback) {
							callback(e.type === "error" ? "Image not available or can not be rendered." : thisImage);
						});

						thisLoader.timeoutConfig = null;
						thisLoader.callbacks = [];
					}
				});
			};

			timeoutConfig.id = setTimeout(timeout, 50);
			timeoutConfig.url = url;
			this.timeoutConfig = timeoutConfig;
		}

	});

	return ImageLoader;

});