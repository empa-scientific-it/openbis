import PluginFormComponentTest from '@srcTest/js/components/tools/form/plugin/PluginFormComponentTest.js'
import PluginFormTestData from '@srcTest/js/components/tools/form/plugin/PluginFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new PluginFormComponentTest()
  common.beforeEach()
})

describe(PluginFormComponentTest.SUITE, () => {
  test('change DYNAMIC_PROPERTY', async () => {
    const { testDynamicPropertyJythonPlugin } = PluginFormTestData
    await testChange(testDynamicPropertyJythonPlugin)
  })
  test('change ENTITY_VALIDATION', async () => {
    const { testEntityValidationJythonPlugin } = PluginFormTestData
    await testChange(testEntityValidationJythonPlugin)
  })
})

async function testChange(plugin) {
  const form = await common.mountExisting(plugin)

  form.getButtons().getEdit().click()
  await form.update()

  form.getScript().getScript().change('updated script')
  await form.update()

  form.getParameters().getDescription().change('updated description')
  await form.update()

  form.expectJSON({
    script: {
      title: 'Script',
      script: {
        label: 'Script',
        value: 'updated script',
        enabled: true,
        mode: 'edit'
      }
    },
    parameters: {
      title:
        plugin.getPluginType() === openbis.PluginType.DYNAMIC_PROPERTY
          ? 'Dynamic Property Plugin'
          : 'Entity Validation Plugin',
      name: {
        label: 'Name',
        value: plugin.getName(),
        enabled: false,
        mode: 'edit'
      },
      entityKind: {
        label: 'Entity Kind',
        value:
          plugin.getEntityKinds().length === 1
            ? plugin.getEntityKinds()[0]
            : null,
        options: [
          { value: 'MATERIAL' },
          { value: 'EXPERIMENT' },
          { value: 'SAMPLE' },
          { value: 'DATA_SET' }
        ],
        enabled: false,
        mode: 'edit'
      },
      description: {
        label: 'Description',
        value: 'updated description',
        enabled: true,
        mode: 'edit'
      }
    },
    buttons: {
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      edit: null,
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })
}
