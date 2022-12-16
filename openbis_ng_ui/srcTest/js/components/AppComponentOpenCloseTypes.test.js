import AppComponentTest from '@srcTest/js/components/AppComponentTest.js'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new AppComponentTest()
  common.beforeEach()
})

describe(AppComponentTest.SUITE, () => {
  test('open/close types', testOpenCloseTypes)
})

async function testOpenCloseTypes() {
  openbis.mockSearchSampleTypes([
    fixture.TEST_SAMPLE_TYPE_DTO,
    fixture.ANOTHER_SAMPLE_TYPE_DTO
  ])

  const app = await common.mount()

  await common.login(app)

  app.getTypes().getBrowser().clickAutoShowSelected()
  await app.update()

  app.getTypes().getBrowser().getNodes()[0].getIcon().click()
  await app.update()

  app.getTypes().getBrowser().getNodes()[2].click()
  await app.update()

  app.getTypes().getBrowser().getNodes()[1].click()
  await app.update()

  app.expectJSON({
    types: {
      browser: {
        nodes: [
          { level: 0, text: 'Object Types', selected: false },
          {
            level: 1,
            text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code,
            selected: true
          },
          {
            level: 1,
            text: fixture.TEST_SAMPLE_TYPE_DTO.code,
            selected: false
          },
          { level: 0, text: 'Collection Types', selected: false },
          { level: 0, text: 'Data Set Types', selected: false },
          { level: 0, text: 'Material Types', selected: false },
          { level: 0, text: 'Vocabulary Types', selected: false },
          { level: 0, text: 'Property Types', selected: false }
        ]
      },
      content: {
        tabs: [
          {
            label: 'Object Type: ' + fixture.TEST_SAMPLE_TYPE_DTO.code,
            selected: false
          },
          {
            label: 'Object Type: ' + fixture.ANOTHER_SAMPLE_TYPE_DTO.code,
            selected: true
          }
        ]
      }
    }
  })

  app.getTypes().getContent().getTabs()[1].getCloseIcon().click()
  await app.update()

  app.expectJSON({
    types: {
      browser: {
        nodes: [
          { level: 0, text: 'Object Types', selected: false },
          {
            level: 1,
            text: fixture.ANOTHER_SAMPLE_TYPE_DTO.code,
            selected: false
          },
          {
            level: 1,
            text: fixture.TEST_SAMPLE_TYPE_DTO.code,
            selected: true
          },
          { level: 0, text: 'Collection Types', selected: false },
          { level: 0, text: 'Data Set Types', selected: false },
          { level: 0, text: 'Material Types', selected: false },
          { level: 0, text: 'Vocabulary Types', selected: false },
          { level: 0, text: 'Property Types', selected: false }
        ]
      },
      content: {
        tabs: [
          {
            label: 'Object Type: ' + fixture.TEST_SAMPLE_TYPE_DTO.code,
            selected: true
          }
        ]
      }
    }
  })
}
