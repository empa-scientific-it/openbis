define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var PropertyType = function() {
	};
	stjs.extend(PropertyType, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.PropertyType';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.code = null;
		prototype.permId = null;
		prototype.label = null;
		prototype.description = null;
		prototype.managedInternally = null;
		prototype.dataType = null;
		prototype.vocabulary = null;
		prototype.materialType = null;
		prototype.sampleType = null;
		prototype.schema = null;
		prototype.transformation = null;
		prototype.semanticAnnotations = null;
		prototype.registrator = null;
		prototype.registrationDate = null;
		prototype.metaData = null;
		prototype.multiValue = null;

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
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
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
			return this.isManagedInternally();
		};
		prototype.setInternalNameSpace = function(internalNameSpace) {
			this.setManagedInternally(internalNameSpace);
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
		prototype.getSampleType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSampleType()) {
				return this.sampleType;
			} else {
				throw new exceptions.NotFetchedException("Sample type has not been fetched.");
			}
		};
		prototype.setSampleType = function(sampleType) {
			this.sampleType = sampleType;
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
		prototype.getSemanticAnnotations = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSemanticAnnotations()) {
				return this.semanticAnnotations;
			} else {
				throw new exceptions.NotFetchedException("Semantic annotations have not been fetched.");
			}
		};
		prototype.setSemanticAnnotations = function(semanticAnnotations) {
			this.semanticAnnotations = semanticAnnotations;
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
		prototype.getMetaData = function() {
			return this.metaData;
		};
		prototype.setMetaData = function(metaData) {
			this.metaData = metaData;
		};
		prototype.isMultiValue = function() {
			return this.multiValue;
		};
		prototype.setMultiValue = function(multiValue) {
			this.multiValue = multiValue;
		};
	}, {
		fetchOptions : "PropertyTypeFetchOptions",
		permId : "PropertyTypePermId",
		dataType : "DataType",
		vocabulary : "Vocabulary",
		materialType : "MaterialType",
		sampleType : "SampleType",
		semanticAnnotations: {
			name: "List",
			arguments: ["SemanticAnnotation"]
		},
		registrator : "Person",
		registrationDate : "Date",
		metaData: {
			name: "Map",
			arguments: ["String", "String"]
		}
	});
	return PropertyType;
})