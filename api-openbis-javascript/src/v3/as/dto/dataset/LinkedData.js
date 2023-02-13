/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var LinkedData = function() {
	};
	stjs.extend(LinkedData, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.LinkedData';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.externalCode = null;
		prototype.externalDms = null;
		prototype.contentCopies = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getExternalCode = function() {
			return this.externalCode;
		};
		prototype.setExternalCode = function(externalCode) {
			this.externalCode = externalCode;
		};
		prototype.getExternalDms = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasExternalDms()) {
				return this.externalDms;
			} else {
				throw new exceptions.NotFetchedException("External data management system has not been fetched.");
			}
		};
		prototype.setExternalDms = function(externalDms) {
			this.externalDms = externalDms;
		};
		prototype.getContentCopies = function() {
			return this.contentCopies;
		};
		prototype.setContentCopies = function(contentCopies) {
			this.contentCopies = contentCopies;
		};
	}, {
		fetchOptions : "LinkedDataFetchOptions",
		externalDms : "ExternalDms",
		contentCopies : {
			name : "List",
			arguments : [ "ContentCopy" ]
		}
	});
	return LinkedData;
})