define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetMultiExport = function() {
	};
	stjs.extend(ImagingDataSetMultiExport, null, [], function(constructor, prototype) {
		prototype['@type'] = 'imaging.dto.ImagingDataSetMultiExport';
		constructor.serialVersionUID = 1;
		prototype.permId = null;
        prototype.index = null;
		prototype.config = null;
		prototype.metadata = null;

        prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getIndex = function() {
            return this.index;
        };
        prototype.setIndex = function(index) {
            this.index = index;
        };
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
            arguments : [ "String", "Serializable" ]
        }
	});
	return ImagingDataSetMultiExport;
})