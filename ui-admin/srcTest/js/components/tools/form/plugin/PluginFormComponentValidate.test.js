import PluginFormComponentTest from '@srcTest/js/components/tools/form/plugin/PluginFormComponentTest.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new PluginFormComponentTest()
  common.beforeEach()
})

describe(PluginFormComponentTest.SUITE, () => {
  test('validate DYNAMIC_PROPERTY', async () => {
    await testValidate(openbis.PluginType.DYNAMIC_PROPERTY)
  })
  test('validate ENTITY_VALIDATION', async () => {
    await testValidate(openbis.PluginType.ENTITY_VALIDATION)
  })
})

async function testValidate(pluginType) {
  const form = await common.mountNew(pluginType)

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    script: {
      title: 'Script',
      script: {
        label: 'Script',
        value: null,
        error: 'Script cannot be empty',
        enabled: true,
        mode: 'edit'
      }
    },
    parameters: {
      title:
        pluginType === openbis.PluginType.DYNAMIC_PROPERTY
          ? 'New Dynamic Property Plugin'
          : 'New Entity Validation Plugin',
      name: {
        label: 'Name',
        value: null,
        error: 'Name cannot be empty',
        enabled: true,
        mode: 'edit'
      },
      entityKind: {
        label: 'Entity Kind',
        value: null,
        enabled: true,
        mode: 'edit'
      },
      description: {
        label: 'Description',
        value: null,
        enabled: true,
        mode: 'edit'
      }
    },
    buttons: {
      save: {
        enabled: true
      },
      edit: null,
      cancel: null,
      message: null
    }
  })
}
