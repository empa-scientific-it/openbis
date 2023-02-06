import EntityTypeFormComponentTest from '@srcTest/js/components/types/form/entitytype/EntityTypeFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new EntityTypeFormComponentTest()
  common.beforeEach()
})

describe(EntityTypeFormComponentTest.SUITE, () => {
  test('add section', testAddSection)
})

async function testAddSection() {
  common.facade.loadValidationPlugins.mockReturnValue(
    Promise.resolve([fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin])
  )

  const form = await common.mountExisting(fixture.TEST_SAMPLE_TYPE_DTO)

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
    buttons: {
      edit: {
        enabled: true
      },
      addSection: null,
      addProperty: null,
      remove: null,
      save: null,
      cancel: null,
      message: null
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getPreview().getSections()[0].click()
  form.getButtons().getAddSection().click()
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1'
        },
        {
          name: null
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
          value: null,
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      edit: null,
      addSection: {
        enabled: true
      },
      addProperty: {
        enabled: true
      },
      remove: {
        enabled: true
      },
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })
}
