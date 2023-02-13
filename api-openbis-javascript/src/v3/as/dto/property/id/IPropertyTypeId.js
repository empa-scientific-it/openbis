/**
 * Holds information that uniquely identifies a property type in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IPropertyTypeId = function() {
	};
	stjs.extend(IPropertyTypeId, null, [ IObjectId ], null, {});
	return IPropertyTypeId;
})