import EntityTypeFormComponentTest from '@srcTest/js/components/types/form/entitytype/EntityTypeFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new EntityTypeFormComponentTest()
  common.beforeEach()
})

describe(EntityTypeFormComponentTest.SUITE, () => {
  test('remove section', testRemoveSection)
})

async function testRemoveSection() {
  common.facade.loadValidationPlugins.mockReturnValue(
    Promise.resolve([fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin])
  )

  const form = await common.mountExisting(fixture.TEST_SAMPLE_TYPE_DTO)

  form.getButtons().getEdit().click()
  await form.update()

  form.getPreview().getSections()[0].click()
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
    buttons: {
      message: null
    },
    removeSectionDialog: null
  })

  form.getButtons().getRemove().click()
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
    buttons: {
      message: null
    },
    removeSectionDialog: {
      title: 'Are you sure you want to remove "TEST_SECTION_1"?',
      content: `This section contains only property assignments which are not yet used by any entities of "${fixture.TEST_SAMPLE_TYPE_DTO.getCode()}" type.`,
      type: 'info'
    }
  })

  form.getRemoveSectionDialog().getConfirm().click()
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_2'
        }
      ]
    },
    buttons: {
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    },
    removeSectionDialog: null
  })
}
