import QueryFormComponentTest from '@srcTest/js/components/tools/form/query/QueryFormComponentTest.js'
import QueryFormTestData from '@srcTest/js/components/tools/form/query/QueryFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new QueryFormComponentTest()
  common.beforeEach()
})

describe(QueryFormComponentTest.SUITE, () => {
  test('load new', testLoadNew)
  test('load existing SAMPLE query', async () => {
    const { sampleQuery } = QueryFormTestData
    await testLoadExisting(sampleQuery)
  })
  test('load existing GENERIC query', async () => {
    const { genericQuery } = QueryFormTestData
    await testLoadExisting(genericQuery)
  })
  test('load existing query with parameters', testLoadExistingWithParameters)
})

async function testLoadNew() {
  const { testDatabase, anotherDatabase } = QueryFormTestData

  common.facade.loadQueryDatabases.mockReturnValue(
    Promise.resolve([testDatabase, anotherDatabase])
  )

  const form = await common.mountNew()

  form.expectJSON({
    sql: {
      title: 'SQL',
      sql: {
        label: 'SQL',
        value: null,
        enabled: true,
        mode: 'edit'
      }
    },
    parameters: {
      title: 'New Query',
      name: {
        label: 'Name',
        value: null,
        enabled: true,
        mode: 'edit'
      },
      description: {
        label: 'Description',
        value: null,
        enabled: true,
        mode: 'edit'
      },
      database: {
        label: 'Database',
        value: null,
        enabled: true,
        mode: 'edit',
        options: [
          {
            label: `${testDatabase.getLabel()} (Space: ${
              testDatabase.space.code
            })`,
            value: testDatabase.getName()
          },
          {
            label: `${anotherDatabase.getLabel()} (Space: none)`,
            value: anotherDatabase.getName()
          }
        ]
      },
      queryType: {
        label: 'Query Type',
        value: null,
        options: [
          { value: 'GENERIC', label: 'GENERIC' },
          { value: 'EXPERIMENT', label: 'COLLECTION' },
          { value: 'DATA_SET', label: 'DATA_SET' },
          { value: 'MATERIAL', label: 'MATERIAL' },
          { value: 'SAMPLE', label: 'OBJECT' }
        ],
        enabled: true,
        mode: 'edit'
      },
      entityTypeCodePattern: null,
      publicFlag: {
        label: 'Public',
        value: false,
        enabled: true,
        mode: 'edit'
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
      execute: {
        enabled: true
      },
      edit: null,
      cancel: null,
      message: null
    }
  })
}

async function testLoadExisting(query) {
  const { testDatabase, anotherDatabase } = QueryFormTestData

  common.facade.loadQueryDatabases.mockReturnValue(
    Promise.resolve([testDatabase, anotherDatabase])
  )

  const form = await common.mountExisting(query)

  form.expectJSON({
    sql: {
      title: 'SQL',
      sql: {
        label: 'SQL',
        value: query.sql,
        mode: 'view'
      }
    },
    parameters: {
      title: 'Query',
      name: {
        label: 'Name',
        value: query.getName(),
        mode: 'view'
      },
      description: {
        label: 'Description',
        value: query.getDescription(),
        mode: 'view'
      },
      database: {
        label: 'Database',
        value: query.getDatabaseId().getName(),
        mode: 'view'
      },
      queryType: {
        label: 'Query Type',
        value: query.getQueryType(),
        mode: 'view'
      },
      entityTypeCodePattern:
        query.getQueryType() === openbis.QueryType.GENERIC
          ? null
          : {
              label: 'Entity Type Pattern',
              value: query.getEntityTypeCodePattern(),
              mode: 'view'
            },
      publicFlag: {
        label: 'Public',
        value: query.isPublic(),
        mode: 'view'
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
    sql: {
      title: 'SQL',
      sql: {
        label: 'SQL',
        value: query.sql,
        enabled: true,
        mode: 'edit'
      }
    },
    parameters: {
      title: 'Query',
      name: {
        label: 'Name',
        value: query.getName(),
        enabled: false,
        mode: 'edit'
      },
      description: {
        label: 'Description',
        value: query.getDescription(),
        enabled: true,
        mode: 'edit'
      },
      database: {
        label: 'Database',
        value: query.getDatabaseId().getName(),
        enabled: true,
        mode: 'edit'
      },
      queryType: {
        label: 'Query Type',
        value: query.getQueryType(),
        options: [
          { value: 'GENERIC', label: 'GENERIC' },
          { value: 'EXPERIMENT', label: 'COLLECTION' },
          { value: 'DATA_SET', label: 'DATA_SET' },
          { value: 'MATERIAL', label: 'MATERIAL' },
          { value: 'SAMPLE', label: 'OBJECT' }
        ],
        enabled: true,
        mode: 'edit'
      },
      entityTypeCodePattern:
        query.getQueryType() === openbis.QueryType.GENERIC
          ? null
          : {
              label: 'Entity Type Pattern',
              value: query.getEntityTypeCodePattern(),
              enabled: true,
              mode: 'edit'
            },
      publicFlag: {
        label: 'Public',
        value: query.isPublic(),
        enabled: true,
        mode: 'edit'
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

async function testLoadExistingWithParameters() {
  const {
    testDatabase,
    anotherDatabase,
    queryWithParameters
  } = QueryFormTestData

  common.facade.loadQueryDatabases.mockReturnValue(
    Promise.resolve([testDatabase, anotherDatabase])
  )

  const form = await common.mountExisting(queryWithParameters)

  form.expectJSON({
    sql: {
      title: 'SQL',
      sql: {
        label: 'SQL',
        value: queryWithParameters.sql,
        mode: 'view'
      }
    },
    parameters: {
      messages: [
        {
          text:
            'Security warning: this query is public (i.e. visible to other users) and is defined for a database that is not assigned to any space. Please make sure the query returns only data that can be seen by every user or the results contain one of the special columns (i.e. experiment_key/sample_key/data_set_key) that will be used for an automatic query results filtering.',
          type: 'warning'
        }
      ]
    },
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

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    sql: {
      title: 'SQL',
      sql: {
        label: 'SQL',
        value: queryWithParameters.sql,
        mode: 'edit'
      }
    },
    parameters: {
      messages: [
        {
          text:
            'Security warning: this query is public (i.e. visible to other users) and is defined for a database that is not assigned to any space. Please make sure the query returns only data that can be seen by every user or the results contain one of the special columns (i.e. experiment_key/sample_key/data_set_key) that will be used for an automatic query results filtering.',
          type: 'warning'
        }
      ]
    },
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
}
