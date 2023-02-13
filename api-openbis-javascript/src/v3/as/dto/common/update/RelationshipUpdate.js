define([ "stjs", "as/dto/common/update/ListUpdateMapValues" ], function(stjs, ListUpdateMapValues) {
	var RelationshipUpdate = function() {
		this.childAnnotations = new ListUpdateMapValues();
		this.parentAnnotations = new ListUpdateMapValues();
	};
	stjs.extend(RelationshipUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.update.RelationshipUpdate';
		constructor.serialVersionUID = 1;
		prototype.childAnnotations = null;
		prototype.parentAnnotations = null;
		
		prototype.getChildAnnotations = function() {
			return this.childAnnotations;
		};
		prototype.setChildAnnotations = function(childAnnotations) {
			this.childAnnotations = childAnnotations;
		};
		prototype.addChildAnnotation = function(key, value) {
			this.childAnnotations.put(key, value);
			return this;
		};
		prototype.removeChildAnnotations = function(keys) {
			this.childAnnotations.remove(keys)
			return this;
		};
		prototype.getParentAnnotations = function() {
			return this.parentAnnotations;
		};
		prototype.setParentAnnotations = function(parentAnnotations) {
			this.parentAnnotations = parentAnnotations;
		};
		prototype.addParentAnnotation = function(key, value) {
			this.parentAnnotations.put(key, value);
			return this;
		};
		prototype.removeParentAnnotations = function(keys) {
			this.parentAnnotations.remove(keys)
			return this;
		};
		prototype.setRelationship = function(relationship) {
			this.childAnnotations.set(relationship.getChildAnnotations());
			this.parentAnnotations.set(relationship.getParentAnnotations());
		};
	}, {
		childAnnotations : "ListUpdateMapValues",
		parentAnnotations : "ListUpdateMapValues"
	});
	return RelationshipUpdate;
})
