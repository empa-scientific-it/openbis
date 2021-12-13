/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var ExternalDmsUpdate = function() {
		this.label = new FieldUpdateValue();
		this.address = new FieldUpdateValue();
	};
	stjs.extend(ExternalDmsUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.update.ExternalDmsUpdate';
		constructor.serialVersionUID = 1;
		prototype.externalDmsId = null;
		prototype.label = null;
		prototype.address = null;

		prototype.getObjectId = function() {
			return this.getExternalDmsId();
		};
		prototype.getExternalDmsId = function() {
			return this.externalDmsId;
		};
		prototype.setExternalDmsId = function(externalDmsId) {
			this.externalDmsId = externalDmsId;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label.setValue(label);
		};
		prototype.getAddress = function() {
			return this.address;
		};
		prototype.setAddress = function(address) {
			this.address.setValue(address);
		};
	}, {
		externalDmsId : "IExternalDmsId",
		label: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		},
		address: {
			name: "FieldUpdateValue",
			arguments: ["String"]
		}
	});
	return ExternalDmsUpdate;
})