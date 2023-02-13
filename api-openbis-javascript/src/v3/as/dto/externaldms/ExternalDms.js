/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ExternalDms = function() {
	};
	stjs.extend(ExternalDms, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.ExternalDms';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.code = null;
		prototype.label = null;
		prototype.urlTemplate = null;
		prototype.openbis = null;
		prototype.address = null
		prototype.addressType = null

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
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
		prototype.getUrlTemplate = function() {
			return this.urlTemplate;
		};
		prototype.setUrlTemplate = function(urlTemplate) {
			this.urlTemplate = urlTemplate;
		};
		prototype.isOpenbis = function() {
			return this.openbis;
		};
		prototype.setOpenbis = function(openbis) {
			this.openbis = openbis;
		};
		prototype.getAddress = function() {
			return this.address;
		};
		prototype.setAddress = function(address) {
			this.address = address;
		};		
		prototype.getAddressType = function() {
			return this.addressType;
		};
		prototype.setAddressType = function(addressType) {
			this.addressType = addressType;
		};		
		prototype.toString = function() {
			return "ExternalDms " + this.code;
		};
	}, {
		permId : "ExternalDmsPermId",
		fetchOptions : "ExternalDmsFetchOptions",
		addressType : "ExternalDmsAddressType"
	});
	return ExternalDms;
})