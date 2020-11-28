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
})

async function testLoadNew() {
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
      title: 'Query',
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
        mode: 'edit'
      },
      queryType: {
        label: 'Query Type',
        value: null,
        options: [
          { value: 'GENERIC' },
          { value: 'EXPERIMENT' },
          { value: 'SAMPLE' },
          { value: 'DATA_SET' },
          { value: 'MATERIAL' }
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

async function testLoadExisting(query) {
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
    buttons: {
      edit: {
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
          { value: 'GENERIC' },
          { value: 'EXPERIMENT' },
          { value: 'SAMPLE' },
          { value: 'DATA_SET' },
          { value: 'MATERIAL' }
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
    buttons: {
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      edit: null,
      message: null
    }
  })
}
