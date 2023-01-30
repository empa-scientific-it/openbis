/**
 * Property type perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/property/id/IPropertyTypeId" ], function(stjs, ObjectPermId, IPropertyTypeId) {

	/**
	 * @param permId
	 *            Property type perm id, e.g. "MY_PROPERTY_TYPE".
	 */
	var PropertyTypePermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(PropertyTypePermId, ObjectPermId, [ ObjectPermId, IPropertyTypeId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.id.PropertyTypePermId';
		constructor.serialVersionUID = 1;
	}, {});
	return PropertyTypePermId;
})