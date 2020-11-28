import PluginFormComponentTest from '@srcTest/js/components/tools/form/plugin/PluginFormComponentTest.js'
import PluginFormTestData from '@srcTest/js/components/tools/form/plugin/PluginFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new PluginFormComponentTest()
  common.beforeEach()
})

describe(PluginFormComponentTest.SUITE, () => {
  test('load new DYNAMIC_PROPERTY', async () => {
    await testLoadNew(openbis.PluginType.DYNAMIC_PROPERTY)
  })
  test('load new ENTITY_VALIDATION', async () => {
    await testLoadNew(openbis.PluginType.ENTITY_VALIDATION)
  })
  test('load existing DYNAMIC_PROPERTY JYTHON', async () => {
    const { testDynamicPropertyJythonPlugin } = PluginFormTestData
    await testLoadExistingJython(testDynamicPropertyJythonPlugin)
  })
  test('load existing ENTITY_VALIDATION JYTHON', async () => {
    const { testEntityValidationJythonPlugin } = PluginFormTestData
    await testLoadExistingJython(testEntityValidationJythonPlugin)
  })
  test('load existing DYNAMIC_PROPERTY PREDEPLOYED', async () => {
    const { testDynamicPropertyPredeployedPlugin } = PluginFormTestData
    await testLoadExistingPredeployed(testDynamicPropertyPredeployedPlugin)
  })
  test('load existing ENTITY_VALIDATION PREDEPLOYED', async () => {
    const { testEntityValidationPredeployedPlugin } = PluginFormTestData
    await testLoadExistingPredeployed(testEntityValidationPredeployedPlugin)
  })
})

async function testLoadNew(pluginType) {
  const form = await common.mountNew(pluginType)

  form.expectJSON({
    script: {
      title: 'Script',
      script: {
        label: 'Script',
        value: null,
        enabled: true,
        mode: 'edit'
      }
    },
    parameters: {
      title: 'Plugin',
      name: {
        label: 'Name',
        value: null,
        enabled: true,
        mode: 'edit'
      },
      entityKind: {
        label: 'Entity Kind',
        value: null,
        options: [
          { value: 'MATERIAL' },
          { value: 'EXPERIMENT' },
          { value: 'SAMPLE' },
          { value: 'DATA_SET' }
        ],
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

async function testLoadExistingJython(plugin) {
  const form = await common.mountExisting(plugin)

  form.expectJSON({
    script: {
      title: 'Script',
      script: {
        label: 'Script',
        value: plugin.script,
        mode: 'view'
      }
    },
    parameters: {
      title: 'Plugin',
      name: {
        label: 'Name',
        value: plugin.getName(),
        mode: 'view'
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
        mode: 'view'
      },
      description: {
        label: 'Description',
        value: plugin.getDescription(),
        mode: 'view'
      }
    },
    buttons: {
      edit: {
        enabled: true
      },
      save: null,
      cancel: null,
      message: null
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    script: {
      title: 'Script',
      script: {
        label: 'Script',
        value: plugin.script,
        enabled: true,
        mode: 'edit'
      }
    },
    parameters: {
      title: 'Plugin',
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
        value: plugin.getDescription(),
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
      message: null
    }
  })
}

async function testLoadExistingPredeployed(plugin) {
  const form = await common.mountExisting(plugin)

  form.expectJSON({
    script: null,
    parameters: {
      title: 'Plugin',
      messages: [
        {
          text: 'The plugin is disabled.',
          type: 'warning'
        },
        {
          text:
            'This is a predeployed Java plugin. Its parameters and logic are defined in the plugin Java class and therefore cannot be changed from the UI.',
          type: 'info'
        }
      ],
      name: {
        label: 'Name',
        value: plugin.getName(),
        mode: 'view'
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
        mode: 'view'
      },
      description: {
        label: 'Description',
        value: plugin.getDescription(),
        mode: 'view'
      }
    },
    buttons: {
      edit: null,
      save: null,
      cancel: null,
      message: null
    }
  })
}
