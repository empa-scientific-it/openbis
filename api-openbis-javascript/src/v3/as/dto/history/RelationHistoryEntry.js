define([ "stjs", "util/Exceptions", "as/dto/history/HistoryEntry" ], function(stjs, exceptions, HistoryEntry) {
	var RelationHistoryEntry = function() {
		HistoryEntry.call(this);
	};
	stjs.extend(RelationHistoryEntry, HistoryEntry, [ HistoryEntry ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.history.RelationHistoryEntry';
		constructor.serialVersionUID = 1;
		prototype.relationType = null;
		prototype.relatedObjectId = null;

		prototype.getRelationType = function() {
			return this.relationType;
		};
		prototype.setRelationType = function(relationType) {
			this.relationType = relationType;
		};
		prototype.getRelatedObjectId = function() {
			return this.relatedObjectId;
		};
		prototype.setRelatedObjectId = function(relatedObjectId) {
			this.relatedObjectId = relatedObjectId;
		};
	}, {
		relationType : "IRelationType",
		relatedObjectId : "IObjectId"
	});
	return RelationHistoryEntry;
})