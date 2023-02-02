define([ "jquery" ], function($) {

	//
	// IMAGE DATA
	//

	function ImageData() {
		this.init();
	}

	$.extend(ImageData.prototype, {

		init : function() {
		},

		setDataStoreUrl : function(dataStoreUrl) {
			this.dataStoreUrl = dataStoreUrl;
		},

		setSessionToken : function(sessionToken) {
			this.sessionToken = sessionToken;
		},

		setDataSetCode : function(dataSetCode) {
			this.dataSetCode = dataSetCode;
		},

		setChannelStackId : function(channelStackId) {
			this.channelStackId = channelStackId;
		},

		setChannels : function(channels) {
			this.channels = channels;
		},

		setResolution : function(resolution) {
			this.resolution = resolution;
		},

		setTransformation : function(transformation) {
			this.transformation = transformation;
		},

		setUserDefinedTransformationParametersMap : function(parametersMap) {
			this.userDefinedTransformationParametersMap = parametersMap;
		}

	// TODO add getters

	});

	return ImageData;

});