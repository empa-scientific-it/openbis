/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var DataSetArchiveOptions = function() {
		this.removeFromDataStore = true;
		this.options = {};
	};
	stjs.extend(DataSetArchiveOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.archive.DataSetArchiveOptions';
		constructor.serialVersionUID = 1;
		prototype.removeFromDataStore = null;
		prototype.options = null;
		prototype.isRemoveFromDataStore = function() {
			return this.removeFromDataStore;
		};
		prototype.setRemoveFromDataStore = function(removeFromDataStore) {
			this.removeFromDataStore = removeFromDataStore;
		};
		prototype.withOption = function(option, value) {
			this.options[option] = value;
		}
		prototype.getOptions = function() {
			return this.options;
		}
	}, {
		options : {
			name : "Map",
			arguments : [ "String", "String"]
		}
	});
	return DataSetArchiveOptions;
})