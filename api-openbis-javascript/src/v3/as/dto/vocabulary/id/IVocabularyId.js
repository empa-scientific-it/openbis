/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IVocabularyId = function() {
	};
	stjs.extend(IVocabularyId, null, [ IObjectId ], null, {});
	return IVocabularyId;
})