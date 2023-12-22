define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingMultiExportContainer = function() {
	};
	stjs.extend(ImagingMultiExportContainer, null, [], function(constructor, prototype) {
		prototype['@type'] = 'imaging.dto.ImagingMultiExportContainer';
		constructor.serialVersionUID = 1;
		prototype.type = null;
		prototype.error = null;
		prototype.exports = null;
		prototype.url = null;

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
        prototype.getExports = function() {
            return this.exports;
        };
        prototype.setExports = function(exports) {
            this.exports = exports;
        };
        prototype.getUrl = function() {
            return this.url;
        };
        prototype.setUrl = function(url) {
            this.url = url;
        };
		prototype.toString = function() {
            return "ImagingMultiExportContainer: ";
        };

	}, {
        exports : {
            name : "List",
            arguments : [ "ImagingDataSetMultiExport"]
        }
	});
	return ImagingMultiExportContainer;
})