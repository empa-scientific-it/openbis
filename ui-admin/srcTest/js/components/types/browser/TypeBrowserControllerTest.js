import AppController from '@src/js/components/AppController.js'
import TestAppController from '@srcTest/js/components/AppController.js'
import TypeBrowserController from '@src/js/components/types/browser/TypeBrowserController.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import openbis from '@srcTest/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import fixture from '@srcTest/js/common/fixture.js'

export default class TypeBrowserControllerTest {
  static SUITE = 'TypeBrowserController'

  beforeEach() {
    jest.resetAllMocks()

    AppController.setInstance(new TestAppController())

    this.context = new ComponentContext()
    this.controller = new TypeBrowserController()
    this.controller.loadSettings = function () {
      return {}
    }
    this.controller.onSettingsChange = function () {}

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

    openbis.mockSearchPropertyAssignments([
      ...fixture.TEST_SAMPLE_TYPE_DTO.propertyAssignments,
      ...fixture.ANOTHER_SAMPLE_TYPE_DTO.propertyAssignments
    ])
  }
  expectNewTypeAction(type) {
    expect(AppController.getInstance().objectNew).toHaveBeenCalledWith(
      pages.TYPES,
      type
    )
  }

  expectDeleteTypeAction(type, id) {
    expect(AppController.getInstance().objectDelete).toHaveBeenCalledWith(
      pages.TYPES,
      type,
      id
    )
  }
}
