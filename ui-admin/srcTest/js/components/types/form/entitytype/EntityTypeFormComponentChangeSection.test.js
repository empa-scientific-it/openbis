import EntityTypeFormComponentTest from '@srcTest/js/components/types/form/entitytype/EntityTypeFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new EntityTypeFormComponentTest()
  common.beforeEach()
})

describe(EntityTypeFormComponentTest.SUITE, () => {
  test('change section', testChangeSection)
})

async function testChangeSection() {
  common.facade.loadValidationPlugins.mockReturnValue(
    Promise.resolve([fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin])
  )

  const form = await common.mountExisting(fixture.TEST_SAMPLE_TYPE_DTO)

  form.getButtons().getEdit().click()
  await form.update()

  form.getPreview().getSections()[1].click()
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1'
        },
        {
          name: 'TEST_SECTION_2'
        }
      ]
    },
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

  form.getParameters().getSection().getName().change('NEW_NAME')
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1'
        },
        {
          name: 'NEW_NAME'
        }
      ]
    },
    parameters: {
      section: {
        title: 'Section',
        name: {
          label: 'Name',
          value: 'NEW_NAME',
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })
}
