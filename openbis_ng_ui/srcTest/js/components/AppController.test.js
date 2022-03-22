import AppController from '@src/js/components/AppController.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import openbis from '@srcTest/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import fixture from '@srcTest/js/common/fixture.js'

const SUITE = 'AppController'

let controller = null
beforeEach(() => {
  jest.resetAllMocks()
  controller = new AppController.AppController()
  controller.init(new ComponentContext())
})

describe(SUITE, () => {
  test('login successful', async () => {
    openbis.login.mockReturnValue(fixture.TEST_SESSION_TOKEN)

    await controller.login(fixture.TEST_USER, fixture.TEST_PASSWORD)

    expect(controller.getSession()).toEqual({
      sessionToken: fixture.TEST_SESSION_TOKEN,
      userName: fixture.TEST_USER
    })
    expect(controller.getCurrentPage()).toEqual(pages.TYPES)
  })

  test('login failed', async () => {
    openbis.login.mockReturnValue(null)

    await controller.login(fixture.TEST_USER, fixture.TEST_PASSWORD)

    expect(controller.getSession()).toBeNull()
    expect(controller.getError()).toEqual('Incorrect user or password')
  })

  test('logout', async () => {
    openbis.login.mockReturnValue(fixture.TEST_SESSION_TOKEN)

    await controller.login(fixture.TEST_USER, fixture.TEST_PASSWORD)
    await controller.logout()

    expect(controller.getSession()).toBeNull()
    expect(controller.getError()).toBeNull()
  })

  test('open and close types', async () => {
    let object1 = {
      type: objectType.OBJECT_TYPE,
      id: fixture.TEST_SAMPLE_TYPE_DTO.code
    }
    let object2 = {
      type: objectType.COLLECTION_TYPE,
      id: fixture.TEST_EXPERIMENT_TYPE_DTO.code
    }
    let object3 = {
      type: objectType.VOCABULARY_TYPE,
      id: fixture.TEST_VOCABULARY_DTO.code
    }

    await controller.objectOpen(pages.TYPES, object1.type, object1.id)
    await update()

    expectSelectedObject(pages.TYPES, object1)
    expectOpenObjects(pages.TYPES, [object1])

    await controller.objectOpen(pages.TYPES, object2.type, object2.id)
    await update()

    expectSelectedObject(pages.TYPES, object2)
    expectOpenObjects(pages.TYPES, [object1, object2])

    await controller.objectOpen(pages.TYPES, object3.type, object3.id)
    await update()

    expectSelectedObject(pages.TYPES, object3)
    expectOpenObjects(pages.TYPES, [object1, object2, object3])

    await controller.objectClose(pages.TYPES, object1.type, object1.id)
    await update()

    expectSelectedObject(pages.TYPES, object3)
    expectOpenObjects(pages.TYPES, [object2, object3])

    await controller.objectClose(pages.TYPES, object3.type, object3.id)
    await update()

    expectSelectedObject(pages.TYPES, object2)
    expectOpenObjects(pages.TYPES, [object2])

    await controller.objectClose(pages.TYPES, object2.type, object2.id)
    await update()

    expectSelectedObject(pages.TYPES, null)
    expectOpenObjects(pages.TYPES, [])
  })

  test('open and close users and groups', async () => {
    let object1 = { type: objectType.USER, id: fixture.TEST_USER_DTO.userId }
    let object2 = { type: objectType.USER, id: fixture.ANOTHER_USER_DTO.userId }
    let object3 = {
      type: objectType.USER_GROUP,
      id: fixture.TEST_USER_GROUP_DTO.code
    }

    await controller.objectOpen(pages.USERS, object1.type, object1.id)
    await update()

    expectSelectedObject(pages.USERS, object1)
    expectOpenObjects(pages.USERS, [object1])

    await controller.objectOpen(pages.USERS, object2.type, object2.id)
    await update()

    expectSelectedObject(pages.USERS, object2)
    expectOpenObjects(pages.USERS, [object1, object2])

    await controller.objectOpen(pages.USERS, object3.type, object3.id)
    await update()

    expectSelectedObject(pages.USERS, object3)
    expectOpenObjects(pages.USERS, [object1, object2, object3])

    await controller.objectClose(pages.USERS, object1.type, object1.id)
    await update()

    expectSelectedObject(pages.USERS, object3)
    expectOpenObjects(pages.USERS, [object2, object3])

    await controller.objectClose(pages.USERS, object3.type, object3.id)
    await update()

    expectSelectedObject(pages.USERS, object2)
    expectOpenObjects(pages.USERS, [object2])

    await controller.objectClose(pages.USERS, object2.type, object2.id)
    await update()

    expectSelectedObject(pages.USERS, null)
    expectOpenObjects(pages.USERS, [])
  })
})

function expectSelectedObject(page, object) {
  expect(controller.getSelectedObject(page)).toEqual(object)
}

function expectOpenObjects(page, objects) {
  expect(controller.getOpenObjects(page)).toEqual(objects)
}

function update() {
  return new Promise(resolve => {
    setTimeout(() => {
      resolve()
    }, 0)
  })
}
