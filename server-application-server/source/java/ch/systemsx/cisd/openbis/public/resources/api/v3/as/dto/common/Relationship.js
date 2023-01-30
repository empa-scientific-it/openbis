define([ "stjs" ], function(stjs) {
	var Relationship = function() {
	};
	stjs.extend(Relationship, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.Relationship';
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
			if (this.childAnnotations === null) {
				this.childAnnotations = {};
			}
			this.childAnnotations[key] = value;
			return this;
		};
		prototype.getParentAnnotations = function() {
			return this.parentAnnotations;
		};
		prototype.setParentAnnotations = function(parentAnnotations) {
			this.parentAnnotations = parentAnnotations;
		};
		prototype.addParentAnnotation = function(key, value) {
			if (this.parentAnnotations === null) {
				this.parentAnnotations = {};
			}
			this.parentAnnotations[key] = value;
			return this;
		};
	}, {
	});
	return Relationship;
})
