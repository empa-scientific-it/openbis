import TypeBrowserControllerTest from '@srcTest/js/components/types/browser/TypeBrowserControllerTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new TypeBrowserControllerTest()
  common.beforeEach()
})

describe(TypeBrowserControllerTest.SUITE, () => {
  test('filter', testFilter)
})

async function testFilter() {
  await common.controller.load()

  common.controller.filterChange('ANOTHER')

  expect(common.controller.getNodes()).toMatchObject([
    {
      text: 'Object Types',
      expanded: true,
      selected: false,
      children: [
        {
          text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code,
          expanded: false,
          selected: false
        }
      ]
    },
    {
      text: 'Material Types',
      expanded: true,
      selected: false,
      children: [
        {
          text: fixture.ANOTHER_MATERIAL_TYPE_DTO.code,
          expanded: false,
          selected: false
        }
      ]
    },
    {
      text: 'Vocabulary Types',
      expanded: true,
      selected: false,
      children: [
        {
          text: fixture.ANOTHER_VOCABULARY_DTO.code,
          expanded: false,
          selected: false
        }
      ]
    }
  ])
}
