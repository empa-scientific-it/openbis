import EntityTypeFormComponentTest from '@srcTest/js/components/types/form/entitytype/EntityTypeFormComponentTest.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new EntityTypeFormComponentTest()
  common.beforeEach()
})

describe(EntityTypeFormComponentTest.SUITE, () => {
  test('change property', testChangeProperty)
  test('convert property', testConvertProperty)
})

async function testChangeProperty() {
  const form = await common.mountNew()

  form.getButtons().getAddSection().click()
  await form.update()

  form.getButtons().getAddProperty().click()
  await form.update()

  form.getParameters().getProperty().getCode().change('TEST_CODE')
  await form.update()

  form.getParameters().getProperty().getDataType().change('VARCHAR')
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: null,
          properties: [{ code: 'TEST_CODE', dataType: 'VARCHAR' }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: 'TEST_CODE',
          enabled: true,
          mode: 'edit'
        },
        dataType: {
          label: 'Data Type',
          value: 'VARCHAR',
          enabled: true,
          mode: 'edit'
        },
        vocabulary: null,
        materialType: null,
        schema: null,
        transformation: null
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })

  form
    .getParameters()
    .getProperty()
    .getDataType()
    .change('CONTROLLEDVOCABULARY')
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: null,
          properties: [{ code: 'TEST_CODE', dataType: 'CONTROLLEDVOCABULARY' }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: 'TEST_CODE',
          enabled: true,
          mode: 'edit'
        },
        dataType: {
          label: 'Data Type',
          value: 'CONTROLLEDVOCABULARY',
          enabled: true,
          mode: 'edit'
        },
        vocabulary: {
          label: 'Vocabulary Type',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        materialType: null,
        schema: null,
        transformation: null
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })

  form.getParameters().getProperty().getDataType().change('MATERIAL')
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: null,
          properties: [{ code: 'TEST_CODE', dataType: 'MATERIAL' }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: 'TEST_CODE',
          enabled: true,
          mode: 'edit'
        },
        dataType: {
          label: 'Data Type',
          value: 'MATERIAL',
          enabled: true,
          mode: 'edit'
        },
        vocabulary: null,
        materialType: {
          label: 'Material Type',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        schema: null,
        transformation: null
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })

  form.getParameters().getProperty().getDataType().change('XML')
  await form.update()

  form.expectJSON({
    preview: {
      sections: [
        {
          name: null,
          properties: [{ code: 'TEST_CODE', dataType: 'XML' }]
        }
      ]
    },
    parameters: {
      property: {
        title: 'Property',
        code: {
          label: 'Code',
          value: 'TEST_CODE',
          enabled: true,
          mode: 'edit'
        },
        dataType: {
          label: 'Data Type',
          value: 'XML',
          enabled: true,
          mode: 'edit'
        },
        vocabulary: null,
        materialType: null,
        schema: {
          label: 'XML Schema',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        transformation: {
          label: 'XSLT Script',
          value: null,
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })
}

async function testConvertProperty() {
  const properties = [
    openbis.DataType.INTEGER,
    openbis.DataType.REAL,
    openbis.DataType.VARCHAR,
    openbis.DataType.MULTILINE_VARCHAR,
    openbis.DataType.XML,
    openbis.DataType.HYPERLINK,
    openbis.DataType.TIMESTAMP,
    openbis.DataType.DATE,
    openbis.DataType.BOOLEAN,
    openbis.DataType.CONTROLLEDVOCABULARY,
    openbis.DataType.MATERIAL,
    openbis.DataType.SAMPLE
  ].map(dataType => {
    const propertyType = new openbis.PropertyType()
    propertyType.setCode(dataType)
    propertyType.setDataType(dataType)
    const property = new openbis.PropertyAssignment()
    property.setPropertyType(propertyType)
    return property
  })

  const type = new openbis.SampleType()
  type.setCode('TEST_TYPE')
  type.setPropertyAssignments(properties)

  common.facade.loadType.mockReturnValue(Promise.resolve(type))

  const suffix = ' (Converted)'
  let index = 0

  const form = await common.mount({
    id: type.getCode(),
    type: objectTypes.OBJECT_TYPE
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'INTEGER',
          enabled: true,
          options: [
            { label: openbis.DataType.INTEGER },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix },
            { label: openbis.DataType.REAL + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'REAL',
          enabled: true,
          options: [
            { label: openbis.DataType.REAL },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'VARCHAR',
          enabled: true,
          options: [
            { label: openbis.DataType.VARCHAR },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'MULTILINE_VARCHAR',
          enabled: true,
          options: [
            { label: openbis.DataType.MULTILINE_VARCHAR },
            { label: openbis.DataType.VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'XML',
          enabled: true,
          options: [
            { label: openbis.DataType.XML },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'HYPERLINK',
          enabled: true,
          options: [
            { label: openbis.DataType.HYPERLINK },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'TIMESTAMP',
          enabled: true,
          options: [
            { label: openbis.DataType.TIMESTAMP },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix },
            { label: openbis.DataType.DATE + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'DATE',
          enabled: true,
          options: [
            { label: openbis.DataType.DATE },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'BOOLEAN',
          enabled: true,
          options: [
            { label: openbis.DataType.BOOLEAN },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'CONTROLLEDVOCABULARY',
          enabled: true,
          options: [
            { label: openbis.DataType.CONTROLLEDVOCABULARY },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'MATERIAL',
          enabled: true,
          options: [
            { label: openbis.DataType.MATERIAL },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[index++].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        dataType: {
          value: 'SAMPLE',
          enabled: true,
          options: [
            { label: 'OBJECT' },
            { label: openbis.DataType.VARCHAR + suffix },
            { label: openbis.DataType.MULTILINE_VARCHAR + suffix }
          ]
        }
      }
    }
  })
}
