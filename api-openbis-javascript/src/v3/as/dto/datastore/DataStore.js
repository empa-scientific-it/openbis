/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var DataStore = function() {
	};
	stjs.extend(DataStore, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.datastore.DataStore';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.code = null;
		prototype.downloadUrl = null;
		prototype.remoteUrl = null;
		prototype.registrationDate = null;
		prototype.modificationDate = null;

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
		prototype.getDownloadUrl = function() {
			return this.downloadUrl;
		};
		prototype.setDownloadUrl = function(downloadUrl) {
			this.downloadUrl = downloadUrl;
		};
		prototype.getRemoteUrl = function() {
			return this.remoteUrl;
		};
		prototype.setRemoteUrl = function(remoteUrl) {
			this.remoteUrl = remoteUrl;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
		};
		prototype.toString = function() {
			return "DataStore " + this.code;
		};
	}, {
		fetchOptions : "DataStoreFetchOptions",
		registrationDate : "Date",
		modificationDate : "Date"
	});
	return DataStore;
})