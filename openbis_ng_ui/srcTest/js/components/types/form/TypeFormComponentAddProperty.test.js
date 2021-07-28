import TypeFormComponentTest from '@srcTest/js/components/types/form/TypeFormComponentTest.js'
import TypeFormPropertyScope from '@src/js/components/types/form/TypeFormPropertyScope.js'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new TypeFormComponentTest()
  common.beforeEach()
})

describe(TypeFormComponentTest.SUITE, () => {
  test('add local property', testAddLocalProperty)
  test('add new global property', testAddNewGlobalProperty)
  test('add existing global property', testAddExistingGlobalProperty)
})

async function testAddLocalProperty() {
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

  form.getPreview().getSections()[1].getProperties()[0].click()
  form.getButtons().getAddProperty().click()
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
            {
              message: {
                type: 'info',
                text: 'Please select a data type to display the field preview.'
              }
            },
            { code: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
          ]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        scope: {
          label: 'Scope',
          value: TypeFormPropertyScope.LOCAL,
          enabled: true,
          mode: 'edit'
        },
        code: {
          label: 'Code',
          value: null,
          enabled: true,
          mode: 'edit'
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
      edit: null,
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
      cancel: {
        enabled: true
      },
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })
}

async function testAddNewGlobalProperty() {
  const EXISTING_GLOBAL_PROPERTY = new openbis.PropertyType()
  EXISTING_GLOBAL_PROPERTY.setCode('EXISTING_GLOBAL_PROPERTY')

  common.facade.loadGlobalPropertyTypes.mockReturnValue(
    Promise.resolve([EXISTING_GLOBAL_PROPERTY])
  )
  common.facade.loadDynamicPlugins.mockReturnValue(
    Promise.resolve([fixture.TEST_PLUGIN_DTO, fixture.ANOTHER_PLUGIN_DTO])
  )

  const form = await common.mountNew()

  form.getButtons().getAddSection().click()
  await form.update()

  form.getButtons().getAddProperty().click()
  await form.update()

  form
    .getParameters()
    .getProperty()
    .getScope()
    .change(TypeFormPropertyScope.GLOBAL)
  await form.update()

  form.getParameters().getProperty().getCode().change('NEW_GLOBAL_PROPERTY')
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
        scope: {
          label: 'Scope',
          value: TypeFormPropertyScope.GLOBAL,
          enabled: true,
          mode: 'edit'
        },
        code: {
          label: 'Code',
          value: 'NEW_GLOBAL_PROPERTY',
          enabled: true,
          mode: 'edit',
          options: [EXISTING_GLOBAL_PROPERTY.getCode()]
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
          properties: [{ code: 'NEW_GLOBAL_PROPERTY' }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        scope: {
          label: 'Scope',
          value: TypeFormPropertyScope.GLOBAL,
          enabled: true,
          mode: 'edit'
        },
        code: {
          label: 'Code',
          value: 'NEW_GLOBAL_PROPERTY',
          enabled: true,
          mode: 'edit',
          options: [EXISTING_GLOBAL_PROPERTY.getCode()]
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

async function testAddExistingGlobalProperty() {
  const EXISTING_GLOBAL_PROPERTY = new openbis.PropertyType()
  EXISTING_GLOBAL_PROPERTY.setCode('EXISTING_GLOBAL_PROPERTY')
  EXISTING_GLOBAL_PROPERTY.setDataType('CONTROLLEDVOCABULARY')
  EXISTING_GLOBAL_PROPERTY.setVocabulary(fixture.TEST_VOCABULARY_DTO)
  EXISTING_GLOBAL_PROPERTY.setLabel('Existing Label')
  EXISTING_GLOBAL_PROPERTY.setDescription('Existing Description')

  common.facade.loadGlobalPropertyTypes.mockReturnValue(
    Promise.resolve([EXISTING_GLOBAL_PROPERTY])
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
    .getScope()
    .change(TypeFormPropertyScope.GLOBAL)
  await form.update()

  form
    .getParameters()
    .getProperty()
    .getCode()
    .change(EXISTING_GLOBAL_PROPERTY.getCode())
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: null,
          properties: [{ code: EXISTING_GLOBAL_PROPERTY.getCode() }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        scope: {
          label: 'Scope',
          value: TypeFormPropertyScope.GLOBAL,
          enabled: true,
          mode: 'edit'
        },
        code: {
          label: 'Code',
          value: EXISTING_GLOBAL_PROPERTY.getCode(),
          enabled: true,
          mode: 'edit',
          options: [EXISTING_GLOBAL_PROPERTY.getCode()]
        },
        dataType: {
          label: 'Data Type',
          value: EXISTING_GLOBAL_PROPERTY.getDataType(),
          enabled: true,
          mode: 'edit'
        },
        vocabulary: {
          label: 'Vocabulary Type',
          value: EXISTING_GLOBAL_PROPERTY.vocabulary.getCode(),
          enabled: false,
          mode: 'edit'
        },
        label: {
          label: 'Label',
          value: EXISTING_GLOBAL_PROPERTY.getLabel(),
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: EXISTING_GLOBAL_PROPERTY.getDescription(),
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
          properties: [{ code: EXISTING_GLOBAL_PROPERTY.getCode() }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        scope: {
          label: 'Scope',
          value: TypeFormPropertyScope.GLOBAL,
          enabled: true,
          mode: 'edit'
        },
        code: {
          label: 'Code',
          value: EXISTING_GLOBAL_PROPERTY.getCode(),
          enabled: true,
          mode: 'edit',
          options: [EXISTING_GLOBAL_PROPERTY.getCode()]
        },
        dataType: {
          label: 'Data Type',
          value: EXISTING_GLOBAL_PROPERTY.getDataType(),
          enabled: true,
          mode: 'edit'
        },
        vocabulary: {
          label: 'Vocabulary Type',
          value: EXISTING_GLOBAL_PROPERTY.vocabulary.getCode(),
          enabled: false,
          mode: 'edit'
        },
        label: {
          label: 'Label',
          value: EXISTING_GLOBAL_PROPERTY.getLabel(),
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: EXISTING_GLOBAL_PROPERTY.getDescription(),
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
