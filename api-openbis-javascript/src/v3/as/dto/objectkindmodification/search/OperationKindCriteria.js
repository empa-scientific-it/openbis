define([ "require", "stjs", "util/Exceptions", "as/dto/common/search/AbstractSearchCriteria" ], function(require, stjs, exceptions, AbstractSearchCriteria) {
	var OperationKindCriteria = function() {
		this.operationKinds = [];
	};

	stjs.extend(OperationKindCriteria, AbstractSearchCriteria, [ AbstractSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.objectkindmodification.search.OperationKindCriteria';
		constructor.serialVersionUID = 1;
		prototype.operationKinds = null;

		prototype.thatIn = function(operationKinds) {
			this.operationKinds = operationKinds;
		};
		prototype.getOperationKinds = function() {
			return this.operationKinds;
		};
		prototype.toString = function() {
			return "with operation kinds " + this.operationKinds;
		}
	}, {
		operationKinds: {
			name: "List",
			arguments: ["OperationKind"]
		}
	});

	return OperationKindCriteria;
})
