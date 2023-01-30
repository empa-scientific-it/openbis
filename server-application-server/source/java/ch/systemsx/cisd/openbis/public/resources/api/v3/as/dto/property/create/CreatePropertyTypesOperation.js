define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
    var CreatePropertyTypesOperation = function(creations) {
        CreateObjectsOperation.call(this, creations);
    };
    stjs.extend(CreatePropertyTypesOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
        prototype['@type'] = 'as.dto.property.create.CreatePropertyTypesOperation';
        prototype.getMessage = function() {
            return "CreatePropertyTypesOperation";
        };
    }, {});
    return CreatePropertyTypesOperation;
})

