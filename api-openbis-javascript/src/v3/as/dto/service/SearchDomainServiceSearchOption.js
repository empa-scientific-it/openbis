define([ "stjs" ], function(stjs) {
	var SearchDomainServiceSearchOption = function() {
	};
	stjs.extend(SearchDomainServiceSearchOption, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.SearchDomainServiceSearchOption';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.label = null;
		prototype.description = null;
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		}
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		}
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.toString = function() {
			return this.label + " [" + this.code + "]";
		};
	}, {}
	);
	return SearchDomainServiceSearchOption;
})
