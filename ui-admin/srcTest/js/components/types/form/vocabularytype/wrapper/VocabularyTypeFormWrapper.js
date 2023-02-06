import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridWrapper from '@srcTest/js/components/common/grid/wrapper/GridWrapper.js'
import VocabularyTypeFormParameters from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormParameters.jsx'
import VocabularyTypeFormParametersWrapper from '@srcTest/js/components/types/form/vocabularytype/wrapper/VocabularyTypeFormParametersWrapper.js'
import VocabularyTypeFormButtons from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormButtons.jsx'
import VocabularyTypeFormButtonsWrapper from '@srcTest/js/components/types/form/vocabularytype/wrapper/VocabularyTypeFormButtonsWrapper.js'

export default class VocabularyTypeFormWrapper extends BaseWrapper {
  getGrid() {
    return new GridWrapper(this.findComponent(Grid))
  }

  getParameters() {
    return new VocabularyTypeFormParametersWrapper(
      this.findComponent(VocabularyTypeFormParameters)
    )
  }

  getButtons() {
    return new VocabularyTypeFormButtonsWrapper(
      this.findComponent(VocabularyTypeFormButtons)
    )
  }

  toJSON() {
    return {
      grid: this.getGrid().toJSON(),
      parameters: this.getParameters().toJSON(),
      buttons: this.getButtons().toJSON()
    }
  }
}
