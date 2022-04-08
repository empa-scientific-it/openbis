import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import VocabularyTypeFormParametersVocabulary from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormParametersVocabulary.jsx'
import VocabularyTypeFormParametersVocabularyWrapper from '@srcTest/js/components/types/form/vocabularytype/wrapper/VocabularyTypeFormParametersVocabularyWrapper.js'
import VocabularyTypeFormParametersTerm from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormParametersTerm.jsx'
import VocabularyTypeFormParametersTermWrapper from '@srcTest/js/components/types/form/vocabularytype/wrapper/VocabularyTypeFormParametersTermWrapper.js'

export default class VocabularyTypeFormParametersWrapper extends BaseWrapper {
  getVocabulary() {
    return new VocabularyTypeFormParametersVocabularyWrapper(
      this.findComponent(VocabularyTypeFormParametersVocabulary)
    )
  }

  getTerm() {
    return new VocabularyTypeFormParametersTermWrapper(
      this.findComponent(VocabularyTypeFormParametersTerm)
    )
  }

  toJSON() {
    return {
      vocabulary: this.getVocabulary().toJSON(),
      term: this.getTerm().toJSON()
    }
  }
}
