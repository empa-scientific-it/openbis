import TypeBrowserControllerTest from '@srcTest/js/components/types/browser/TypeBrowserControllerTest.js'
import objectType from '@src/js/common/consts/objectType.js'

let common = null

beforeEach(() => {
  common = new TypeBrowserControllerTest()
  common.beforeEach()
})

describe(TypeBrowserControllerTest.SUITE, () => {
  test('add node', testAddNode)
})

async function testAddNode() {
  await common.controller.load()

  await common.controller.selectObject({
    type: objectType.OVERVIEW,
    id: objectType.OBJECT_TYPE
  })
  await common.controller.addNode()

  common.expectNewTypeAction(objectType.NEW_OBJECT_TYPE)
}
