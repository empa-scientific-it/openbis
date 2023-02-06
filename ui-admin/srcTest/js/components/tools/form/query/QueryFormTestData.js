import openbis from '@srcTest/js/services/openbis.js'

const testSpace = new openbis.Space()
testSpace.setCode('TEST')

const testDatabase = new openbis.QueryDatabase()
testDatabase.setName('test database name')
testDatabase.setLabel('test database label')
testDatabase.setPermId(new openbis.QueryDatabaseName(testDatabase.getName()))
testDatabase.setSpace(testSpace)

const anotherDatabase = new openbis.QueryDatabase()
anotherDatabase.setName('another database name')
anotherDatabase.setLabel('another database label')
anotherDatabase.setPermId(
  new openbis.QueryDatabaseName(anotherDatabase.getName())
)

const genericQuery = new openbis.Query()
genericQuery.setName('generic query')
genericQuery.setDescription('generic query description')
genericQuery.setDatabaseId(testDatabase.getPermId())
genericQuery.setQueryType(openbis.QueryType.GENERIC)
genericQuery.setSql('select * from persons;')
genericQuery.setPublic(true)

const sampleQuery = new openbis.Query()
sampleQuery.setName('sample query')
sampleQuery.setDescription('sample query description')
sampleQuery.setDatabaseId(anotherDatabase.getPermId())
sampleQuery.setQueryType(openbis.QueryType.SAMPLE)
sampleQuery.setSql('select * from samples;')
sampleQuery.setPublic(false)

const dataSetQuery = new openbis.Query()
dataSetQuery.setName('data set query')
dataSetQuery.setDescription('data set query description')
dataSetQuery.setDatabaseId(anotherDatabase.getPermId())
dataSetQuery.setQueryType(openbis.QueryType.DATA_SET)
dataSetQuery.setSql('select * from data;')
dataSetQuery.setPublic(true)

const queryWithParameters = new openbis.Query()
queryWithParameters.setName('query with parameters')
queryWithParameters.setDescription('query with parameters description')
queryWithParameters.setDatabaseId(anotherDatabase.getPermId())
queryWithParameters.setQueryType(openbis.QueryType.EXPERIMENT)
queryWithParameters.setSql(
  'select id, code, perm_id as experiment_key from experiments where code = ${code} or perm_id = ${permId}'
)
queryWithParameters.setPublic(true)

const testResultColumns = [
  new openbis.TableColumn('id'),
  new openbis.TableColumn('code'),
  new openbis.TableColumn('Experiment') // server returns "Experiment" for "experiment_key" column
]
const testResultRows = [
  [
    new openbis.TableStringCell('1'),
    new openbis.TableStringCell('code_1'),
    new openbis.TableStringCell('perm_id_1')
  ],
  [
    new openbis.TableStringCell('2'),
    new openbis.TableStringCell('code_2'),
    new openbis.TableStringCell('perm_id_2')
  ]
]
const testResult = new openbis.TableModel()
testResult.setColumns(testResultColumns)
testResult.setRows(testResultRows)

export default {
  testDatabase,
  anotherDatabase,
  genericQuery,
  sampleQuery,
  dataSetQuery,
  queryWithParameters,
  testResult
}
