define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetConfig = function() {
	};
	stjs.extend(ImagingDataSetConfig, null, [], function(constructor, prototype) {
		prototype['@type'] = 'imaging.dto.ImagingDataSetConfig';
		constructor.serialVersionUID = 1;
		prototype.adaptor = null;
		prototype.version = null;
		prototype.speeds = null;
		prototype.resolutions = null;
		prototype.playable = null;
		prototype.exports = null;
		prototype.inputs = null;
		prototype.metadata = null;

		prototype.getAdaptor = function() {
			return this.adaptor;
		};
		prototype.setAdaptor = function(adaptor) {
			this.adaptor = adaptor;
		};
		prototype.getVersion = function() {
            return this.version;
        };
        prototype.setVersion = function(version) {
            this.version = version;
        };
        prototype.getSpeeds = function() {
            return this.speeds;
        };
        prototype.setSpeeds = function(speeds) {
            this.speeds = speeds;
        };
        prototype.getResolutions = function() {
            return this.resolutions;
        };
        prototype.setResolutions = function(resolutions) {
            this.resolutions = resolutions;
        };
        prototype.getPlayable = function() {
            return this.playable;
        };
        prototype.setPlayable = function(playable) {
            this.playable = playable;
        };
        prototype.getExports = function() {
            return this.exports;
        };
        prototype.setExports = function(exports) {
            this.exports = exports;
        };
        prototype.getInputs = function() {
            return this.inputs;
        };
        prototype.setInputs = function(inputs) {
            this.inputs = inputs;
        };
		prototype.getMetadata = function() {
			return this.metadata;
		};
		prototype.setMetadata = function(metadata) {
			this.metadata = metadata;
		};
		prototype.toString = function() {
            return "ImagingDataSetConfig: " + this.adaptor;
        };

	}, {
        resolutions : {
            name : "List",
            arguments : [ "String"]
        },
        speeds : {
            name : "List",
            arguments : [ "Integer"]
        },
        exports : {
            name : "List",
            arguments : [ "ImagingDataSetControl"]
        },
        inputs : {
            name : "List",
            arguments : [ "ImagingDataSetControl"]
        },
        metadata : {
            name : "Map",
            arguments : [ "String", "String" ]
        }
	});
	return ImagingDataSetConfig;
})