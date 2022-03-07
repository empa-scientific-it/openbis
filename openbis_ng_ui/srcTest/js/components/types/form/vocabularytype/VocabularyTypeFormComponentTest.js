import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import VocabularyTypeForm from '@src/js/components/types/form/vocabularytype/VocabularyTypeForm.jsx'
import VocabularyTypeFormWrapper from '@srcTest/js/components/types/form/vocabularytype/wrapper/VocabularyTypeFormWrapper.js'
import VocabularyTypeFormController from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormController.js'
import VocabularyTypeFormFacade from '@src/js/components/types/form/vocabularytype/VocabularyTypeFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'

jest.mock(
  '@src/js/components/types/form/vocabularytype/VocabularyTypeFormFacade'
)

export default class VocabularyTypeFormComponentTest extends ComponentTest {
  static SUITE = 'VocabularyTypeFormComponent'

  constructor() {
    super(
      object => (
        <VocabularyTypeForm object={object} controller={this.controller} />
      ),
      wrapper => new VocabularyTypeFormWrapper(wrapper)
    )
    this.facade = null
    this.controller = null
  }

  async beforeEach() {
    super.beforeEach()

    this.facade = new VocabularyTypeFormFacade()
    this.controller = new VocabularyTypeFormController(this.facade)
  }

  async mountNew() {
    return await this.mount({
      type: objectTypes.NEW_VOCABULARY_TYPE
    })
  }

  async mountExisting(vocabulary) {
    this.facade.loadVocabulary.mockReturnValue(Promise.resolve(vocabulary))

    return await this.mount({
      id: vocabulary.getCode(),
      type: objectTypes.VOCABULARY_TYPE
    })
  }
}
