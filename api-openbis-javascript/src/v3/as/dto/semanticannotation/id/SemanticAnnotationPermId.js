/**
 * Semantic annotation perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/semanticannotation/id/ISemanticAnnotationId" ], function(stjs, ObjectPermId, ISemanticAnnotationId) {
	/**
	 * @param permId
	 *            Semantic annotation perm id, e.g. "201108050937246-1031".
	 */
	var SemanticAnnotationPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(SemanticAnnotationPermId, ObjectPermId, [ ObjectPermId, ISemanticAnnotationId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.id.SemanticAnnotationPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return SemanticAnnotationPermId;
})