define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingExportContainer = function() {
	};
	stjs.extend(ImagingExportContainer, null, [], function(constructor, prototype) {
		prototype['@type'] = 'imaging.dto.ImagingExportContainer';
		constructor.serialVersionUID = 1;
		prototype.permId = null;
		prototype.type = null;
		prototype.error = null;
		prototype.index = null;
		prototype.export = null;
		prototype.url = null;

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
        prototype.getExport = function() {
            return this.export;
        };
        prototype.setExport = function(exportVar) {
            this.export = exportVar;
        };
        prototype.getUrl = function() {
            return this.url;
        };
        prototype.setUrl = function(url) {
            this.url = url;
        };
		prototype.toString = function() {
            return "ImagingExportContainer: " + this.permId;
        };

	}, {

	});
	return ImagingExportContainer;
})