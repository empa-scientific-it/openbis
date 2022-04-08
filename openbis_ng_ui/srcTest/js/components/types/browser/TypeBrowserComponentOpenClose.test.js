import TypeBrowserComponentTest from '@srcTest/js/components/types/browser/TypeBrowserComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new TypeBrowserComponentTest()
  common.beforeEach()
})

describe(TypeBrowserComponentTest.SUITE, () => {
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
      { level: 0, text: 'Object Types' },
      { level: 1, text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code },
      { level: 1, text: fixture.TEST_SAMPLE_TYPE_DTO.code },
      { level: 0, text: 'Collection Types' },
      { level: 0, text: 'Data Set Types' },
      { level: 0, text: 'Material Types' },
      { level: 0, text: 'Vocabulary Types' },
      { level: 0, text: 'Property Types' }
    ]
  })

  browser.getNodes()[0].getIcon().click()
  await browser.update()

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
