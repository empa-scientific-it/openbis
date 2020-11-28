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
  const { sampleQuery } = QueryFormTestData

  const form = await common.mountExisting(sampleQuery)

  form.getButtons().getEdit().click()
  await form.update()

  form.getSql().getSql().change('select * from sometable;')
  await form.update()

  form.getParameters().getDescription().change('updated description')
  await form.update()

  form.getParameters().getDatabase().change('updated database')
  await form.update()

  form.getParameters().getQueryType().change(openbis.QueryType.GENERIC)
  await form.update()

  form.getParameters().getPublicFlag().change(!sampleQuery.isPublic())
  await form.update()

  form.expectJSON({
    sql: {
      title: 'SQL',
      sql: {
        label: 'SQL',
        value: 'select * from sometable;',
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
        value: 'updated database',
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
    buttons: {
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      edit: null,
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}
