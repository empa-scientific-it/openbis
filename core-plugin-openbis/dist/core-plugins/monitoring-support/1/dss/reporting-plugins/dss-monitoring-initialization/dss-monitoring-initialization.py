from __future__ import with_statement
import subprocess
from java.util import Arrays
from ch.systemsx.cisd.common.exceptions import UserFailureException
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id import SpacePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create import SpaceCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SampleIdentifier
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create import SampleCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id import EntityTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create import SampleTypeCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create import DataSetTypeCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id import PersonPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions import PersonFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create import PersonCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create import RoleAssignmentCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment import Role

def process(tr, parameters, tableBuilder):
    session_token = _get_mandatory_parameter(parameters, "sessionToken")
    data_set_code = _get_mandatory_parameter(parameters, "data set code")
    space_code = _get_mandatory_parameter(parameters, "space code")
    sample_code = _get_mandatory_parameter(parameters, "sample code")
    user_id = _get_mandatory_parameter(parameters, "user id")
    password = _get_mandatory_parameter(parameters, "password")

    data_set_type_code = _get_parameter(parameters, "data set type code", "MONITORED_DATA_SET")
    sample_type_code = _get_parameter(parameters, "sample type code", "MONITOR_SAMPLE")
    file_name = _get_parameter(parameters, "file name", "info.txt")
    file_content = _get_parameter(parameters, "file content", "blab blah")
    
    if tr.getSearchService().getDataSet(data_set_code) is None:
        _create_data_set_type(session_token, data_set_type_code)
        data_set = tr.createNewDataSet(data_set_type_code, data_set_code)
        sample_id = _create_sample(tr, session_token, space_code, sample_code, sample_type_code)
        data_set.setSample(sample_id)
        with open(tr.createNewFile(data_set, file_name), "w") as file:
            file.write(file_content)
    _create_user(session_token, user_id, password, space_code)
    
    tableBuilder.addHeader("STATUS")
    tableBuilder.addHeader("MESSAGE")
    tableBuilder.addHeader("RESULT")
    row = tableBuilder.addRow()
    row.setCell("STATUS","OK")
    row.setCell("MESSAGE", "Operation Successful")
    row.setCell("RESULT", "done")

def _create_data_set_type(session_token, data_set_type_code):
    v3 = ServiceProvider.getV3ApplicationService()
    data_set_type_permid = EntityTypePermId(data_set_type_code)
    if v3.getDataSetTypes(session_token, Arrays.asList(data_set_type_permid), DataSetTypeFetchOptions()).get(data_set_type_permid) is None:
        data_set_type_creation = DataSetTypeCreation()
        data_set_type_creation.setCode(data_set_type_code)
        v3.createDataSetTypes(session_token, Arrays.asList(data_set_type_creation))

def _create_sample(tr, session_token, space_code, sample_code, sample_type_code):
    v3 = ServiceProvider.getV3ApplicationService()
    space_permid = SpacePermId(space_code)
    if v3.getSpaces(session_token, Arrays.asList(space_permid), SpaceFetchOptions()).get(space_permid) is None:
        space_creation = SpaceCreation()
        space_creation.setCode(space_code)
        v3.createSpaces(session_token, Arrays.asList(space_creation))
    sample_id = SampleIdentifier(space_code, None, sample_code)
    if v3.getSamples(session_token, Arrays.asList(sample_id), SampleFetchOptions()).get(sample_id) is None:
        sample_type_permid = EntityTypePermId(sample_type_code)
        if v3.getSampleTypes(session_token, Arrays.asList(sample_type_permid), SampleTypeFetchOptions()).get(sample_type_permid) is None:
            sample_type_creation = SampleTypeCreation()
            sample_type_creation.setCode(sample_type_code)
            v3.createSampleTypes(session_token, Arrays.asList(sample_type_creation))
        sample_creation = SampleCreation()
        sample_creation.setTypeId(sample_type_permid)
        sample_creation.setSpaceId(space_permid)
        sample_creation.setCode(sample_code)
        v3.createSamples(session_token, Arrays.asList(sample_creation))
    return tr.getSearchService().getSample(sample_id.getIdentifier())

def _create_user(session_token, user_id, password, space_code):
    v3 = ServiceProvider.getV3ApplicationService()
    person_permid = PersonPermId(user_id)
    if v3.getPersons(session_token, Arrays.asList(person_permid), PersonFetchOptions()).get(person_permid) is None:
        passwdShPath = '../openBIS-server/jetty/bin/passwd.sh'
        subprocess.call([passwdShPath, 'add', user_id, '-p', password])
        creation = PersonCreation()
        creation.setUserId(user_id)
        v3.createPersons(session_token, Arrays.asList(creation))
        role_assignment_creation = RoleAssignmentCreation()
        role_assignment_creation.setUserId(person_permid)
        role_assignment_creation.setRole(Role.OBSERVER)
        role_assignment_creation.setSpaceId(SpacePermId(space_code))
        v3.createRoleAssignments(session_token, Arrays.asList(role_assignment_creation))

def _get_parameter(parameters, key, default_value):
    p = parameters.get(key)
    return p if p is not None else default_value

def _get_mandatory_parameter(parameters, key):
    p = parameters.get(key)
    if p is None:
        raise UserFailureException("Parameter '%s' not specified." % key)
    return p
