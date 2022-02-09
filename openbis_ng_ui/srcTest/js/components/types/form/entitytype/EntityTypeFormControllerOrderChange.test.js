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
  test('move section', testMoveSection)
  test('move property within section', testMovePropertyWithinSection)
  test('move property between sections', testMovePropertyBetweenSections)
})

async function testMoveSection() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
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

  common.controller.handleOrderChange(EntityTypeFormSelectionType.SECTION, {
    fromIndex: 0,
    toIndex: 1
  })

  expect(common.context.getState()).toMatchObject({
    sections: [
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2']
      },
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      }
    ]
  })
}

async function testMovePropertyWithinSection() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
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

  common.controller.handleOrderChange(EntityTypeFormSelectionType.PROPERTY, {
    fromSectionId: 'section-1',
    toSectionId: 'section-1',
    fromIndex: 0,
    toIndex: 1
  })

  expect(common.context.getState()).toMatchObject({
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-2', 'property-1']
      }
    ]
  })
}

async function testMovePropertyBetweenSections() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
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

  common.controller.handleOrderChange(EntityTypeFormSelectionType.PROPERTY, {
    fromSectionId: 'section-1',
    toSectionId: 'section-0',
    fromIndex: 1,
    toIndex: 0
  })

  expect(common.context.getState()).toMatchObject({
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-2', 'property-0']
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1']
      }
    ]
  })
}
