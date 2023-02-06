import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import EntityTypeForm from '@src/js/components/types/form/entitytype/EntityTypeForm.jsx'
import EntityTypeFormWrapper from '@srcTest/js/components/types/form/entitytype/wrapper/EntityTypeFormWrapper.js'
import EntityTypeFormController from '@src/js/components/types/form/entitytype/EntityTypeFormController.js'
import EntityTypeFormFacade from '@src/js/components/types/form/entitytype/EntityTypeFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'

jest.mock('@src/js/components/types/form/entitytype/EntityTypeFormFacade')

export default class EntityTypeFormComponentTest extends ComponentTest {
  static SUITE = 'EntityTypeFormComponent'

  constructor() {
    super(
      object => <EntityTypeForm object={object} controller={this.controller} />,
      wrapper => new EntityTypeFormWrapper(wrapper)
    )
    this.facade = null
    this.controller = null
  }

  async beforeEach() {
    super.beforeEach()

    this.facade = new EntityTypeFormFacade()
    this.controller = new EntityTypeFormController(this.facade)

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
