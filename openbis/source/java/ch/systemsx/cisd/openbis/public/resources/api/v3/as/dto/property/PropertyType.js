define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var PropertyType = function() {
	};
	stjs.extend(PropertyType, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.PropertyType';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.code = null;
		prototype.label = null;
		prototype.description = null;
		prototype.managedInternally = null;
		prototype.internalNameSpace = null;
		prototype.dataType = null;
		prototype.vocabulary = null;
		prototype.materialType = null;
		prototype.schema = null;
		prototype.transformation = null;
		prototype.registrator = null;
		prototype.registrationDate = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.isManagedInternally = function() {
			return this.managedInternally;
		};
		prototype.setManagedInternally = function(managedInternally) {
			this.managedInternally = managedInternally;
		};
		prototype.isInternalNameSpace = function() {
			return this.internalNameSpace;
		};
		prototype.setInternalNameSpace = function(internalNameSpace) {
			this.internalNameSpace = internalNameSpace;
		};
		prototype.getDataType = function() {
			return this.dataType;
		};
		prototype.setDataType = function(dataType) {
			this.dataType = dataType;
		};
		prototype.getVocabulary = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasVocabulary()) {
				return this.vocabulary;
			} else {
				throw new exceptions.NotFetchedException("Vocabulary has not been fetched.");
			}
		};
		prototype.setVocabulary = function(vocabulary) {
			this.vocabulary = vocabulary;
		};
		prototype.getMaterialType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasMaterialType()) {
				return this.materialType;
			} else {
				throw new exceptions.NotFetchedException("Material type has not been fetched.");
			}
		};
		prototype.setMaterialType = function(materialType) {
			this.materialType = materialType;
		};
		prototype.getSchema = function() {
			return this.schema;
		};
		prototype.setSchema = function(schema) {
			this.schema = schema;
		};
		prototype.getTransformation = function() {
			return this.transformation;
		};
		prototype.setTransformation = function(transformation) {
			this.transformation = transformation;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
	}, {
		fetchOptions : "PropertyTypeFetchOptions",
		dataType : "DataType",
		vocabulary : "Vocabulary",
		materialType : "MaterialType",
		registrator : "Person",
		registrationDate : "Date"
	});
	return PropertyType;
})