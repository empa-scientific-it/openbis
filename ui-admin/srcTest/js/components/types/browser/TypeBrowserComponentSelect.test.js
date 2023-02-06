import TypeBrowserComponentTest from '@srcTest/js/components/types/browser/TypeBrowserComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new TypeBrowserComponentTest()
  common.beforeEach()
})

describe(TypeBrowserComponentTest.SUITE, () => {
  test('select entity kind', testSelectEntityKind)
  test('select entity type', testSelectEntityType)
})

async function testSelectEntityKind() {
  const browser = await common.mount()

  browser.clickAutoShowSelected()
  await browser.update()

  browser.expectJSON({
    nodes: [
      { level: 0, text: 'Object Types', selected: false },
      { level: 0, text: 'Collection Types', selected: false },
      { level: 0, text: 'Data Set Types', selected: false },
      { level: 0, text: 'Material Types', selected: false },
      { level: 0, text: 'Vocabulary Types', selected: false },
      { level: 0, text: 'Property Types', selected: false }
    ],
    buttons: {
      add: {
        enabled: false
      },
      remove: {
        enabled: false
      }
    }
  })

  browser.getNodes()[0].click()
  await browser.update()

  browser.expectJSON({
    nodes: [
      { level: 0, text: 'Object Types', selected: true },
      { level: 0, text: 'Collection Types', selected: false },
      { level: 0, text: 'Data Set Types', selected: false },
      { level: 0, text: 'Material Types', selected: false },
      { level: 0, text: 'Vocabulary Types', selected: false },
      { level: 0, text: 'Property Types', selected: false }
    ],
    buttons: {
      add: {
        enabled: true
      },
      remove: {
        enabled: false
      }
    }
  })
}

async function testSelectEntityType() {
  const browser = await common.mount()

  browser.clickAutoShowSelected()
  await browser.update()

  browser.getNodes()[0].getIcon().click()
  await browser.update()

  browser.expectJSON({
    nodes: [
      { level: 0, text: 'Object Types', selected: false },
      { level: 1, text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code, selected: false },
      { level: 1, text: fixture.TEST_SAMPLE_TYPE_DTO.code, selected: false },
      { level: 0, text: 'Collection Types', selected: false },
      { level: 0, text: 'Data Set Types', selected: false },
      { level: 0, text: 'Material Types', selected: false },
      { level: 0, text: 'Vocabulary Types', selected: false },
      { level: 0, text: 'Property Types', selected: false }
    ],
    buttons: {
      add: {
        enabled: false
      },
      remove: {
        enabled: false
      }
    }
  })

  browser.getNodes()[1].click()
  await browser.update()

  browser.expectJSON({
    nodes: [
      { level: 0, text: 'Object Types', selected: false },
      { level: 1, text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code, selected: true },
      { level: 1, text: fixture.TEST_SAMPLE_TYPE_DTO.code, selected: false },
      { level: 0, text: 'Collection Types', selected: false },
      { level: 0, text: 'Data Set Types', selected: false },
      { level: 0, text: 'Material Types', selected: false },
      { level: 0, text: 'Vocabulary Types', selected: false },
      { level: 0, text: 'Property Types', selected: false }
    ],
    buttons: {
      add: {
        enabled: false
      },
      remove: {
        enabled: true
      }
    }
  })
}
