import QueryFormComponentTest from '@srcTest/js/components/tools/form/query/QueryFormComponentTest.js'

let common = null

beforeEach(() => {
  common = new QueryFormComponentTest()
  common.beforeEach()
})

describe(QueryFormComponentTest.SUITE, () => {
  test('validate', async () => {
    await testValidate()
  })
})

async function testValidate() {
  const form = await common.mountNew()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    sql: {
      title: 'SQL',
      sql: {
        label: 'SQL',
        value: null,
        error: 'SQL cannot be empty'
      }
    },
    parameters: {
      title: 'New Query',
      name: {
        label: 'Name',
        value: null,
        error: 'Name cannot be empty'
      },
      description: {
        label: 'Description',
        value: null,
        error: null
      },
      database: {
        label: 'Database',
        value: null,
        error: 'Database cannot be empty'
      },
      queryType: {
        label: 'Query Type',
        value: null,
        error: 'Query Type cannot be empty'
      },
      entityTypeCodePattern: null,
      publicFlag: {
        label: 'Public',
        value: false,
        error: null
      }
    },
    executeParameters: {
      title: null,
      parameters: []
    },
    executeResults: {
      title: null,
      grid: null
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
