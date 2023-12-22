define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingPreviewContainer = function() {
	};
	stjs.extend(ImagingPreviewContainer, null, [], function(constructor, prototype) {
		prototype['@type'] = 'imaging.dto.ImagingPreviewContainer';
		constructor.serialVersionUID = 1;
		prototype.permId = null;
		prototype.type = null;
		prototype.error = null;
		prototype.index = null;
		prototype.preview = null;

		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getType = function() {
			return this.type;
		};
		prototype.setType = function(type) {
			this.type = type;
		};
		prototype.getError = function() {
			return this.error;
		};
		prototype.setError = function(error) {
			this.error = error;
		};
		prototype.getIndex = function() {
            return this.index;
        };
        prototype.setIndex = function(index) {
            this.index = index;
        };
        prototype.getPreview = function() {
            return this.preview;
        };
        prototype.setPreview = function(preview) {
            this.preview = preview;
        };
		prototype.toString = function() {
            return "ImagingPreviewContainer: " + this.permId;
        };

	}, {

	});
	return ImagingPreviewContainer;
})