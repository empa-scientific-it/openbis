import openbis from '@srcTest/js/services/openbis.js'

const testRegistrator = new openbis.Person()
testRegistrator.setUserId('test_registrator')

const anotherRegistrator = new openbis.Person()
anotherRegistrator.setUserId('another_registrator')

const testPlugin = new openbis.Plugin()
testPlugin.setName('TEST_PLUGIN')
testPlugin.setDescription('test description')
testPlugin.setPluginType(openbis.PluginType.DYNAMIC_PROPERTY)
testPlugin.setPluginKind(openbis.PluginKind.JYTHON)
testPlugin.setEntityKinds([openbis.EntityKind.SAMPLE])
testPlugin.setRegistrator(testRegistrator)

const testPlugin2 = new openbis.Plugin()
testPlugin2.setName('TEST_PLUGIN_2')
testPlugin2.setPluginType(openbis.PluginType.DYNAMIC_PROPERTY)
testPlugin2.setDescription('test description 2')

const anotherPlugin = new openbis.Plugin()
anotherPlugin.setName('ANOTHER_PLUGIN')
anotherPlugin.setPluginType(openbis.PluginType.DYNAMIC_PROPERTY)
anotherPlugin.setDescription('another description')

const anotherPlugin2 = new openbis.Plugin()
anotherPlugin2.setName('ANOTHER_PLUGIN_2')
anotherPlugin2.setDescription('another description 2')

const testQuery = new openbis.Query()
testQuery.setName('TEST_QUERY')
testQuery.setDescription('test description')
testQuery.setDatabaseLabel('Test Database')
testQuery.setQueryType(openbis.QueryType.GENERIC)
testQuery.setEntityTypeCodePattern('Test Pattern')
testQuery.setPublic(true)
testQuery.setRegistrator(testRegistrator)

const anotherQuery = new openbis.Query()
anotherQuery.setName('ANOTHER_QUERY')
anotherQuery.setDescription('another description')

export default {
  testPlugin,
  testPlugin2,
  anotherPlugin,
  anotherPlugin2,
  testQuery,
  anotherQuery
}
