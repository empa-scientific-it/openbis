import openbis from '@srcTest/js/services/openbis.js'

const testDynamicPropertyJythonPlugin = new openbis.Plugin()
testDynamicPropertyJythonPlugin.setName('TEST_DYNAMIC_PROPERTY_JYTHON')
testDynamicPropertyJythonPlugin.setPluginKind(openbis.PluginKind.JYTHON)
testDynamicPropertyJythonPlugin.setPluginType(
  openbis.PluginType.DYNAMIC_PROPERTY
)

const testDynamicPropertyPredeployedPlugin = new openbis.Plugin()
testDynamicPropertyPredeployedPlugin.setName(
  'TEST_DYNAMIC_PROPERTY_PREDEPLOYED'
)
testDynamicPropertyPredeployedPlugin.setPluginKind(
  openbis.PluginKind.PREDEPLOYED
)
testDynamicPropertyPredeployedPlugin.setPluginType(
  openbis.PluginType.DYNAMIC_PROPERTY
)

const testManagedPropertyJythonPlugin = new openbis.Plugin()
testManagedPropertyJythonPlugin.setName('TEST_MANAGED_PROPERTY_JYTHON')
testManagedPropertyJythonPlugin.setPluginKind(openbis.PluginKind.JYTHON)
testManagedPropertyJythonPlugin.setPluginType(
  openbis.PluginType.MANAGED_PROPERTY
)

const testEntityValidationJythonPlugin = new openbis.Plugin()
testEntityValidationJythonPlugin.setName('TEST_ENTITY_VALIDATION_JYTHON')
testEntityValidationJythonPlugin.setPluginKind(openbis.PluginKind.JYTHON)
testEntityValidationJythonPlugin.setPluginType(
  openbis.PluginType.ENTITY_VALIDATION
)

const testQuery = new openbis.Query()
testQuery.setName('TEST_QUERY')

export default {
  testDynamicPropertyJythonPlugin,
  testDynamicPropertyPredeployedPlugin,
  testManagedPropertyJythonPlugin,
  testEntityValidationJythonPlugin,
  testQuery
}
