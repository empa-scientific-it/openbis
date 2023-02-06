import UserBrowserComponentTest from '@srcTest/js/components/users/browser/UserBrowserComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new UserBrowserComponentTest()
  common.beforeEach()
})

describe(UserBrowserComponentTest.SUITE, () => {
  test('open/close', testOpenClose)
})

async function testOpenClose() {
  const browser = await common.mount()

  browser.getNodes()[0].getIcon().click()
  await browser.update()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Users' },
      { level: 1, text: fixture.ANOTHER_USER_DTO.userId },
      { level: 1, text: fixture.TEST_USER_DTO.userId },
      { level: 0, text: 'Groups' }
    ]
  })

  browser.getNodes()[3].getIcon().click()
  await browser.update()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Users' },
      { level: 1, text: fixture.ANOTHER_USER_DTO.userId },
      { level: 1, text: fixture.TEST_USER_DTO.userId },
      { level: 0, text: 'Groups' },
      { level: 1, text: fixture.ALL_USERS_GROUP_DTO.code },
      { level: 1, text: fixture.ANOTHER_USER_GROUP_DTO.code },
      { level: 1, text: fixture.TEST_USER_GROUP_DTO.code }
    ]
  })

  browser.getNodes()[0].getIcon().click()
  await browser.update()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Users' },
      { level: 0, text: 'Groups' },
      { level: 1, text: fixture.ALL_USERS_GROUP_DTO.code },
      { level: 1, text: fixture.ANOTHER_USER_GROUP_DTO.code },
      { level: 1, text: fixture.TEST_USER_GROUP_DTO.code }
    ]
  })

  browser.getNodes()[1].getIcon().click()
  await browser.update()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Users' },
      { level: 0, text: 'Groups' }
    ]
  })
}
