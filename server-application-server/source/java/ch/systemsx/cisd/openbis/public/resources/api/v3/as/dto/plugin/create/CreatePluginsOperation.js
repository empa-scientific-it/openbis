define([ "stjs", "as/dto/common/create/CreateObjectsOperation" ], function(stjs, CreateObjectsOperation) {
    var CreatePluginsOperation = function(creations) {
        CreateObjectsOperation.call(this, creations);
    };
    stjs.extend(CreatePluginsOperation, CreateObjectsOperation, [ CreateObjectsOperation ], function(constructor, prototype) {
        prototype['@type'] = 'as.dto.plugin.create.CreatePluginsOperation';
        prototype.getMessage = function() {
            return "CreatePluginsOperation";
        };
    }, {});
    return CreatePluginsOperation;
})

