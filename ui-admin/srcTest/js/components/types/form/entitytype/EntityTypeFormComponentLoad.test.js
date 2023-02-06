import EntityTypeFormComponentTest from '@srcTest/js/components/types/form/entitytype/EntityTypeFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new EntityTypeFormComponentTest()
  common.beforeEach()
})

describe(EntityTypeFormComponentTest.SUITE, () => {
  test('load new', testLoadNew)
  test('load existing', testLoadExisting)
})

async function testLoadNew() {
  const form = await common.mountNew()

  form.expectJSON({
    preview: {
      sections: []
    },
    parameters: {
      type: {
        title: 'New Object Type',
        code: {
          label: 'Code',
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
        validationPlugin: {
          label: 'Entity Validation Plugin',
          value: null,
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      addSection: {
        enabled: true
      },
      addProperty: {
        enabled: false
      },
      remove: {
        enabled: false
      },
      save: {
        enabled: true
      },
      edit: null,
      cancel: null,
      message: null
    }
  })
}

async function testLoadExisting() {
  common.facade.loadValidationPlugins.mockReturnValue(
    Promise.resolve([fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin])
  )

  const form = await common.mountExisting(fixture.TEST_SAMPLE_TYPE_DTO)

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1',
          properties: [{ code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }]
        },
        {
          name: 'TEST_SECTION_2',
          properties: [
            { code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
            { code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
          ]
        }
      ]
    },
    parameters: {
      type: {
        title: 'Object Type',
        code: {
          label: 'Code',
          value: fixture.TEST_SAMPLE_TYPE_DTO.getCode(),
          mode: 'view'
        },
        description: {
          label: 'Description',
          value: fixture.TEST_SAMPLE_TYPE_DTO.getDescription(),
          mode: 'view'
        },
        validationPlugin: {
          label: 'Entity Validation Plugin',
          value: fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin.name,
          mode: 'view'
        }
      }
    },
    buttons: {
      edit: {
        enabled: true
      },
      addSection: null,
      addProperty: null,
      remove: null,
      save: null,
      cancel: null,
      message: null
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: 'TEST_SECTION_1',
          properties: [{ code: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }]
        },
        {
          name: 'TEST_SECTION_2',
          properties: [
            { code: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
            { code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
          ]
        }
      ]
    },
    parameters: {
      type: {
        title: 'Object Type',
        code: {
          label: 'Code',
          value: fixture.TEST_SAMPLE_TYPE_DTO.getCode(),
          enabled: false,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: fixture.TEST_SAMPLE_TYPE_DTO.getDescription(),
          enabled: true,
          mode: 'edit'
        },
        validationPlugin: {
          label: 'Entity Validation Plugin',
          value: fixture.TEST_SAMPLE_TYPE_DTO.validationPlugin.name,
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      addSection: {
        enabled: true
      },
      addProperty: {
        enabled: false
      },
      remove: {
        enabled: false
      },
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
