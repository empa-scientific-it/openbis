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
  test('add section with nothing selected', testAddWithNothingSelected)
  test('add section with a property selected', testAddWithPropertySelected)
  test('add section with a section selected', testAddWithSectionSelected)
})

async function testAddWithNothingSelected() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    selection: null,
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

  common.controller.handleAddSection()

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.SECTION,
      params: {
        id: 'section-2'
      }
    },
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
      },
      {
        id: 'section-2',
        name: { value: null },
        properties: []
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
      id: 'property-0'
    }
  )

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.PROPERTY,
      params: {
        id: 'property-0'
      }
    },
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

  common.controller.handleAddSection()

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.SECTION,
      params: {
        id: 'section-2'
      }
    },
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      },
      {
        id: 'section-2',
        name: { value: null },
        properties: []
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2']
      }
    ]
  })
}

async function testAddWithSectionSelected() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )

  await common.controller.load()
  common.controller.handleSelectionChange(EntityTypeFormSelectionType.SECTION, {
    id: 'section-0'
  })

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.SECTION,
      params: {
        id: 'section-0'
      }
    },
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

  common.controller.handleAddSection()

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.SECTION,
      params: {
        id: 'section-2'
      }
    },
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      },
      {
        id: 'section-2',
        name: { value: null },
        properties: []
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2']
      }
    ]
  })
}
