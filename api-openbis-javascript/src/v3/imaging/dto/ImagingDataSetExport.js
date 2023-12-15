define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetExport = function() {
	};
	stjs.extend(ImagingDataSetExport, null, [], function(constructor, prototype) {
		prototype['@type'] = 'imaging.dto.ImagingDataSetExport';
		constructor.serialVersionUID = 1;
		prototype.config = null;
		prototype.metadata = null;

		prototype.getConfig = function() {
			return this.config;
		};
		prototype.setConfig = function(config) {
			this.config = config;
		};
		prototype.getMetadata = function() {
			return this.metadata;
		};
		prototype.setMetadata = function(metadata) {
			this.metadata = metadata;
		};
		prototype.toString = function() {
            return "ImagingDataSetExport: " + this.config;
        };

	}, {
		config : {
            name : "Map",
            arguments : [ "String", "Serializable" ]
        },
        metadata : {
            name : "Map",
            arguments : [ "String", "String" ]
        }
	});
	return ImagingDataSetExport;
})