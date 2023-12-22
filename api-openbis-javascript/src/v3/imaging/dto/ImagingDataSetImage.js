define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetImage = function() {
	};
	stjs.extend(ImagingDataSetImage, null, [], function(constructor, prototype) {
		prototype['@type'] = 'imaging.dto.ImagingDataSetImage';
		constructor.serialVersionUID = 1;
		prototype.previews = null;
		prototype.config = null;
		prototype.index = null;
		prototype.metadata = null;

		prototype.getPreviews = function() {
			return this.previews;
		};
		prototype.setPreviews = function(previews) {
			this.previews = previews;
		};
		prototype.getConfig = function() {
            return this.config;
        };
        prototype.setConfig = function(config) {
            this.config = config;
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
            return "ImagingDataSetImage: " + this.previews;
        };

	}, {
		previews : {
            name : "List",
            arguments : [ "ImagingDataSetPreview"]
        },
        config : {
            name : "Map",
            arguments : [ "String", "Serializable" ]
        },
        metadata : {
            name : "Map",
            arguments : [ "String", "Serializable" ]
        }
	});
	return ImagingDataSetImage;
})