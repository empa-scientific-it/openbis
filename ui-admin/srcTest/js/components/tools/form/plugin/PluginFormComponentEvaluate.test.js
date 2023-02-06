import PluginFormComponentTest from '@srcTest/js/components/tools/form/plugin/PluginFormComponentTest.js'
import PluginFormTestData from '@srcTest/js/components/tools/form/plugin/PluginFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new PluginFormComponentTest()
  common.beforeEach()
})

describe(PluginFormComponentTest.SUITE, () => {
  test('evaluate new DYNAMIC_PROPERTY', async () => {
    await testEvaluateNew(openbis.PluginType.DYNAMIC_PROPERTY)
  })
  test('evaluate new ENTITY_VALIDATION', async () => {
    await testEvaluateNew(openbis.PluginType.ENTITY_VALIDATION)
  })
  test('evaluate existing DYNAMIC_PROPERTY', async () => {
    const { testDynamicPropertyJythonPlugin } = PluginFormTestData
    await testEvaluateExisting(testDynamicPropertyJythonPlugin)
  })
  test('evaluate existing ENTITY_VALIDATION', async () => {
    const { testEntityValidationJythonPlugin } = PluginFormTestData
    await testEvaluateExisting(testEntityValidationJythonPlugin)
  })
})

async function testEvaluateNew(pluginType) {
  let result = null
  let resultObject = null

  if (pluginType === openbis.PluginType.DYNAMIC_PROPERTY) {
    result = 'test value'
    resultObject = new openbis.DynamicPropertyPluginEvaluationResult(result)
  } else if (pluginType === openbis.PluginType.ENTITY_VALIDATION) {
    result = 'test error'
    resultObject = new openbis.EntityValidationPluginEvaluationResult(result)
  }

  common.facade.evaluatePlugin.mockReturnValue(Promise.resolve(resultObject))

  const form = await common.mountNew(pluginType)

  form.expectJSON({
    parameters: {
      entityKind: {
        value: null
      }
    },
    evaluateParameters: {
      entityKind: {
        value: null,
        enabled: true
      },
      entity: {
        value: null,
        enabled: false
      }
    },
    evaluateResults: {
      title: null,
      result: null
    }
  })

  form.getParameters().getEntityKind().change(openbis.EntityKind.SAMPLE)
  await form.update()

  form.getEvaluateParameters().getEntity().change({
    entityKind: openbis.EntityKind.SAMPLE,
    entityId: '/TEST_SPACE/TEST_SAMPLE'
  })
  await form.update()

  form.expectJSON({
    parameters: {
      entityKind: {
        value: openbis.EntityKind.SAMPLE
      }
    },
    evaluateParameters: {
      entityKind: {
        value: openbis.EntityKind.SAMPLE,
        enabled: false
      },
      entity: {
        value: {
          entityKind: openbis.EntityKind.SAMPLE,
          entityId: '/TEST_SPACE/TEST_SAMPLE'
        },
        enabled: true
      }
    },
    evaluateResults: {
      title: null,
      result: null
    }
  })

  form.getParameters().getEntityKind().change(null)
  await form.update()

  form.expectJSON({
    parameters: {
      entityKind: {
        value: null
      }
    },
    evaluateParameters: {
      entityKind: {
        value: openbis.EntityKind.SAMPLE,
        enabled: true
      },
      entity: {
        value: {
          entityKind: openbis.EntityKind.SAMPLE,
          entityId: '/TEST_SPACE/TEST_SAMPLE'
        },
        enabled: true
      }
    },
    evaluateResults: {
      title: null,
      result: null
    }
  })

  form.getParameters().getEntityKind().change(openbis.EntityKind.EXPERIMENT)
  await form.update()

  form.expectJSON({
    parameters: {
      entityKind: {
        value: openbis.EntityKind.EXPERIMENT
      }
    },
    evaluateParameters: {
      entityKind: {
        value: openbis.EntityKind.EXPERIMENT,
        enabled: false
      },
      entity: {
        value: null,
        enabled: true
      }
    },
    evaluateResults: {
      title: null,
      result: null
    }
  })

  form.getEvaluateParameters().getEntity().change({
    entityKind: openbis.EntityKind.EXPERIMENT,
    entityId: '/TEST_SPACE/TEST_PROJECT/TEST_EXPERIMENT'
  })
  await form.update()

  form.getButtons().getEvaluate().click()
  await form.update()

  form.expectJSON({
    parameters: {
      entityKind: {
        value: openbis.EntityKind.EXPERIMENT
      }
    },
    evaluateParameters: {
      entityKind: {
        value: openbis.EntityKind.EXPERIMENT,
        enabled: false
      },
      entity: {
        value: {
          entityKind: openbis.EntityKind.EXPERIMENT,
          entityId: '/TEST_SPACE/TEST_PROJECT/TEST_EXPERIMENT'
        },
        enabled: true
      }
    },
    evaluateResults: {
      title: 'Result',
      result: result
    }
  })
}

async function testEvaluateExisting(plugin) {
  let result = null
  let resultObject = null

  if (plugin.pluginType === openbis.PluginType.DYNAMIC_PROPERTY) {
    result = 'test value'
    resultObject = new openbis.DynamicPropertyPluginEvaluationResult(result)
  } else if (plugin.pluginType === openbis.PluginType.ENTITY_VALIDATION) {
    result = 'test error'
    resultObject = new openbis.EntityValidationPluginEvaluationResult(result)
  }

  common.facade.evaluatePlugin.mockReturnValue(Promise.resolve(resultObject))

  const form = await common.mountExisting(plugin)

  form.getEvaluateParameters().getEntity().change({
    entityKind: plugin.getEntityKinds()[0],
    entityId: 'TEST_ENTITY_ID'
  })
  await form.update()

  form.expectJSON({
    parameters: {
      entityKind: {
        value: plugin.getEntityKinds()[0]
      }
    },
    evaluateParameters: {
      entityKind: {
        value: plugin.getEntityKinds()[0],
        enabled: false
      },
      entity: {
        value: {
          entityKind: plugin.getEntityKinds()[0],
          entityId: 'TEST_ENTITY_ID'
        },
        enabled: true
      }
    },
    evaluateResults: {
      title: null,
      result: null
    },
    buttons: {
      edit: {
        enabled: true
      },
      evaluate: {
        enabled: true
      },
      save: null,
      cancel: null,
      message: null
    }
  })

  form.getButtons().getEvaluate().click()
  await form.update()

  form.expectJSON({
    parameters: {
      entityKind: {
        value: plugin.getEntityKinds()[0]
      }
    },
    evaluateParameters: {
      entityKind: {
        value: plugin.getEntityKinds()[0],
        enabled: false
      },
      entity: {
        value: {
          entityKind: plugin.getEntityKinds()[0],
          entityId: 'TEST_ENTITY_ID'
        },
        enabled: true
      }
    },
    evaluateResults: {
      title: 'Result',
      result: result
    },
    buttons: {
      edit: {
        enabled: true
      },
      evaluate: {
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
    parameters: {
      entityKind: {
        value: plugin.getEntityKinds()[0]
      }
    },
    evaluateParameters: {
      entityKind: {
        value: plugin.getEntityKinds()[0],
        enabled: false
      },
      entity: {
        value: null,
        enabled: true
      }
    },
    evaluateResults: {
      title: null,
      result: null
    },
    buttons: {
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      evaluate: {
        enabled: true
      },
      edit: null,
      message: null
    }
  })

  form.getEvaluateParameters().getEntity().change({
    entityKind: plugin.getEntityKinds()[0],
    entityId: 'TEST_ENTITY_ID_2'
  })
  await form.update()

  form.expectJSON({
    parameters: {
      entityKind: {
        value: plugin.getEntityKinds()[0]
      }
    },
    evaluateParameters: {
      entityKind: {
        value: plugin.getEntityKinds()[0],
        enabled: false
      },
      entity: {
        value: {
          entityKind: plugin.getEntityKinds()[0],
          entityId: 'TEST_ENTITY_ID_2'
        },
        enabled: true
      }
    },
    evaluateResults: {
      title: null,
      result: null
    },
    buttons: {
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      evaluate: {
        enabled: true
      },
      edit: null,
      message: null
    }
  })

  form.getButtons().getEvaluate().click()
  await form.update()

  form.expectJSON({
    parameters: {
      entityKind: {
        value: plugin.getEntityKinds()[0]
      }
    },
    evaluateParameters: {
      entityKind: {
        value: plugin.getEntityKinds()[0],
        enabled: false
      },
      entity: {
        value: {
          entityKind: plugin.getEntityKinds()[0],
          entityId: 'TEST_ENTITY_ID_2'
        },
        enabled: true
      }
    },
    evaluateResults: {
      title: 'Result',
      result: result
    },
    buttons: {
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      evaluate: {
        enabled: true
      },
      edit: null,
      message: null
    }
  })
}
