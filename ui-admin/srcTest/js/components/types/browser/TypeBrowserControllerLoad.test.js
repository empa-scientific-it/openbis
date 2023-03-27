import TypeBrowserControllerTest from '@srcTest/js/components/types/browser/TypeBrowserControllerTest.js'

let common = null

beforeEach(() => {
  common = new TypeBrowserControllerTest()
  common.beforeEach()
})

describe(TypeBrowserControllerTest.SUITE, () => {
  test('load', testLoad)
})

async function testLoad() {
  await common.controller.load()

  expect(common.controller.getTree()).toMatchObject({
    children: [
      {
        text: 'Object Types',
        expanded: false,
        selected: false
      },
      {
        text: 'Collection Types',
        expanded: false,
        selected: false
      },
      {
        text: 'Data Set Types',
        expanded: false,
        selected: false
      },
      {
        text: 'Material Types',
        expanded: false,
        selected: false
      },
      {
        text: 'Vocabulary Types',
        expanded: false,
        selected: false
      },
      {
        text: 'Property Types',
        expanded: false,
        selected: false
      }
    ]
  })
}
