/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var CreateCodesOperation = function(prefix, entityKind, count) {
		this.prefix = prefix;
		this.entityKind = entityKind;
		this.count = count;
	};
	stjs.extend(CreateCodesOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entity.create.CreateCodesOperation';
		prototype.prefix = null;
		prototype.entityKind = null;
		prototype.count = null;

		prototype.getPrefix = function() {
			return this.prefix;
		};
		prototype.getEntityKind = function() {
			return this.entityKind;
		};
		prototype.getCount = function() {
			return this.count;
		};
		prototype.getMessage = function() {
			return "CreateCodesOperation";
		};
	}, {
		entityKind : "EntityKind"
	});
	return CreateCodesOperation;
})