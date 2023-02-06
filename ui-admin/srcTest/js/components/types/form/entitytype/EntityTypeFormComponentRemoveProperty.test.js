import EntityTypeFormComponentTest from '@srcTest/js/components/types/form/entitytype/EntityTypeFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new EntityTypeFormComponentTest()
  common.beforeEach()
})

describe(EntityTypeFormComponentTest.SUITE, () => {
  test('remove property', testRemoveProperty)
})

async function testRemoveProperty() {
  common.facade.loadValidationPlugins.mockReturnValue(
    Promise.resolve([fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin])
  )
  common.facade.loadPropertyUsages.mockReturnValue(Promise.resolve({}))

  const form = await common.mountExisting(fixture.TEST_SAMPLE_TYPE_DTO)

  form.getButtons().getEdit().click()
  await form.update()

  form.getPreview().getSections()[1].getProperties()[0].click()
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1',
          properties: [{ code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }]
        },
        {
          name: 'TEST_SECTION_2',
          properties: [
            { code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
            { code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
          ]
        }
      ]
    },
    buttons: {
      message: null
    },
    removePropertyDialog: null
  })

  form.getButtons().getRemove().click()
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1',
          properties: [{ code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }]
        },
        {
          name: 'TEST_SECTION_2',
          properties: [
            { code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
            { code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
          ]
        }
      ]
    },
    buttons: {
      message: null
    },
    removePropertyDialog: {
      title: `Are you sure you want to remove "${fixture.TEST_PROPERTY_TYPE_2_DTO.getCode()}"?`,
      content: `This property assignment is not yet used by any entities of "${fixture.TEST_SAMPLE_TYPE_DTO.getCode()}" type.`,
      type: 'info'
    }
  })

  form.getRemovePropertyDialog().getConfirm().click()
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1',
          properties: [{ code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }]
        },
        {
          name: 'TEST_SECTION_2',
          properties: [{ code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }]
        }
      ]
    },
    buttons: {
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    },
    removePropertyDialog: null
  })
}
