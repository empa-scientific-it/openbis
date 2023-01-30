/**
 * @author anttil
 */
define([ "stjs" ], function(stjs) {
	var ExternalDmsCreation = function() {
	};
	stjs.extend(ExternalDmsCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.create.ExternalDmsCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.label = null;
		prototype.address = null;
		prototype.addressType = null;
		prototype.creationId = null;

		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setAddress = function(address) {
			this.address = address;
		};
		prototype.getAddress = function() {
			return this.address;
		};
		prototype.setAddressType = function(addressType) {
			this.addressType = addressType;
		};
		prototype.getAddressType = function() {
			return this.addressType;
		};
		prototype.getCreationId = function() {
			return this.creationId;
		};
		prototype.setCreationId = function(creationId) {
			this.creationId = creationId;
		};
	}, {
		creationId : "CreationId",
		addressType : "ExternalDmsAddressType"
	});
	return ExternalDmsCreation;
})