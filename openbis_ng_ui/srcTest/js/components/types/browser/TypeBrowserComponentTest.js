import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import BrowserWrapper from '@srcTest/js/components/common/browser/wrapper/BrowserWrapper.js'
import TypeBrowser from '@src/js/components/types/browser/TypeBrowser.jsx'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

export default class TypeBrowserComponentTest extends ComponentTest {
  static SUITE = 'TypeBrowserComponent'

  constructor() {
    super(
      () => <TypeBrowser />,
      wrapper => new BrowserWrapper(wrapper)
    )
  }

  async beforeEach() {
    super.beforeEach()

    openbis.mockSearchSampleTypes([
      fixture.TEST_SAMPLE_TYPE_DTO,
      fixture.ANOTHER_SAMPLE_TYPE_DTO
    ])

    openbis.mockSearchExperimentTypes([fixture.TEST_EXPERIMENT_TYPE_DTO])
    openbis.mockSearchDataSetTypes([fixture.TEST_DATA_SET_TYPE_DTO])

    openbis.mockSearchMaterialTypes([
      fixture.TEST_MATERIAL_TYPE_DTO,
      fixture.ANOTHER_MATERIAL_TYPE_DTO
    ])

    openbis.mockSearchVocabularies([
      fixture.TEST_VOCABULARY_DTO,
      fixture.ANOTHER_VOCABULARY_DTO
    ])

    openbis.mockSearchPropertyTypes([
      fixture.TEST_PROPERTY_TYPE_1_DTO,
      fixture.TEST_PROPERTY_TYPE_2_DTO
    ])
  }
}
