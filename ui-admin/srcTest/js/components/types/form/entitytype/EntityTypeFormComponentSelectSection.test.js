import EntityTypeFormComponentTest from '@srcTest/js/components/types/form/entitytype/EntityTypeFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new EntityTypeFormComponentTest()
  common.beforeEach()
})

describe(EntityTypeFormComponentTest.SUITE, () => {
  test('select section', testSelectSection)
})

async function testSelectSection() {
  const form = await common.mountExisting(fixture.TEST_SAMPLE_TYPE_DTO)

  form.getPreview().getSections()[1].click()
  await form.update()

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    parameters: {
      section: {
        title: 'Section',
        name: {
          label: 'Name',
          value: 'TEST_SECTION_2',
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: null
    }
  })
}
