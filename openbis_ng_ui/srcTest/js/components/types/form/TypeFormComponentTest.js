import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import TypeForm from '@src/js/components/types/form/TypeForm.jsx'
import TypeFormWrapper from '@srcTest/js/components/types/form/wrapper/TypeFormWrapper.js'
import TypeFormController from '@src/js/components/types/form/TypeFormController.js'
import TypeFormFacade from '@src/js/components/types/form/TypeFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'

jest.mock('@src/js/components/types/form/TypeFormFacade')

export default class TypeFormComponentTest extends ComponentTest {
  static SUITE = 'TypeFormComponent'

  constructor() {
    super(
      object => <TypeForm object={object} controller={this.controller} />,
      wrapper => new TypeFormWrapper(wrapper)
    )
    this.facade = null
    this.controller = null
  }

  async beforeEach() {
    super.beforeEach()

    this.facade = new TypeFormFacade()
    this.controller = new TypeFormController(this.facade)

    this.facade.loadType.mockReturnValue(Promise.resolve({}))
    this.facade.loadDynamicPlugins.mockReturnValue(Promise.resolve([]))
    this.facade.loadValidationPlugins.mockReturnValue(Promise.resolve([]))
    this.facade.loadMaterials.mockReturnValue(Promise.resolve([]))
    this.facade.loadSamples.mockReturnValue(Promise.resolve([]))
    this.facade.loadVocabularies.mockReturnValue(Promise.resolve([]))
    this.facade.loadVocabularyTerms.mockReturnValue(Promise.resolve([]))
    this.facade.loadPropertyTypes.mockReturnValue(Promise.resolve([]))
  }

  async mountNew() {
    return await this.mount({
      type: objectTypes.NEW_OBJECT_TYPE
    })
  }

  async mountExisting(type) {
    this.facade.loadType.mockReturnValue(Promise.resolve(type))

    return await this.mount({
      id: type.getCode(),
      type: objectTypes.OBJECT_TYPE
    })
  }
}
