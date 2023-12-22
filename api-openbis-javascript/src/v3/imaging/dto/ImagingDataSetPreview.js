define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetPreview = function() {
	};
	stjs.extend(ImagingDataSetPreview, null, [], function(constructor, prototype) {
		prototype['@type'] = 'imaging.dto.ImagingDataSetPreview';
		constructor.serialVersionUID = 1;
		prototype.config = null;
		prototype.format = null;
		prototype.bytes = null;
		prototype.show = null;
		prototype.width = null;
		prototype.height = null;
		prototype.index = null;
		prototype.metadata = null;

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
		prototype.getWidth = function() {
            return this.bytes;
        };
        prototype.setWidth = function(width) {
            this.width = width;
        };
        prototype.getHeight = function() {
            return this.height;
        };
        prototype.setHeight = function(height) {
            this.height = height;
        };
        prototype.getIndex = function() {
            return this.index;
        };
        prototype.setIndex = function(index) {
            this.index = index;
        };
		prototype.getMetadata = function() {
			return this.metadata;
		};
		prototype.setMetadata = function(metadata) {
			this.metadata = metadata;
		};
		prototype.toString = function() {
            return "ImagingDataSetPreview: " + this.config;
        };

	}, {
		config : {
            name : "Map",
            arguments : [ "String", "Serializable" ]
        },
        metadata : {
            name : "Map",
            arguments : [ "String", "Serializable" ]
        }
	});
	return ImagingDataSetPreview;
})