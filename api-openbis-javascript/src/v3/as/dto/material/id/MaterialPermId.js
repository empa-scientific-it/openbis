/**
 * Material perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/material/id/IMaterialId" ], function(stjs, IMaterialId) {
	/**
	 * @param permId
	 *            Material perm id, e.g. "MY_MATERIAL (MY_MATERIAL_TYPE)".
	 */
	var MaterialPermId = function(code, typeCode) {
		this.setCode(code);
		this.setTypeCode(typeCode);
	};
	stjs.extend(MaterialPermId, null, [ IMaterialId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.id.MaterialPermId';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.typeCode = null;
		prototype.getCode = function() {
			return this.code;
		}
		prototype.setCode = function(code) {
			this.code = code;
		}
		prototype.getTypeCode = function() {
			return this.typeCode;
		}
		prototype.setTypeCode = function(typeCode) {
			this.typeCode = typeCode;
		}
		prototype.toString = function() {
			return this.getCode() + " (" + this.getTypeCode() + ")";
		};
		prototype.hashCode = function() {

		};
	}, {});
	return MaterialPermId;
})