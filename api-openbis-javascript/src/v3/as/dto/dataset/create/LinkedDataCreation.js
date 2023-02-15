define([ "stjs" ], function(stjs) {
	var LinkedDataCreation = function() {
	};
	stjs.extend(LinkedDataCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.create.LinkedDataCreation';
		constructor.serialVersionUID = 1;
		prototype.externalCode = null;
		prototype.externalDmsId = null;
		prototype.contentCopies = null;

		prototype.getExternalCode = function() {
			return this.externalCode;
		};
		prototype.setExternalCode = function(externalCode) {
			this.externalCode = externalCode;
		};
		
		prototype.getExternalDmsId = function() {
			return this.externalDmsId;
		};
		prototype.setExternalDmsId = function(externalDmsId) {
			this.externalDmsId = externalDmsId;
		};
		
		prototype.getContentCopies = function() {
			return this.contentCopies;
		};
		prototype.setContentCopies = function(contentCopies) {
			this.contentCopies = contentCopies;
		};
	}, {
		externalDmsId : "IExternalDmsId",
		contentCopies : {
			name : "List",
			arguments : [ "Object" ]
		}
	});
	return LinkedDataCreation;
})		
