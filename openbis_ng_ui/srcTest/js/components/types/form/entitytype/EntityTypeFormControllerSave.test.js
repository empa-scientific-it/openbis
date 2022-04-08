import EntityTypeFormControllerTest from '@srcTest/js/components/types/form/entitytype/EntityTypeFormControllerTest.js'
import EntityTypeFormSelectionType from '@src/js/components/types/form/entitytype/EntityTypeFormSelectionType.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new EntityTypeFormControllerTest()
  common.beforeEach()
  common.init({
    id: 'TEST_OBJECT_ID',
    type: objectTypes.OBJECT_TYPE
  })
})

afterEach(() => {
  common.afterEach()
})

describe(EntityTypeFormControllerTest.SUITE, () => {
  test('save add property', testSaveAddProperty)
  test('save update property assignment', testSaveUpdatePropertyAssignment)
  test('save update property type', testSaveUpdatePropertyType)
  test('save delete property', testSaveDeleteProperty)
  test(
    'save delete property last assignment',
    testSaveDeletePropertyLastAssignment
  )
})

async function testSaveAddProperty() {
  const SAMPLE_TYPE = new openbis.SampleType()
  SAMPLE_TYPE.setCode('TEST_TYPE')
  SAMPLE_TYPE.setGeneratedCodePrefix('TEST_PREFIX')

  common.facade.loadType.mockReturnValue(Promise.resolve(SAMPLE_TYPE))
  common.facade.loadPropertyTypes.mockReturnValue(Promise.resolve([]))
  common.facade.executeOperations.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  common.controller.handleAddSection()
  common.controller.handleAddProperty()

  common.controller.handleChange(EntityTypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'code',
    value: 'NEW_CODE'
  })
  common.controller.handleChange(EntityTypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'dataType',
    value: 'VARCHAR'
  })
  common.controller.handleChange(EntityTypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'label',
    value: 'NEW_LABEL'
  })
  common.controller.handleChange(EntityTypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'description',
    value: 'NEW_DESCRIPTION'
  })

  await common.controller.handleSave()

  expectExecuteOperations([
    createPropertyTypeOperation({
      code: 'NEW_CODE',
      dataType: openbis.DataType.VARCHAR,
      label: 'NEW_LABEL'
    }),
    setPropertyAssignmentOperation(SAMPLE_TYPE.getCode(), 'NEW_CODE', false)
  ])
}

async function testSaveUpdatePropertyAssignment() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(SAMPLE_TYPE_WITH_TEST_PROPERTY)
  )
  common.facade.loadTypeUsages.mockReturnValue(Promise.resolve(0))
  common.facade.executeOperations.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  common.controller.handleChange(EntityTypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'mandatory',
    value: true
  })

  await common.controller.handleSave()

  expectExecuteOperations([
    setPropertyAssignmentOperation(
      SAMPLE_TYPE_WITH_TEST_PROPERTY.getCode(),
      TEST_PROPERTY_TYPE.getCode(),
      true
    )
  ])
}

async function testSaveUpdatePropertyType() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(SAMPLE_TYPE_WITH_TEST_PROPERTY)
  )
  common.facade.executeOperations.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  common.controller.handleChange(EntityTypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'label',
    value: 'Updated label'
  })

  await common.controller.handleSave()

  expectExecuteOperations([
    updatePropertyTypeOperation(TEST_PROPERTY_TYPE.getCode(), 'Updated label'),
    setPropertyAssignmentOperation(
      SAMPLE_TYPE_WITH_TEST_PROPERTY.getCode(),
      TEST_PROPERTY_TYPE.getCode(),
      TEST_PROPERTY_ASSIGNMENT.isMandatory()
    )
  ])
}

async function testSaveDeleteProperty() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(SAMPLE_TYPE_WITH_TEST_PROPERTY)
  )
  common.facade.loadPropertyUsages.mockReturnValue(Promise.resolve({}))
  common.facade.executeOperations.mockReturnValue(Promise.resolve({}))
  common.facade.loadAssignments.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  common.controller.handleSelectionChange(
    EntityTypeFormSelectionType.PROPERTY,
    {
      id: 'property-0'
    }
  )
  common.controller.handleRemove()
  common.controller.handleRemoveConfirm()

  await common.controller.handleSave()

  expectExecuteOperations([
    deletePropertyAssignmentOperation(
      SAMPLE_TYPE_WITH_TEST_PROPERTY.getCode(),
      TEST_PROPERTY_TYPE.getCode()
    ),
    setPropertyAssignmentOperation(SAMPLE_TYPE_WITH_TEST_PROPERTY.getCode())
  ])
}

async function testSaveDeletePropertyLastAssignment() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(SAMPLE_TYPE_WITH_TEST_PROPERTY)
  )
  common.facade.loadPropertyUsages.mockReturnValue(Promise.resolve({}))
  common.facade.loadAssignments.mockReturnValue(
    Promise.resolve({
      [TEST_PROPERTY_TYPE.getCode()]: 1
    })
  )
  common.facade.executeOperations.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  common.controller.handleSelectionChange(
    EntityTypeFormSelectionType.PROPERTY,
    {
      id: 'property-0'
    }
  )
  common.controller.handleRemove()
  common.controller.handleRemoveConfirm()

  await common.controller.handleSave()

  expectExecuteOperations([
    deletePropertyAssignmentOperation(
      SAMPLE_TYPE_WITH_TEST_PROPERTY.getCode(),
      TEST_PROPERTY_TYPE.getCode()
    ),
    setPropertyAssignmentOperation(SAMPLE_TYPE_WITH_TEST_PROPERTY.getCode()),
    deletePropertyTypeOperation(TEST_PROPERTY_TYPE.getCode())
  ])
}

function createPropertyTypeOperation({
  code: propertyTypeCode,
  dataType: propertyDataType,
  vocabulary: propertyTypeVocabulary,
  label: propertyTypeLabel
}) {
  const creation = new openbis.PropertyTypeCreation()
  creation.setCode(propertyTypeCode)
  creation.setDataType(propertyDataType)
  if (propertyTypeVocabulary) {
    creation.setVocabularyId(
      new openbis.VocabularyPermId(propertyTypeVocabulary)
    )
  }
  creation.setLabel(propertyTypeLabel)
  return new openbis.CreatePropertyTypesOperation([creation])
}

function updatePropertyTypeOperation(propertyTypeCode, propertyTypeLabel) {
  const update = new openbis.PropertyTypeUpdate()
  update.setTypeId(new openbis.PropertyTypePermId(propertyTypeCode))
  update.setLabel(propertyTypeLabel)
  return new openbis.UpdatePropertyTypesOperation([update])
}

function setPropertyAssignmentOperation(
  typeCode,
  propertyCode,
  propertyMandatory
) {
  const assignments = []
  if (propertyCode) {
    let assignment = new openbis.PropertyAssignmentCreation()
    assignment.setPropertyTypeId(new openbis.PropertyTypePermId(propertyCode))
    assignment.setMandatory(propertyMandatory)
    assignments.push(assignment)
  }

  const update = new openbis.SampleTypeUpdate()
  update.setTypeId(
    new openbis.EntityTypePermId(typeCode, openbis.EntityKind.SAMPLE)
  )
  update.getPropertyAssignments().set(assignments)

  return new openbis.UpdateSampleTypesOperation([update])
}

function deletePropertyAssignmentOperation(typeCode, propertyCode) {
  const assignmentId = new openbis.PropertyAssignmentPermId(
    new openbis.EntityTypePermId(typeCode, openbis.EntityKind.SAMPLE),
    new openbis.PropertyTypePermId(propertyCode)
  )

  const update = new openbis.SampleTypeUpdate()
  update.setTypeId(
    new openbis.EntityTypePermId(typeCode, openbis.EntityKind.SAMPLE)
  )
  update.getPropertyAssignments().remove([assignmentId])
  update.getPropertyAssignments().setForceRemovingAssignments(true)

  return new openbis.UpdateSampleTypesOperation([update])
}

function deletePropertyTypeOperation(propertyCode) {
  const options = new openbis.PropertyTypeDeletionOptions()
  options.setReason('deleted via ng_ui')
  return new openbis.DeletePropertyTypesOperation(
    [new openbis.PropertyTypePermId(propertyCode)],
    options
  )
}

function expectExecuteOperations(expectedOperations) {
  expect(common.facade.executeOperations).toHaveBeenCalledTimes(1)
  const actualOperations = common.facade.executeOperations.mock.calls[0][0]
  expect(actualOperations.length).toEqual(expectedOperations.length)
  actualOperations.forEach((actualOperation, index) => {
    expect(actualOperation).toMatchObject(expectedOperations[index])
  })
}

const TEST_PROPERTY_TYPE = new openbis.PropertyType()
TEST_PROPERTY_TYPE.setCode('TEST_PROPERTY_TYPE')
TEST_PROPERTY_TYPE.setLabel('TEST_LABEL')
TEST_PROPERTY_TYPE.setDescription('TEST_DESCRIPTION')
TEST_PROPERTY_TYPE.setDataType(openbis.DataType.INTEGER)

const TEST_PROPERTY_ASSIGNMENT = new openbis.PropertyAssignment()
TEST_PROPERTY_ASSIGNMENT.setPropertyType(TEST_PROPERTY_TYPE)

const SAMPLE_TYPE_WITH_TEST_PROPERTY = new openbis.SampleType()
SAMPLE_TYPE_WITH_TEST_PROPERTY.setCode('TEST_TYPE')
SAMPLE_TYPE_WITH_TEST_PROPERTY.setGeneratedCodePrefix('TEST_PREFIX')
SAMPLE_TYPE_WITH_TEST_PROPERTY.setPropertyAssignments([
  TEST_PROPERTY_ASSIGNMENT
])
