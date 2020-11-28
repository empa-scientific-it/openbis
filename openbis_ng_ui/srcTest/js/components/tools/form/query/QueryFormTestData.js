import openbis from '@srcTest/js/services/openbis.js'

const testDatabaseId = new openbis.QueryDatabaseName('test database')
const anotherDatabaseId = new openbis.QueryDatabaseName('another database')

const genericQuery = new openbis.Query()
genericQuery.setName('generic query')
genericQuery.setDescription('generic query description')
genericQuery.setDatabaseId(testDatabaseId)
genericQuery.setQueryType(openbis.QueryType.GENERIC)
genericQuery.setSql('select * from persons;')
genericQuery.setPublic(true)

const sampleQuery = new openbis.Query()
sampleQuery.setName('sample query')
sampleQuery.setDescription('sample query description')
sampleQuery.setDatabaseId(anotherDatabaseId)
sampleQuery.setQueryType(openbis.QueryType.SAMPLE)
sampleQuery.setSql('select * from samples;')
sampleQuery.setPublic(false)

const dataSetQuery = new openbis.Query()
dataSetQuery.setName('data set query')
dataSetQuery.setDescription('data set query description')
dataSetQuery.setDatabaseId(anotherDatabaseId)
dataSetQuery.setQueryType(openbis.QueryType.DATA_SET)
dataSetQuery.setSql('select * from data;')
dataSetQuery.setPublic(true)

export default {
  testDatabaseId,
  genericQuery,
  sampleQuery,
  dataSetQuery
}
