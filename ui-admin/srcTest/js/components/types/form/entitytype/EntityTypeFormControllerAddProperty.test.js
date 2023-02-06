import EntityTypeFormControllerTest from '@srcTest/js/components/types/form/entitytype/EntityTypeFormControllerTest.js'
import EntityTypeFormSelectionType from '@src/js/components/types/form/entitytype/EntityTypeFormSelectionType.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import fixture from '@srcTest/js/common/fixture.js'

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
  test('add property with a section selected', testAddWithSectionSelected)
  test('add property with a property selected', testAddWithPropertySelected)
})

async function testAddWithSectionSelected() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )

  await common.controller.load()
  common.controller.handleSelectionChange(EntityTypeFormSelectionType.SECTION, {
    id: 'section-1'
  })

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.SECTION,
      params: {
        id: 'section-1'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2']
      }
    ]
  })

  common.controller.handleAddProperty()

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.PROPERTY,
      params: {
        id: 'property-3'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      },
      {
        id: 'property-3',
        code: { value: null }
      }
    ],
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2', 'property-3']
      }
    ]
  })
}

async function testAddWithPropertySelected() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )

  await common.controller.load()
  common.controller.handleSelectionChange(
    EntityTypeFormSelectionType.PROPERTY,
    {
      id: 'property-1'
    }
  )

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.PROPERTY,
      params: {
        id: 'property-1'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2']
      }
    ]
  })

  common.controller.handleAddProperty()

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.PROPERTY,
      params: {
        id: 'property-3'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      },
      {
        id: 'property-3',
        code: { value: null }
      }
    ],
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-3', 'property-2']
      }
    ]
  })
}
