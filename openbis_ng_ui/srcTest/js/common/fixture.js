import openbis from '@srcTest/js/services/openbis.js'

const TEST_USER = 'test-user'
const TEST_PASSWORD = 'test-password'
const TEST_SESSION_TOKEN = 'test-session-token'

const TEST_USER_DTO = new openbis.Person()
TEST_USER_DTO.setUserId('test-user')
TEST_USER_DTO.setFirstName('test-first-name')
TEST_USER_DTO.setLastName('test-last-name')

const ANOTHER_USER_DTO = new openbis.Person()
ANOTHER_USER_DTO.setUserId('another-user')
ANOTHER_USER_DTO.setFirstName('another-first-name')
ANOTHER_USER_DTO.setLastName('another-last-name')

const ALL_USERS_GROUP_DTO = new openbis.AuthorizationGroup()
ALL_USERS_GROUP_DTO.setCode('all-users-group')
ALL_USERS_GROUP_DTO.setUsers([TEST_USER_DTO, ANOTHER_USER_DTO])

const TEST_GROUP_DTO = new openbis.AuthorizationGroup()
TEST_GROUP_DTO.setCode('test-group')
TEST_GROUP_DTO.setUsers([TEST_USER_DTO])

const ANOTHER_GROUP_DTO = new openbis.AuthorizationGroup()
ANOTHER_GROUP_DTO.setCode('another-group')
ANOTHER_GROUP_DTO.setUsers([ANOTHER_USER_DTO])

const TEST_PLUGIN_DTO = new openbis.Plugin()
TEST_PLUGIN_DTO.setName('TEST_PLUGIN')

const TEST_PROPERTY_TYPE_1_DTO = new openbis.PropertyType()
TEST_PROPERTY_TYPE_1_DTO.setCode('TEST_PROPERTY_TYPE_1')
TEST_PROPERTY_TYPE_1_DTO.setPermId(
  new openbis.PropertyTypePermId(TEST_PROPERTY_TYPE_1_DTO.getCode())
)
TEST_PROPERTY_TYPE_1_DTO.setDataType(openbis.DataType.VARCHAR)

const TEST_PROPERTY_TYPE_2_DTO = new openbis.PropertyType()
TEST_PROPERTY_TYPE_2_DTO.setCode('TEST_PROPERTY_TYPE_2')
TEST_PROPERTY_TYPE_2_DTO.setPermId(
  new openbis.PropertyTypePermId(TEST_PROPERTY_TYPE_2_DTO.getCode())
)
TEST_PROPERTY_TYPE_2_DTO.setDataType(openbis.DataType.VARCHAR)

const TEST_PROPERTY_TYPE_3_DTO = new openbis.PropertyType()
TEST_PROPERTY_TYPE_3_DTO.setCode('TEST_PROPERTY_TYPE_3')
TEST_PROPERTY_TYPE_3_DTO.setPermId(
  new openbis.PropertyTypePermId(TEST_PROPERTY_TYPE_3_DTO.getCode())
)
TEST_PROPERTY_TYPE_3_DTO.setDataType(openbis.DataType.VARCHAR)

const TEST_PROPERTY_ASSIGNMENT_1 = new openbis.PropertyAssignment()
TEST_PROPERTY_ASSIGNMENT_1.setPropertyType(TEST_PROPERTY_TYPE_1_DTO)
TEST_PROPERTY_ASSIGNMENT_1.setSection('TEST_SECTION_1')

const TEST_PROPERTY_ASSIGNMENT_2 = new openbis.PropertyAssignment()
TEST_PROPERTY_ASSIGNMENT_2.setPropertyType(TEST_PROPERTY_TYPE_2_DTO)
TEST_PROPERTY_ASSIGNMENT_2.setSection('TEST_SECTION_2')

const TEST_PROPERTY_ASSIGNMENT_3 = new openbis.PropertyAssignment()
TEST_PROPERTY_ASSIGNMENT_3.setPropertyType(TEST_PROPERTY_TYPE_3_DTO)
TEST_PROPERTY_ASSIGNMENT_3.setSection('TEST_SECTION_2')

const TEST_SAMPLE_TYPE_DTO = new openbis.SampleType()
TEST_SAMPLE_TYPE_DTO.setCode('TEST_TYPE')
TEST_SAMPLE_TYPE_DTO.setDescription('TEST_DESCRIPTION')
TEST_SAMPLE_TYPE_DTO.setValidationPlugin(TEST_PLUGIN_DTO)
TEST_SAMPLE_TYPE_DTO.setPropertyAssignments([
  TEST_PROPERTY_ASSIGNMENT_1,
  TEST_PROPERTY_ASSIGNMENT_2,
  TEST_PROPERTY_ASSIGNMENT_3
])

const ANOTHER_SAMPLE_TYPE_DTO = new openbis.SampleType()
ANOTHER_SAMPLE_TYPE_DTO.setCode('ANOTHER_SAMPLE_TYPE')

const TEST_EXPERIMENT_TYPE_DTO = new openbis.ExperimentType()
TEST_EXPERIMENT_TYPE_DTO.setCode('TEST_TYPE')

const ANOTHER_EXPERIMENT_TYPE_DTO = new openbis.ExperimentType()
ANOTHER_EXPERIMENT_TYPE_DTO.setCode('ANOTHER_EXPERIMENT_TYPE')

const TEST_DATA_SET_TYPE_DTO = new openbis.DataSetType()
TEST_DATA_SET_TYPE_DTO.setCode('TEST_TYPE')

const ANOTHER_DATA_SET_TYPE_DTO = new openbis.DataSetType()
ANOTHER_DATA_SET_TYPE_DTO.setCode('ANOTHER_DATA_SET_TYPE')

const TEST_MATERIAL_TYPE_DTO = new openbis.MaterialType()
TEST_MATERIAL_TYPE_DTO.setCode('TEST_TYPE')

const ANOTHER_MATERIAL_TYPE_DTO = new openbis.MaterialType()
ANOTHER_MATERIAL_TYPE_DTO.setCode('ANOTHER_MATERIAL_TYPE')

export default {
  TEST_USER,
  TEST_PASSWORD,
  TEST_SESSION_TOKEN,
  TEST_USER_DTO,
  ANOTHER_USER_DTO,
  ALL_USERS_GROUP_DTO,
  TEST_GROUP_DTO,
  ANOTHER_GROUP_DTO,
  TEST_PLUGIN_DTO,
  TEST_PROPERTY_TYPE_1_DTO,
  TEST_PROPERTY_TYPE_2_DTO,
  TEST_PROPERTY_TYPE_3_DTO,
  TEST_SAMPLE_TYPE_DTO,
  ANOTHER_SAMPLE_TYPE_DTO,
  TEST_EXPERIMENT_TYPE_DTO,
  ANOTHER_EXPERIMENT_TYPE_DTO,
  TEST_DATA_SET_TYPE_DTO,
  ANOTHER_DATA_SET_TYPE_DTO,
  TEST_MATERIAL_TYPE_DTO,
  ANOTHER_MATERIAL_TYPE_DTO
}
