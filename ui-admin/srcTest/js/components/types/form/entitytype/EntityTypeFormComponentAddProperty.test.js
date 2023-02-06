import EntityTypeFormComponentTest from '@srcTest/js/components/types/form/entitytype/EntityTypeFormComponentTest.js'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new EntityTypeFormComponentTest()
  common.beforeEach()
})

describe(EntityTypeFormComponentTest.SUITE, () => {
  test('add new property', testAddNewProperty)
  test('add existing property', testAddExistingProperty)
})

async function testAddNewProperty() {
  const EXISTING_PROPERTY = new openbis.PropertyType()
  EXISTING_PROPERTY.setCode('EXISTING_PROPERTY')

  common.facade.loadPropertyTypes.mockReturnValue(
    Promise.resolve([EXISTING_PROPERTY])
  )
  common.facade.loadDynamicPlugins.mockReturnValue(
    Promise.resolve([fixture.TEST_PLUGIN_DTO, fixture.ANOTHER_PLUGIN_DTO])
  )

  const form = await common.mountNew()

  form.getButtons().getAddSection().click()
  await form.update()

  form.getButtons().getAddProperty().click()
  await form.update()

  form.getParameters().getProperty().getCode().change('NEW_PROPERTY')
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: null,
          properties: [
            {
              message: {
                type: 'info',
                text: 'Please select a data type to display the field preview.'
              }
            }
          ]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: 'NEW_PROPERTY',
          enabled: true,
          mode: 'edit',
          options: [EXISTING_PROPERTY.getCode()]
        },
        dataType: {
          label: 'Data Type',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        label: {
          label: 'Label',
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
        plugin: {
          label: 'Dynamic Property Plugin',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        editable: {
          label: 'Editable',
          value: true,
          enabled: true,
          mode: 'edit'
        },
        mandatory: {
          label: 'Mandatory',
          value: false,
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
        enabled: true
      },
      remove: {
        enabled: true
      },
      save: {
        enabled: true
      },
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      },
      edit: null,
      cancel: null
    }
  })

  form.getParameters().getProperty().getDataType().change('VARCHAR')
  form.getParameters().getProperty().getLabel().change('New Label')
  form.getParameters().getProperty().getDescription().change('New Description')
  form
    .getParameters()
    .getProperty()
    .getPlugin()
    .change(fixture.ANOTHER_PLUGIN_DTO.getName())
  form.getParameters().getProperty().getEditable().change(false)
  form.getParameters().getProperty().getMandatory().change(true)
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: null,
          properties: [{ code: 'NEW_PROPERTY' }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: 'NEW_PROPERTY',
          enabled: true,
          mode: 'edit',
          options: [EXISTING_PROPERTY.getCode()]
        },
        dataType: {
          label: 'Data Type',
          value: 'VARCHAR',
          enabled: true,
          mode: 'edit'
        },
        label: {
          label: 'Label',
          value: 'New Label',
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: 'New Description',
          enabled: true,
          mode: 'edit'
        },
        plugin: {
          label: 'Dynamic Property Plugin',
          value: fixture.ANOTHER_PLUGIN_DTO.getName(),
          enabled: true,
          mode: 'edit'
        },
        editable: {
          label: 'Editable',
          value: false,
          enabled: true,
          mode: 'edit'
        },
        mandatory: {
          label: 'Mandatory',
          value: true,
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
        enabled: true
      },
      remove: {
        enabled: true
      },
      save: {
        enabled: true
      },
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      },
      edit: null,
      cancel: null
    }
  })
}

async function testAddExistingProperty() {
  const EXISTING_PROPERTY = new openbis.PropertyType()
  EXISTING_PROPERTY.setCode('EXISTING_PROPERTY')
  EXISTING_PROPERTY.setDataType('CONTROLLEDVOCABULARY')
  EXISTING_PROPERTY.setVocabulary(fixture.TEST_VOCABULARY_DTO)
  EXISTING_PROPERTY.setLabel('Existing Label')
  EXISTING_PROPERTY.setDescription('Existing Description')

  common.facade.loadPropertyTypes.mockReturnValue(
    Promise.resolve([EXISTING_PROPERTY])
  )
  common.facade.loadDynamicPlugins.mockReturnValue(
    Promise.resolve([fixture.TEST_PLUGIN_DTO, fixture.ANOTHER_PLUGIN_DTO])
  )
  common.facade.loadVocabularies.mockReturnValue(
    Promise.resolve([fixture.TEST_VOCABULARY_DTO])
  )

  const form = await common.mountNew()

  form.getButtons().getAddSection().click()
  await form.update()

  form.getButtons().getAddProperty().click()
  await form.update()

  form
    .getParameters()
    .getProperty()
    .getCode()
    .change(EXISTING_PROPERTY.getCode())
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: null,
          properties: [{ code: EXISTING_PROPERTY.getCode() }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: EXISTING_PROPERTY.getCode(),
          enabled: true,
          mode: 'edit',
          options: [EXISTING_PROPERTY.getCode()]
        },
        dataType: {
          label: 'Data Type',
          value: EXISTING_PROPERTY.getDataType(),
          enabled: true,
          mode: 'edit'
        },
        vocabulary: {
          label: 'Vocabulary Type',
          value: EXISTING_PROPERTY.vocabulary.getCode(),
          enabled: false,
          mode: 'edit'
        },
        label: {
          label: 'Label',
          value: EXISTING_PROPERTY.getLabel(),
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: EXISTING_PROPERTY.getDescription(),
          enabled: true,
          mode: 'edit'
        },
        plugin: {
          label: 'Dynamic Property Plugin',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        editable: {
          label: 'Editable',
          value: true,
          enabled: true,
          mode: 'edit'
        },
        mandatory: {
          label: 'Mandatory',
          value: false,
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
        enabled: true
      },
      remove: {
        enabled: true
      },
      save: {
        enabled: true
      },
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      },
      edit: null,
      cancel: null
    }
  })

  form
    .getParameters()
    .getProperty()
    .getPlugin()
    .change(fixture.ANOTHER_PLUGIN_DTO.getName())
  form.getParameters().getProperty().getEditable().change(false)
  form.getParameters().getProperty().getMandatory().change(true)
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: null,
          properties: [{ code: EXISTING_PROPERTY.getCode() }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: EXISTING_PROPERTY.getCode(),
          enabled: true,
          mode: 'edit',
          options: [EXISTING_PROPERTY.getCode()]
        },
        dataType: {
          label: 'Data Type',
          value: EXISTING_PROPERTY.getDataType(),
          enabled: true,
          mode: 'edit'
        },
        vocabulary: {
          label: 'Vocabulary Type',
          value: EXISTING_PROPERTY.vocabulary.getCode(),
          enabled: false,
          mode: 'edit'
        },
        label: {
          label: 'Label',
          value: EXISTING_PROPERTY.getLabel(),
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: EXISTING_PROPERTY.getDescription(),
          enabled: true,
          mode: 'edit'
        },
        plugin: {
          label: 'Dynamic Property Plugin',
          value: fixture.ANOTHER_PLUGIN_DTO.getName(),
          enabled: true,
          mode: 'edit'
        },
        editable: {
          label: 'Editable',
          value: false,
          enabled: true,
          mode: 'edit'
        },
        mandatory: {
          label: 'Mandatory',
          value: true,
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
        enabled: true
      },
      remove: {
        enabled: true
      },
      save: {
        enabled: true
      },
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      },
      edit: null,
      cancel: null
    }
  })
}
