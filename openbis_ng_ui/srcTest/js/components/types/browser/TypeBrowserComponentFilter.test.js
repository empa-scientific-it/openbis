import TypeBrowserComponentTest from '@srcTest/js/components/types/browser/TypeBrowserComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new TypeBrowserComponentTest()
  common.beforeEach()
})

describe(TypeBrowserComponentTest.SUITE, () => {
  test('filter', testFilter)
})

async function testFilter() {
  const browser = await common.mount()

  browser.getFilter().change('ANOTHER')
  await browser.update()

  browser.expectJSON({
    filter: {
      value: 'ANOTHER'
    },
    nodes: [
      { level: 0, text: 'Object Types' },
      { level: 1, text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code },
      { level: 0, text: 'Material Types' },
      { level: 1, text: fixture.ANOTHER_MATERIAL_TYPE_DTO.code },
      { level: 0, text: 'Vocabulary Types' },
      { level: 1, text: fixture.ANOTHER_VOCABULARY_DTO.code }
    ]
  })

  browser.getFilter().getClearIcon().click()
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
      { level: 1, text: fixture.TEST_EXPERIMENT_TYPE_DTO.code },
      { level: 0, text: 'Data Set Types' },
      { level: 1, text: fixture.TEST_DATA_SET_TYPE_DTO.code },
      { level: 0, text: 'Material Types' },
      { level: 1, text: fixture.ANOTHER_MATERIAL_TYPE_DTO.code },
      { level: 1, text: fixture.TEST_MATERIAL_TYPE_DTO.code },
      { level: 0, text: 'Vocabulary Types' },
      { level: 1, text: fixture.ANOTHER_VOCABULARY_DTO.code },
      { level: 1, text: fixture.TEST_VOCABULARY_DTO.code },
      { level: 0, text: 'Property Types' }
    ]
  })
}
