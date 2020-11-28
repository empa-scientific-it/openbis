import ToolBrowserComponentTest from '@srcTest/js/components/tools/browser/ToolBrowserComponentTest.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new ToolBrowserComponentTest()
  common.beforeEach()
})

describe(ToolBrowserComponentTest.SUITE, () => {
  test('load', testLoad)
})

async function testLoad() {
  openbis.mockSearchPlugins([])
  openbis.mockSearchQueries([])

  const browser = await common.mount()

  browser.expectJSON({
    filter: {
      value: null
    },
    nodes: [
      { level: 0, text: 'Dynamic Property Plugins' },
      { level: 0, text: 'Entity Validation Plugins' },
      { level: 0, text: 'Queries' }
    ]
  })
}
