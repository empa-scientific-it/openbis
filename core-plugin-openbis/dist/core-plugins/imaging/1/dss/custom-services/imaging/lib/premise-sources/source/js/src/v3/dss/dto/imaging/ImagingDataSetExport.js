define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetExport = function() {
	};
	stjs.extend(ImagingDataSetExport, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.imaging.ImagingDataSetExport';
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
		prototype.toString = function() {
            return "ImagingDataSetExport: " + this.config;
        };

	}, {
		config : {
            name : "Map",
            arguments : [ "String", "Serializable" ]
        },
        metaData : {
            name : "Map",
            arguments : [ "String", "String" ]
        }
	});
	return ImagingDataSetExport;
})