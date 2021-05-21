define([ "stjs", "util/Exceptions", "as/dto/common/id/IObjectId" ], function(stjs, exceptions, IObjectId) {
	var UnknownRelatedObjectId = function(relatedObjectId, relationType) {
		this.relatedObjectId = relatedObjectId;
		this.relationType = relationType;
	};
	stjs.extend(UnknownRelatedObjectId, null, [ IObjectId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.history.id.UnknownRelatedObjectId';
		constructor.serialVersionUID = 1;
		prototype.relatedObjectId = null;
		prototype.relationType = null;
		prototype.getRelatedObjectId = function() {
			return this.relatedObjectId;
		};
        prototype.getRelationType = function() {
            return this.relationType;
        };
	}, {});
	return UnknownRelatedObjectId;
})