define([ "require", "stjs", "util/Exceptions", "as/dto/common/search/AbstractSearchCriteria" ], function(require, stjs, exceptions, AbstractSearchCriteria) {
	var ObjectKindCriteria = function() {
		this.objectKinds = [];
	};

	stjs.extend(ObjectKindCriteria, AbstractSearchCriteria, [ AbstractSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.objectkindmodification.search.ObjectKindCriteria';
		constructor.serialVersionUID = 1;
		prototype.objectKinds = null;

		prototype.thatIn = function(objectKinds) {
			this.objectKinds = objectKinds;
		};
		prototype.getObjectKinds = function() {
			return this.objectKinds;
		};
		prototype.toString = function() {
			return "with object kinds " + this.objectKinds;
		}
	}, {
		objectKinds: {
			name: "List",
			arguments: ["ObjectKind"]
		}
	});

	return ObjectKindCriteria;
})
