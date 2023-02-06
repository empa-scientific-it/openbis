import QueryFormComponentTest from '@srcTest/js/components/tools/form/query/QueryFormComponentTest.js'
import QueryFormTestData from '@srcTest/js/components/tools/form/query/QueryFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new QueryFormComponentTest()
  common.beforeEach()
})

describe(QueryFormComponentTest.SUITE, () => {
  test('save create SAMPLE query', async () => {
    const { sampleQuery } = QueryFormTestData
    await testSaveCreate(sampleQuery)
  })
  test('save create GENERIC query', async () => {
    const { genericQuery } = QueryFormTestData
    await testSaveCreate(genericQuery)
  })
  test('save update SAMPLE query', async () => {
    const { sampleQuery, genericQuery } = QueryFormTestData
    await testSaveUpdate(sampleQuery, genericQuery)
  })
  test('save update GENERIC query', async () => {
    const { genericQuery, sampleQuery } = QueryFormTestData
    await testSaveUpdate(genericQuery, sampleQuery)
  })
})

async function testSaveCreate(query) {
  const { testDatabase, anotherDatabase } = QueryFormTestData

  common.facade.loadQueryDatabases.mockReturnValue(
    Promise.resolve([testDatabase, anotherDatabase])
  )

  const form = await common.mountNew()

  form.getSql().getSql().change(query.getSql())
  await form.update()

  form.getParameters().getName().change(query.getName())
  await form.update()

  form.getParameters().getDescription().change(query.getDescription())
  await form.update()

  form.getParameters().getDatabase().change(query.getDatabaseId().getName())
  await form.update()

  form.getParameters().getQueryType().change(query.getQueryType())
  await form.update()

  if (form.getParameters().getEntityTypeCodePattern().exists()) {
    form
      .getParameters()
      .getEntityTypeCodePattern()
      .change(query.getEntityTypeCodePattern())
    await form.update()
  }

  form.getParameters().getPublicFlag().change(query.isPublic())
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    createQueryOperation({
      name: query.getName(),
      description: query.getDescription(),
      databaseName: query.getDatabaseId().getName(),
      queryType: query.getQueryType(),
      entityTypeCodePattern: query.getEntityTypeCodePattern(),
      sql: query.getSql(),
      publicFlag: query.isPublic()
    })
  ])
}

async function testSaveUpdate(query, update) {
  const { testDatabase, anotherDatabase } = QueryFormTestData

  common.facade.loadQueryDatabases.mockReturnValue(
    Promise.resolve([testDatabase, anotherDatabase])
  )

  const form = await common.mountExisting(query)

  form.getButtons().getEdit().click()
  await form.update()

  form.getSql().getSql().change(update.getSql())
  await form.update()

  form.getParameters().getDescription().change(update.getDescription())
  await form.update()

  form.getParameters().getDatabase().change(update.getDatabaseId().getName())
  await form.update()

  form.getParameters().getQueryType().change(update.getQueryType())
  await form.update()

  if (form.getParameters().getEntityTypeCodePattern().exists()) {
    form
      .getParameters()
      .getEntityTypeCodePattern()
      .change(update.getEntityTypeCodePattern())
    await form.update()
  }

  form.getParameters().getPublicFlag().change(update.isPublic())
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    updateQueryOperation({
      name: query.getName(),
      description: update.getDescription(),
      databaseName: update.getDatabaseId().getName(),
      queryType: update.getQueryType(),
      entityTypeCodePattern: update.getEntityTypeCodePattern(),
      sql: update.getSql(),
      publicFlag: update.isPublic()
    })
  ])
}

function createQueryOperation({
  name,
  description,
  databaseName,
  queryType,
  entityTypeCodePattern,
  sql,
  publicFlag
}) {
  const creation = new openbis.QueryCreation()
  creation.setName(name)
  creation.setDescription(description)
  creation.setDatabaseId(new openbis.QueryDatabaseName(databaseName))
  creation.setQueryType(queryType)
  creation.setEntityTypeCodePattern(entityTypeCodePattern)
  creation.setSql(sql)
  creation.setPublic(publicFlag)
  return new openbis.CreateQueriesOperation([creation])
}

function updateQueryOperation({
  name,
  description,
  databaseName,
  queryType,
  entityTypeCodePattern,
  sql,
  publicFlag
}) {
  const update = new openbis.QueryUpdate()
  update.setQueryId(new openbis.QueryName(name))
  update.setDescription(description)
  update.setDatabaseId(new openbis.QueryDatabaseName(databaseName))
  update.setQueryType(queryType)
  update.setEntityTypeCodePattern(entityTypeCodePattern)
  update.setSql(sql)
  update.setPublic(publicFlag)
  return new openbis.UpdateQueriesOperation([update])
}

function expectExecuteOperations(expectedOperations) {
  expect(common.facade.executeOperations).toHaveBeenCalledTimes(1)
  const actualOperations = common.facade.executeOperations.mock.calls[0][0]
  expect(actualOperations.length).toEqual(expectedOperations.length)
  actualOperations.forEach((actualOperation, index) => {
    expect(actualOperation).toMatchObject(expectedOperations[index])
  })
}
