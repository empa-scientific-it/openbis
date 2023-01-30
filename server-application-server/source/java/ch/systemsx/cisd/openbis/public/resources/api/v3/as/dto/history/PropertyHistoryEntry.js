define([ "stjs", "util/Exceptions", "as/dto/history/HistoryEntry" ], function(stjs, exceptions, HistoryEntry) {
	var PropertyHistoryEntry = function() {
		HistoryEntry.call(this);
	};
	stjs.extend(PropertyHistoryEntry, HistoryEntry, [ HistoryEntry ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.history.PropertyHistoryEntry';
		constructor.serialVersionUID = 1;
		prototype.propertyName = null;
		prototype.propertyValue = null;

		prototype.getPropertyName = function() {
			return this.propertyName;
		};
		prototype.setPropertyName = function(propertyName) {
			this.propertyName = propertyName;
		};
		prototype.getPropertyValue = function() {
			return this.propertyValue;
		};
		prototype.setPropertyValue = function(propertyValue) {
			this.propertyValue = propertyValue;
		};
	}, {});
	return PropertyHistoryEntry;
})