define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ObjectKindModification = function() {
	};
	stjs.extend(ObjectKindModification, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.objectkindmodification.ObjectKindModification';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.objectKind = null;
		prototype.operationKind = null;
		prototype.lastModificationTimeStamp = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getObjectKind = function() {
			return this.objectKind;
		};
		prototype.setObjectKind = function(objectKind) {
			this.objectKind = objectKind;
		};
		prototype.getOperationKind = function() {
			return this.operationKind;
		};
		prototype.setOperationKind = function(operationKind) {
			this.operationKind = operationKind;
		};
		prototype.getLastModificationTimeStamp = function() {
			return this.lastModificationTimeStamp;
		};
		prototype.setLastModificationTimeStamp = function(lastModificationTimeStamp) {
			this.lastModificationTimeStamp = lastModificationTimeStamp;
		};
		prototype.toString = function() {
			return "Last " + this.operationKind + " operation of an object of kind " + this.objectKind 
					+ " occured at " +  this.lastModificationTimeStamp;
		};
	}, {
		fetchOptions : "ObjectKindModificationFetchOptions",
		objectKind : "ObjectKind",
		operationKind : "OperationKind",
		lastModificationTimeStamp : "Date"
	});
	return ObjectKindModification;
})
