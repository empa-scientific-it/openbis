define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetPreview = function() {
	};
	stjs.extend(ImagingDataSetPreview, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.imaging.ImagingDataSetPreview';
		constructor.serialVersionUID = 1;
		prototype.config = null;
		prototype.format = null;
		prototype.bytes = null;
		prototype.show = null;
		prototype.metaData = null;

		prototype.getConfig = function() {
			return this.config;
		};
		prototype.setConfig = function(config) {
			this.config = config;
		};
		prototype.getFormat = function() {
			return this.format;
		};
		prototype.setFormat = function(format) {
			this.format = format;
		};
		prototype.getBytes = function() {
			return this.bytes;
		};
		prototype.setBytes = function(bytes) {
			this.bytes = bytes;
		};
		prototype.isShow = function() {
			return this.show;
		};
		prototype.setShow = function(show) {
			this.show = show;
		};
		prototype.getMetaData = function() {
			return this.metaData;
		};
		prototype.setMetaData = function(metaData) {
			this.metaData = metaData;
		};

	}, {
		config : {
            name : "Map",
            arguments : [ "String", "List" ]
        },
        metaData : {
            name : "Map",
            arguments : [ "String", "String" ]
        }
	});
	return ImagingDataSetPreview;
})