define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetPropertyConfig = function() {
	};
	stjs.extend(ImagingDataSetPropertyConfig, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.imaging.ImagingDataSetPropertyConfig';
		constructor.serialVersionUID = 1;
		prototype.config = null;
		prototype.images = null;

		prototype.getConfig = function() {
			return this.config;
		};
		prototype.setConfig = function(config) {
			this.config = config;
		};
		prototype.getImages = function() {
            return this.images;
        };
        prototype.setSection = function(images) {
            this.images = images;
        };

		prototype.toString = function() {
            return "ImagingDataSetPropertyConfig: " + this.label;
        };

	}, {
		images : {
            name : "List",
            arguments : [ "ImagingDataSetImage"]
        }
	});
	return ImagingDataSetControl;
})