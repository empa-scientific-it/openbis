import TypeBrowserComponentTest from '@srcTest/js/components/types/browser/TypeBrowserComponentTest.js'

let common = null

beforeEach(() => {
  common = new TypeBrowserComponentTest()
  common.beforeEach()
})

describe(TypeBrowserComponentTest.SUITE, () => {
  test('load', testLoad)
})

async function testLoad() {
  const browser = await common.mount()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Object Types' },
      { level: 0, text: 'Collection Types' },
      { level: 0, text: 'Data Set Types' },
      { level: 0, text: 'Material Types' },
      { level: 0, text: 'Vocabulary Types' },
      { level: 0, text: 'Property Types' }
    ]
  })
}
