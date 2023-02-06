import TypeBrowserControllerTest from '@srcTest/js/components/types/browser/TypeBrowserControllerTest.js'
import objectType from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new TypeBrowserControllerTest()
  common.beforeEach()
})

describe(TypeBrowserControllerTest.SUITE, () => {
  test('remove node', testRemoveNode)
})

async function testRemoveNode() {
  openbis.mockSearchPropertyTypes([fixture.TEST_PROPERTY_TYPE_1_DTO])
  openbis.deleteSampleTypes.mockReturnValue(Promise.resolve())

  await common.controller.load()
  await common.controller.changeAutoShowSelectedObject()

  expect(openbis.deleteSampleTypes).toHaveBeenCalledTimes(0)

  await common.controller.selectObject({
    type: objectType.OBJECT_TYPE,
    id: fixture.TEST_SAMPLE_TYPE_DTO.code
  })
  await common.controller.removeNode()

  const createDeleteTypeOperation = typeCode => {
    const id = new openbis.EntityTypePermId(typeCode)
    const options = new openbis.SampleTypeDeletionOptions()
    options.setReason('deleted via ng_ui')
    return new openbis.DeleteSampleTypesOperation([id], options)
  }

  const createDeletePropertyTypeOperation = propertyTypeCodes => {
    const ids = propertyTypeCodes.map(propertyTypeCode => {
      return new openbis.PropertyTypePermId(propertyTypeCode)
    })
    const options = new openbis.PropertyTypeDeletionOptions()
    options.setReason('deleted via ng_ui')
    return new openbis.DeletePropertyTypesOperation(ids, options)
  }

  const options = new openbis.SynchronousOperationExecutionOptions()
  options.setExecuteInOrder(true)

  expect(openbis.executeOperations).toHaveBeenCalledWith(
    [
      createDeleteTypeOperation(fixture.TEST_SAMPLE_TYPE_DTO.code),
      createDeletePropertyTypeOperation([
        fixture.TEST_PROPERTY_TYPE_2_DTO.code,
        fixture.TEST_PROPERTY_TYPE_3_DTO.code
      ])
    ],
    options
  )

  common.expectDeleteTypeAction(
    objectType.OBJECT_TYPE,
    fixture.TEST_SAMPLE_TYPE_DTO.code
  )
}
