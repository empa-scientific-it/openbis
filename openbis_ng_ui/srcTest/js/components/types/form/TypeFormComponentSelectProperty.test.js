import TypeFormComponentTest from '@srcTest/js/components/types/form/TypeFormComponentTest.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new TypeFormComponentTest()
  common.beforeEach()
})

describe(TypeFormComponentTest.SUITE, () => {
  test('select property unused', testSelectPropertyUnused)
  test('select property used', testSelectPropertyUsed)
})

async function testSelectPropertyUnused() {
  await doTestSelectProperty(false)
}

async function testSelectPropertyUsed() {
  await doTestSelectProperty(true)
}

async function doTestSelectProperty(used) {
  const plugin = new openbis.Plugin()
  plugin.setName('TEST_PLUGIN')

  const propertyType = new openbis.PropertyType()
  propertyType.setCode('TEST_PROPERTY')
  propertyType.setLabel('Test Label')
  propertyType.setDescription('Test Description')
  propertyType.setDataType(openbis.DataType.VARCHAR)

  const propertyAssignment = new openbis.PropertyAssignment()
  propertyAssignment.setPropertyType(propertyType)
  propertyAssignment.setPlugin(plugin)

  const type = new openbis.SampleType()
  type.setCode('TEST_TYPE')
  type.setPropertyAssignments([propertyAssignment])

  common.facade.loadType.mockReturnValue(Promise.resolve(type))
  common.facade.loadDynamicPlugins.mockReturnValue(Promise.resolve([plugin]))

  const messages = []

  if (used) {
    common.facade.loadAssignments.mockReturnValue(
      Promise.resolve({
        [propertyType.getCode()]: 2
      })
    )
    messages.push({
      text: 'This property is already assigned to 2 type(s).',
      type: 'info'
    })
  }

  const form = await common.mount({
    id: type.getCode(),
    type: objectTypes.OBJECT_TYPE
  })

  form.getPreview().getSections()[0].getProperties()[0].click()
  await form.update()

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        title: 'Property',
        messages,
        code: {
          label: 'Code',
          value: propertyType.getCode(),
          enabled: false,
          mode: 'edit'
        },
        dataType: {
          label: 'Data Type',
          value: propertyType.getDataType(),
          enabled: true,
          mode: 'edit',
          options: [
            {
              label: openbis.DataType.VARCHAR,
              value: openbis.DataType.VARCHAR
            },
            {
              label: openbis.DataType.MULTILINE_VARCHAR + ' (Converted)',
              value: openbis.DataType.MULTILINE_VARCHAR
            }
          ]
        },
        label: {
          label: 'Label',
          value: propertyType.getLabel(),
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: propertyType.getDescription(),
          enabled: true,
          mode: 'edit'
        },
        plugin: {
          label: 'Dynamic Property Plugin',
          value: plugin.getName(),
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: null
    }
  })
}
