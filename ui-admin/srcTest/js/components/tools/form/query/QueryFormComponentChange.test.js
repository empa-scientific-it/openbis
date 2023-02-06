import QueryFormComponentTest from '@srcTest/js/components/tools/form/query/QueryFormComponentTest.js'
import QueryFormTestData from '@srcTest/js/components/tools/form/query/QueryFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new QueryFormComponentTest()
  common.beforeEach()
})

describe(QueryFormComponentTest.SUITE, () => {
  test('change', testChange)
})

async function testChange() {
  const { sampleQuery, testDatabase, anotherDatabase } = QueryFormTestData

  common.facade.loadQueryDatabases.mockReturnValue(
    Promise.resolve([testDatabase, anotherDatabase])
  )

  const form = await common.mountExisting(sampleQuery)

  form.getButtons().getEdit().click()
  await form.update()

  form
    .getSql()
    .getSql()
    .change('select * from sometable where param = ${param}')
  await form.update()

  form.getParameters().getDescription().change('updated description')
  await form.update()

  form.getParameters().getDatabase().change(testDatabase.getName())
  await form.update()

  form.getParameters().getQueryType().change(openbis.QueryType.GENERIC)
  await form.update()

  form.getParameters().getPublicFlag().change(!sampleQuery.isPublic())
  await form.update()

  form.getExecuteParameters().getParameters()[0].change('some execution value')
  await form.update()

  form.expectJSON({
    sql: {
      title: 'SQL',
      sql: {
        label: 'SQL',
        value: 'select * from sometable where param = ${param}',
        enabled: true,
        mode: 'edit'
      }
    },
    parameters: {
      title: 'Query',
      name: {
        label: 'Name',
        value: sampleQuery.getName(),
        enabled: false,
        mode: 'edit'
      },
      description: {
        label: 'Description',
        value: 'updated description',
        enabled: true,
        mode: 'edit'
      },
      database: {
        label: 'Database',
        value: testDatabase.getName(),
        enabled: true,
        mode: 'edit'
      },
      queryType: {
        label: 'Query Type',
        value: openbis.QueryType.GENERIC,
        enabled: true,
        mode: 'edit'
      },
      entityTypeCodePattern: null,
      publicFlag: {
        label: 'Public',
        value: !sampleQuery.isPublic(),
        enabled: true,
        mode: 'edit'
      }
    },
    executeParameters: {
      title: 'Parameters',
      parameters: [
        {
          label: 'param',
          value: 'some execution value',
          mode: 'edit'
        }
      ]
    },
    executeResults: {
      title: null,
      grid: null
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
