define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetImage = function() {
	};
	stjs.extend(ImagingDataSetImage, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.imaging.ImagingDataSetImage';
		constructor.serialVersionUID = 1;
		prototype.previews = null;
		prototype.metaData = null;

		prototype.getPreviews = function() {
			return this.previews;
		};
		prototype.setPreviews = function(previews) {
			this.previews = previews;
		};
		prototype.getMetaData = function() {
			return this.metaData;
		};
		prototype.setMetaData = function(metaData) {
			this.metaData = metaData;
		};
		prototype.toString = function() {
            return "ImagingDataSetImage: " + this.previews;
        };

	}, {
		previews : {
            name : "List",
            arguments : [ "ImagingDataSetPreview"]
        },
        metaData : {
            name : "Map",
            arguments : [ "String", "String" ]
        }
	});
	return ImagingDataSetImage;
})