import PluginFormComponentTest from '@srcTest/js/components/tools/form/plugin/PluginFormComponentTest.js'
import PluginFormTestData from '@srcTest/js/components/tools/form/plugin/PluginFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new PluginFormComponentTest()
  common.beforeEach()
})

describe(PluginFormComponentTest.SUITE, () => {
  test('save create DYNAMIC_PROPERTY', async () => {
    await testSaveCreate(openbis.PluginType.DYNAMIC_PROPERTY)
  })
  test('save create ENTITY_VALIDATION', async () => {
    await testSaveCreate(openbis.PluginType.ENTITY_VALIDATION)
  })
  test('save update DYNAMIC_PROPERTY', async () => {
    const { testDynamicPropertyJythonPlugin } = PluginFormTestData
    await testSaveUpdate(testDynamicPropertyJythonPlugin)
  })
  test('save update ENTITY_VALIDATION', async () => {
    const { testEntityValidationJythonPlugin } = PluginFormTestData
    await testSaveUpdate(testEntityValidationJythonPlugin)
  })
})

async function testSaveCreate(pluginType) {
  const form = await common.mountNew(pluginType)

  form.getParameters().getName().change('test-plugin')
  await form.update()

  form.getParameters().getEntityKind().change(openbis.EntityKind.SAMPLE)
  await form.update()

  form.getParameters().getDescription().change('test description')
  await form.update()

  form.getScript().getScript().change('test script')
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    createPluginOperation({
      pluginType,
      name: 'test-plugin',
      entityKind: openbis.EntityKind.SAMPLE,
      description: 'test description',
      script: 'test script'
    })
  ])
}

async function testSaveUpdate(plugin) {
  const form = await common.mountExisting(plugin)

  form.getButtons().getEdit().click()
  await form.update()

  form.getParameters().getDescription().change('updated description')
  await form.update()

  form.getScript().getScript().change('updated script')
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    updatePluginOperation({
      name: plugin.getName(),
      description: 'updated description',
      script: 'updated script'
    })
  ])
}

function createPluginOperation({
  pluginType,
  name,
  entityKind,
  description,
  script
}) {
  const creation = new openbis.PluginCreation()
  creation.setPluginType(pluginType)
  creation.setEntityKind(entityKind)
  creation.setName(name)
  creation.setDescription(description)
  creation.setScript(script)
  return new openbis.CreatePluginsOperation([creation])
}

function updatePluginOperation({ name, description, script }) {
  const update = new openbis.PluginUpdate()
  update.setPluginId(new openbis.PluginPermId(name))
  update.setDescription(description)
  update.setScript(script)
  return new openbis.UpdatePluginsOperation([update])
}

function expectExecuteOperations(expectedOperations) {
  expect(common.facade.executeOperations).toHaveBeenCalledTimes(1)
  const actualOperations = common.facade.executeOperations.mock.calls[0][0]
  expect(actualOperations.length).toEqual(expectedOperations.length)
  actualOperations.forEach((actualOperation, index) => {
    expect(actualOperation).toMatchObject(expectedOperations[index])
  })
}
