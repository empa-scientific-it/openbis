import QueryFormComponentTest from '@srcTest/js/components/tools/form/query/QueryFormComponentTest.js'
import QueryFormTestData from '@srcTest/js/components/tools/form/query/QueryFormTestData.js'

let common = null

beforeEach(() => {
  common = new QueryFormComponentTest()
  common.beforeEach()
})

describe(QueryFormComponentTest.SUITE, () => {
  test('execute new', testExecuteNew)
  test('execute existing', testExecuteExisting)
})

async function testExecuteNew() {
  const { testDatabase, anotherDatabase, testResult } = QueryFormTestData

  common.facade.loadQueryDatabases.mockReturnValue(
    Promise.resolve([testDatabase, anotherDatabase])
  )
  common.facade.executeSql.mockReturnValue(Promise.resolve(testResult))

  const form = await common.mountNew()

  form.expectJSON({
    executeResults: {
      title: null,
      grid: null
    }
  })

  form.getButtons().getExecute().click()
  await form.update()

  form.expectJSON({
    executeResults: {
      title: 'Results',
      messages: [],
      grid: {
        columns: [{ label: 'id' }, { label: 'code' }, { label: 'Experiment' }],
        rows: [
          {
            values: {
              id: '1',
              code: 'code_1',
              Experiment: 'perm_id_1'
            }
          },
          {
            values: {
              id: '2',
              code: 'code_2',
              Experiment: 'perm_id_2'
            }
          }
        ]
      }
    }
  })
}

async function testExecuteExisting() {
  const {
    testDatabase,
    anotherDatabase,
    queryWithParameters,
    testResult
  } = QueryFormTestData

  common.facade.loadQueryDatabases.mockReturnValue(
    Promise.resolve([testDatabase, anotherDatabase])
  )
  common.facade.executeQuery.mockReturnValue(Promise.resolve(testResult))

  const form = await common.mountExisting(queryWithParameters)

  form.getExecuteParameters().getParameters()[0].change('some code')
  await form.update()

  form.getExecuteParameters().getParameters()[1].change('some permId')
  await form.update()

  form.expectJSON({
    executeParameters: {
      title: 'Parameters',
      parameters: [
        {
          label: 'code',
          value: 'some code',
          mode: 'edit'
        },
        {
          label: 'permId',
          value: 'some permId',
          mode: 'edit'
        }
      ]
    },
    executeResults: {
      title: null,
      messages: [],
      grid: null
    },
    buttons: {
      edit: {
        enabled: true
      },
      execute: {
        enabled: true
      },
      save: null,
      cancel: null,
      message: null
    }
  })

  form.getButtons().getExecute().click()
  await form.update()

  form.expectJSON({
    executeParameters: {
      title: 'Parameters',
      parameters: [
        {
          label: 'code',
          value: 'some code',
          mode: 'edit'
        },
        {
          label: 'permId',
          value: 'some permId',
          mode: 'edit'
        }
      ]
    },
    executeResults: {
      title: 'Results',
      messages: [
        {
          text:
            'Detected authorization column(s) that will be used for automatic results filtering: Experiment(experiment_key).',
          type: 'info'
        }
      ],
      grid: {
        columns: [{ label: 'id' }, { label: 'code' }, { label: 'Experiment' }],
        rows: [
          {
            values: {
              id: '1',
              code: 'code_1',
              Experiment: 'perm_id_1'
            }
          },
          {
            values: {
              id: '2',
              code: 'code_2',
              Experiment: 'perm_id_2'
            }
          }
        ]
      }
    },
    buttons: {
      edit: {
        enabled: true
      },
      execute: {
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
    executeParameters: {
      title: 'Parameters',
      parameters: [
        {
          label: 'code',
          value: null,
          mode: 'edit'
        },
        {
          label: 'permId',
          value: null,
          mode: 'edit'
        }
      ]
    },
    executeResults: {
      title: null,
      messages: [],
      grid: null
    },
    buttons: {
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      execute: {
        enabled: true
      },
      edit: null,
      message: null
    }
  })

  common.facade.executeSql.mockReturnValue(Promise.resolve(testResult))

  form.getButtons().getExecute().click()
  await form.update()

  form.expectJSON({
    executeParameters: {
      title: 'Parameters',
      parameters: [
        {
          label: 'code',
          value: null,
          mode: 'edit'
        },
        {
          label: 'permId',
          value: null,
          mode: 'edit'
        }
      ]
    },
    executeResults: {
      title: 'Results',
      messages: [
        {
          text:
            'Detected authorization column(s) that will be used for automatic results filtering: Experiment(experiment_key).',
          type: 'info'
        }
      ],
      grid: {
        columns: [{ label: 'id' }, { label: 'code' }, { label: 'Experiment' }],
        rows: [
          {
            values: {
              id: '1',
              code: 'code_1',
              Experiment: 'perm_id_1'
            }
          },
          {
            values: {
              id: '2',
              code: 'code_2',
              Experiment: 'perm_id_2'
            }
          }
        ]
      }
    },
    buttons: {
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      execute: {
        enabled: true
      },
      edit: null,
      message: null
    }
  })
}
