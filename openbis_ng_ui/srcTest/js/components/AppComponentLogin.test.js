import AppComponentTest from '@srcTest/js/components/AppComponentTest.js'

let common = null

beforeEach(() => {
  common = new AppComponentTest()
  common.beforeEach()
})

describe(AppComponentTest.SUITE, () => {
  test('login', testLogin)
})

async function testLogin() {
  const app = await common.mount()

  app.expectJSON({
    login: {
      user: {
        value: null,
        enabled: true
      },
      password: {
        value: null,
        enabled: true
      },
      button: {
        enabled: true
      }
    },
    menu: null,
    types: null,
    users: null
  })

  await common.login(app)

  app.expectJSON({
    login: null,
    menu: {
      tabs: [
        {
          label: 'Types',
          selected: true
        },
        {
          label: 'Users',
          selected: false
        },
        {
          label: 'Tools',
          selected: false
        }
      ]
    },
    types: {
      browser: {
        filter: {
          value: null
        },
        nodes: [
          { level: 0, text: 'Object Types' },
          { level: 0, text: 'Collection Types' },
          { level: 0, text: 'Data Set Types' },
          { level: 0, text: 'Material Types' },
          { level: 0, text: 'Vocabulary Types' }
        ]
      },
      content: {
        tabs: []
      }
    },
    users: {
      browser: {
        filter: {
          value: null
        },
        nodes: [
          { level: 0, text: 'Users' },
          { level: 0, text: 'Groups' }
        ]
      },
      content: {
        tabs: []
      }
    }
  })
}
