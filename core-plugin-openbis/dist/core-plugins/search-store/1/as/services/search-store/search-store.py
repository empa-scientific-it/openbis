#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import json
from ch.ethz.sis.openbis.generic.server.sharedapi.v3.json import GenericObjectMapper
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create import SampleCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id import EntityTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id import SpacePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id import ProjectPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SamplePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update import SampleUpdate
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete import SampleDeletionOptions


SAMPLE_TYPE = 'SEARCH_QUERY'
PROP_NAME = '$NAME'
PROP_SEARCH_CRITERIA = '$SEARCH_QUERY.SEARCH_CRITERIA'
PROP_FETCH_OPTIONS = '$SEARCH_QUERY.FETCH_OPTIONS'
PROP_CUSTOM_DATA = '$SEARCH_QUERY.CUSTOM_DATA'

PREFIX_XML = '<xml><![CDATA['
POSTFIX_XML = ']]></xml>'


def process(context, parameters):
    """ Entry point for all methods.
    :param parameters['method']: SAVE, UPDATE, LOAD or DELETE
    see specific functions for more parameters.
    """

    method = parameters['method']

    if method == 'SAVE':
        return save(context, parameters)
    elif method == 'UPDATE':
        return update(context, parameters)
    elif method == 'LOAD':
        return load(context, parameters)
    elif method == 'DELETE':
        return delete(context, parameters)
    return None


def save(context, parameters):
    """ Saves a search query in a sample.
    :param parameters['spacePermId']: (optional) permId of the space in which to save the search
    :param parameters['projectPermId']: (optional) permId of the project in which to save the search
    :param parameters['experimentPermId']: (optional) permId of the experiment in which to save the search
    :param parameters['name']: human readable name of the search
    :param parameters['searchCriteria']: V3 search criteria
    :param parameters['fetchOptions']: (optional) V3 fetch options
    :param parameters['customData']: (optional) additional data in JSON format
    """
    result = _save(context, parameters)
    if len(result) > 0 and result[0].permId is not None:
        permId = SamplePermId(result[0].permId)
        fetchOptions = _getSampleFetchOptions()
        result = context.applicationService.getSamples(context.sessionToken, [permId], fetchOptions)
        return result[permId]
    else:
        return result


def update(context, parameters):
    """ Updates an existing search.
    :param parameters['permId']: permId of the search sample to update
    :param parameters['name']: (optional) human readable name of the search
    :param parameters['searchCriteria']: (optional) V3 search crieria
    :param parameters['fetchOptions']: (optional) V3 fetch options
    :param parameters['customData']: (optional) additional data in JSON format
    """
    sampleUpdate = SampleUpdate()
    sampleUpdate.setSampleId(SamplePermId(parameters['permId']))
    if 'name' in parameters:
        sampleUpdate.setProperty(PROP_NAME, _serialize(parameters['name']))
    if 'searchCriteria' in parameters:
        sampleUpdate.setProperty(PROP_SEARCH_CRITERIA, _serialize(parameters['searchCriteria']))
    if 'fetchOptions' in parameters:
        sampleUpdate.setProperty(PROP_FETCH_OPTIONS, _serialize(parameters['fetchOptions']))
    if 'customData' in parameters:
        sampleUpdate.setProperty(PROP_CUSTOM_DATA, _serialize(parameters['customData']))

    context.applicationService.updateSamples(context.sessionToken, [sampleUpdate])
    return True


def load(context, parameters):
    """ Loads all stores query samples.
    """
    searchCriteria = SampleSearchCriteria()
    searchCriteria.withType().withCode().thatEquals(SAMPLE_TYPE)
    fetchOptions = _getSampleFetchOptions()
    result = context.applicationService.searchSamples(context.sessionToken, searchCriteria, fetchOptions)
    return result


def delete(context, parameters):
    """ Deletes a stored search.
    :param parameters['permId']: permId of the search to delete
    :param parameters['reason']: reason for deletion    
    """
    permId = SamplePermId(parameters['permId'])
    reason = parameters['reason']
    deletionOptions = SampleDeletionOptions()
    deletionOptions.setReason(reason)
    result = context.applicationService.deleteSamples(context.sessionToken, [permId], deletionOptions)
    return result


def _save(context, parameters):
    sampleCreation = SampleCreation()
    sampleCreation.setAutoGeneratedCode(True)
    sampleCreation.setTypeId(EntityTypePermId(SAMPLE_TYPE))
    if 'experimentPermId' in parameters:
        sampleCreation.setExperimentId(ExperimentPermId(parameters['experimentPermId']))
    if 'spacePermId' in parameters:
        sampleCreation.setSpaceId(SpacePermId(parameters['spacePermId']))
    if 'projectPermId' in parameters:
        sampleCreation.setProjectId(ProjectPermId(parameters['projectPermId']))

    sampleCreation.setProperty(PROP_NAME, parameters['name'])

    searchCriteriaValue = _serialize(parameters['searchCriteria'])
    sampleCreation.setProperty(PROP_SEARCH_CRITERIA, searchCriteriaValue)

    if 'fetchOptions' in parameters:
        fetchOptionsValue = _serialize(parameters['fetchOptions'])
        sampleCreation.setProperty(PROP_FETCH_OPTIONS, fetchOptionsValue)

    if 'customData' in parameters:
        customDataValue = _serialize(parameters['customData'])
        sampleCreation.setProperty(PROP_CUSTOM_DATA, customDataValue)

    return context.applicationService.createSamples(context.sessionToken, [sampleCreation])


def _getSampleFetchOptions():
    fetchOptions = SampleFetchOptions()
    fetchOptions.withProperties()
    fetchOptions.withExperiment().withProject().withSpace()
    fetchOptions.withRegistrator()
    fetchOptions.sortBy().modificationDate().desc()
    return fetchOptions


def _serialize(obj):
    jsonString = GenericObjectMapper().writer().writeValueAsString(obj)
    return PREFIX_XML + jsonString + POSTFIX_XML
