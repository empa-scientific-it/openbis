import TypeFormComponentTest from '@srcTest/js/components/types/form/TypeFormComponentTest.js'

let common = null

beforeEach(() => {
  common = new TypeFormComponentTest()
  common.beforeEach()
})

describe(TypeFormComponentTest.SUITE, () => {
  test('validate type', testValidateType)
  test('validate property', testValidateProperty)
  test('validate type and property', testValidateTypeAndProperty)
})

async function testValidateType() {
  const form = await common.mountNew()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      type: {
        title: 'New Object Type',
        code: {
          error: 'Code cannot be empty',
          focused: true
        },
        description: {
          error: null
        },
        validationPlugin: {
          error: null
        },
        generatedCodePrefix: {
          error: 'Generated code prefix cannot be empty'
        }
      }
    },
    buttons: {
      message: null
    }
  })

  form.getParameters().getType().getCode().change('I am illegal')
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      type: {
        code: {
          value: 'I am illegal',
          error: 'Code can only contain A-Z, a-z, 0-9 and _, -, .',
          focused: true
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

async function testValidateProperty() {
  const form = await common.mountNew()

  form.getParameters().getType().getCode().change('TEST_CODE')
  form.getParameters().getType().getGeneratedCodePrefix().change('TEST_PREFIX_')

  form.getButtons().getAddSection().click()
  form.getButtons().getAddProperty().click()
  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        title: 'Property',
        code: {
          error: 'Code cannot be empty',
          focused: true
        },
        dataType: {
          error: 'Data Type cannot be empty'
        },
        label: {
          error: 'Label cannot be empty'
        },
        description: {
          error: 'Description cannot be empty'
        },
        plugin: {
          error: null
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

  form.getParameters().getProperty().getCode().change('I am illegal')
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        code: {
          value: 'I am illegal',
          error: 'Code can only contain A-Z, a-z, 0-9 and _, -, .',
          focused: true
        }
      }
    }
  })
}

async function testValidateTypeAndProperty() {
  const form = await common.mountNew()

  form.getButtons().getAddSection().click()
  form.getButtons().getAddProperty().click()
  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      type: {
        title: 'New Object Type',
        code: {
          error: 'Code cannot be empty'
        },
        description: {
          error: null
        },
        validationPlugin: {
          error: null
        },
        generatedCodePrefix: {
          error: 'Generated code prefix cannot be empty'
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
