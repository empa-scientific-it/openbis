/**
 * @author pkupczyk
 */
define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/SearchOperator",
		"as/dto/common/search/CodeSearchCriteria", "as/dto/common/search/CodesSearchCriteria",
		"as/dto/common/search/PermIdSearchCriteria", "as/dto/common/search/RegistrationDateSearchCriteria",
		"as/dto/common/search/ModificationDateSearchCriteria",
		"as/dto/common/search/NumberPropertySearchCriteria", "as/dto/common/search/BooleanPropertySearchCriteria",
		"as/dto/tag/search/TagSearchCriteria", "as/dto/common/search/StringPropertySearchCriteria",
		"as/dto/common/search/DatePropertySearchCriteria", "as/dto/common/search/AnyPropertySearchCriteria",
		"as/dto/common/search/AnyFieldSearchCriteria",
		"as/dto/common/search/AbstractCompositeSearchCriteria", "as/dto/person/search/RegistratorSearchCriteria",
		"as/dto/person/search/ModifierSearchCriteria",
		"as/dto/common/search/StrictlyStringPropertySearchCriteria",
		"as/dto/common/search/AnyDatePropertySearchCriteria", "as/dto/common/search/AnyNumberPropertySearchCriteria",
		"as/dto/common/search/AnyStringPropertySearchCriteria", "as/dto/common/search/AnyBooleanPropertySearchCriteria"
		], function(require, stjs, AbstractObjectSearchCriteria, SearchOperator) {
	var AbstractEntitySearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(AbstractEntitySearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.AbstractEntitySearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.operator = SearchOperator.AND;
		prototype.getOperator = function() {
			return this.operator;
		};
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withCodes = function() {
			var CodesSearchCriteria = require("as/dto/common/search/CodesSearchCriteria");
			return this.addCriteria(new CodesSearchCriteria());
		};
		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("as/dto/common/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.withRegistrator = function() {
			var RegistratorSearchCriteria = require("as/dto/person/search/RegistratorSearchCriteria");
			return this.addCriteria(new RegistratorSearchCriteria());
		};
		prototype.withModifier = function() {
			var ModifierSearchCriteria = require("as/dto/person/search/ModifierSearchCriteria");
			return this.addCriteria(new ModifierSearchCriteria());
		};
		prototype.withRegistrationDate = function() {
			var RegistrationDateSearchCriteria = require("as/dto/common/search/RegistrationDateSearchCriteria");
			return this.addCriteria(new RegistrationDateSearchCriteria());
		};
		prototype.withModificationDate = function() {
			var ModificationDateSearchCriteria = require("as/dto/common/search/ModificationDateSearchCriteria");
			return this.addCriteria(new ModificationDateSearchCriteria());
		};
		prototype.withNumberProperty = function(propertyName) {
			var NumberPropertySearchCriteria = require("as/dto/common/search/NumberPropertySearchCriteria");
			return this.addCriteria(new NumberPropertySearchCriteria(propertyName));
		};
		prototype.withBooleanProperty = function(propertyName) {
			var BooleanPropertySearchCriteria = require("as/dto/common/search/BooleanPropertySearchCriteria");
			return this.addCriteria(new BooleanPropertySearchCriteria(propertyName));
		};
		prototype.withTag = function() {
			var TagSearchCriteria = require("as/dto/tag/search/TagSearchCriteria");
			return this.addCriteria(new TagSearchCriteria());
		};
		prototype.withProperty = function(propertyName) {
			var StringPropertySearchCriteria = require("as/dto/common/search/StringPropertySearchCriteria");
			return this.addCriteria(new StringPropertySearchCriteria(propertyName));
		};
		prototype.withStringProperty = function(propertyName) {
			var StrictlyStringPropertySearchCriteria =
				require("as/dto/common/search/StrictlyStringPropertySearchCriteria");
			return this.addCriteria(new StrictlyStringPropertySearchCriteria(propertyName));
		};
		prototype.withDateProperty = function(propertyName) {
			var DatePropertySearchCriteria = require("as/dto/common/search/DatePropertySearchCriteria");
			return this.addCriteria(new DatePropertySearchCriteria(propertyName));
		};
		prototype.withAnyProperty = function() {
			var AnyPropertySearchCriteria = require("as/dto/common/search/AnyPropertySearchCriteria");
			return this.addCriteria(new AnyPropertySearchCriteria());
		};
		prototype.withAnyDateProperty = function() {
			var AnyDatePropertySearchCriteria = require("as/dto/common/search/AnyDatePropertySearchCriteria");
			return this.addCriteria(new AnyDatePropertySearchCriteria());
		};
		prototype.withAnyNumberProperty = function() {
			var AnyNumberPropertySearchCriteria = require("as/dto/common/search/AnyNumberPropertySearchCriteria");
			return this.addCriteria(new AnyNumberPropertySearchCriteria());
		};
		prototype.withAnyStringProperty = function() {
			var AnyStringPropertySearchCriteria = require("as/dto/common/search/AnyStringPropertySearchCriteria");
			return this.addCriteria(new AnyStringPropertySearchCriteria());
		};
		prototype.withAnyBooleanProperty = function() {
			var AnyBooleanPropertySearchCriteria = require("as/dto/common/search/AnyBooleanPropertySearchCriteria");
			return this.addCriteria(new AnyBooleanPropertySearchCriteria());
		};
		prototype.withAnyField = function() {
			var AnyFieldSearchCriteria = require("as/dto/common/search/AnyFieldSearchCriteria");
			return this.addCriteria(new AnyFieldSearchCriteria());
		};
		prototype.withOrOperator = function() {
			return this.withOperator(SearchOperator.OR);
		};
		prototype.withAndOperator = function() {
			return this.withOperator(SearchOperator.AND);
		};
	}, {
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return AbstractEntitySearchCriteria;
})