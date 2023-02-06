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
  test('select a section', testSelectSection)
  test('select a property', testSelectProperty)
})

async function testSelectSection() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    selection: null
  })

  common.controller.handleSelectionChange(EntityTypeFormSelectionType.SECTION, {
    id: 'section-0'
  })

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: EntityTypeFormSelectionType.SECTION,
      params: {
        id: 'section-0'
      }
    }
  })

  common.controller.handleSelectionChange()

  expect(common.context.getState()).toMatchObject({
    selection: null
  })
}

async function testSelectProperty() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    selection: null
  })

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
    }
  })

  common.controller.handleSelectionChange()

  expect(common.context.getState()).toMatchObject({
    selection: null
  })
}
