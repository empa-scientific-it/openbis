import EntityTypeFormControllerTest from '@srcTest/js/components/types/form/entitytype/EntityTypeFormControllerTest.js'
import EntityTypeFormSelectionType from '@src/js/components/types/form/entitytype/EntityTypeFormSelectionType.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new EntityTypeFormControllerTest()
  common.beforeEach()
  common.init({
    id: 'TEST_OBJECT_ID',
    type: objectTypes.OBJECT_TYPE
  })
})

afterEach(() => {
  common.afterEach()
})

describe(EntityTypeFormControllerTest.SUITE, () => {
  test('validate', testValidate)
})

async function testValidate() {
  const SAMPLE_TYPE = new openbis.SampleType()

  common.facade.loadType.mockReturnValue(Promise.resolve(SAMPLE_TYPE))

  await common.controller.load()
  common.controller.handleAddSection()
  common.controller.handleAddProperty()

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.PROPERTY,
      params: {
        id: 'property-0'
      }
    },
    type: {
      errors: 0
    },
    properties: [
      {
        id: 'property-0',
        code: { value: null },
        dataType: { value: null },
        label: { value: null },
        description: { value: null },
        errors: 0
      }
    ]
  })

  await common.controller.handleSave()

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.TYPE,
      params: {
        part: 'code'
      }
    },
    type: {
      code: {
        error: 'Code cannot be empty'
      },
      generatedCodePrefix: {
        error: 'Generated code prefix cannot be empty'
      },
      errors: 2
    },
    properties: [
      {
        id: 'property-0',
        code: { value: null, error: 'Code cannot be empty' },
        dataType: { value: null, error: 'Data Type cannot be empty' },
        label: { value: null, error: 'Label cannot be empty' },
        description: { value: null, error: 'Description cannot be empty' },
        errors: 4
      }
    ]
  })
}
