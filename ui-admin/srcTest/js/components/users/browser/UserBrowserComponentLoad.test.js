import UserBrowserComponentTest from '@srcTest/js/components/users/browser/UserBrowserComponentTest.js'

let common = null

beforeEach(() => {
  common = new UserBrowserComponentTest()
  common.beforeEach()
})

describe(UserBrowserComponentTest.SUITE, () => {
  test('load', testLoad)
})

async function testLoad() {
  const browser = await common.mount()

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
