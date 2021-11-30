/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var DeletedObject = function() {
	};
	stjs.extend(DeletedObject, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.deletion.DeletedObject';
		prototype.id = null;
        prototype.identifier = null;
        prototype.entityTypeCode = null;
        prototype.entityKind = null;
		prototype.getId = function() {
			return this.id;
		};
		prototype.setId = function(id) {
			this.id = id;
		};
        prototype.getIdentifier = function() {
            return this.Identifier;
        };
        prototype.setIdentifier = function(Identifier) {
            this.Identifier = Identifier;
        };
        prototype.getEntityTypeCode = function() {
            return this.entityTypeCode;
        };
        prototype.setEntityTypeCode = function(entityTypeCode) {
            this.entityTypeCode = entityTypeCode;
        };
        prototype.getEntityKind = function() {
            return this.entityKind;
        };
        prototype.setEntityKind = function(entityKind) {
            this.entityKind = entityKind;
        };
	}, {
		id : "IObjectId"
	});
	return DeletedObject;
})