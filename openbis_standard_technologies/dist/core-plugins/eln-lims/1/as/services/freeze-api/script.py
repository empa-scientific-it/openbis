import java.util.ArrayList as ArrayList

import ch.systemsx.cisd.openbis.generic.server.ComponentNames as ComponentNames
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider as CommonServiceProvider

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions as SpaceFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId as SpacePermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space as Space

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria as ProjectSearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions as ProjectFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId as ProjectPermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project as Project

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria as ExperimentSearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions as ExperimentFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId as ExperimentPermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment as Experiment

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria as SampleSearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions as SampleFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId as SamplePermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample as Sample

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria as DataSetSearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions as DataSetFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId as DataSetPermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet as DataSet

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions as PersonFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId as PersonPermId
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role as Role
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel as RoleLevel


def process(context, parameters):
	method = parameters.get("method");
	result = None;
	
#	try:
	if method == "freeze":
		# 1. Get entity by type to verify existence and obtain its space code
		sessionToken = parameters.get("sessionToken");
		type = parameters.get("entityType");
		permId = parameters.get("permId");
		entity = getEntity(context.applicationService, sessionToken, type, permId);
		spaceCode = getSpace(entity);

		# 2. Verify that the user is an admin in such space
		userId = sessionToken.split("-")[0];
		isAdminOfSpace = isUserAdminOnSpace(context.applicationService, sessionToken, userId, spaceCode);
		
		# 3. Create Freeze List
		defaultFreezeList = freezeIfSameSpaceAndChildPolicy(context.applicationService, sessionToken, entity, spaceCode);
		result = "OK"
		
		# Debug Info
		print "sessionToken: " + sessionToken
		print "type: " + type
		print "permId: " + permId
		print "entity: " + str(entity)
		print "spaceCode: " + spaceCode
		print "userId: " + userId
		print "isAdminOfSpace: " + str(isAdminOfSpace)
		print "defaultFreezeList: " + str(defaultFreezeList)
		
		
#	except Exception as e:
#		result = str(e)
	return result;

def isUserAdminOnSpace(service, sessionToken, userId, spaceCode):
	id = PersonPermId(userId);
	personfetchOptions = PersonFetchOptions();
	personfetchOptions.withRoleAssignments().withSpace();
	persons = service.getPersons(sessionToken, [id], personfetchOptions);
	person = persons[id];
	for roleAssignment in person.getRoleAssignments():
		if roleAssignment.getRole() == Role.ADMIN and roleAssignment.getRoleLevel() == RoleLevel.INSTANCE:
			return True
		if roleAssignment.getRole() == Role.ADMIN and roleAssignment.getRoleLevel() == RoleLevel.SPACE and roleAssignment.getSpace().getCode() == spaceCode:
			return True
	return False

def freezeIfSameSpaceAndChildPolicy(service, sessionToken, entity, spaceCode):
	entitiesToFreeze = {};
	entitiesToExpand = {};
	entitiesToExpand[entity.__class__.__name__ +"+"+ entity.getPermId().getPermId()] = entity;

	while entitiesToExpand:
		id = next(iter(entitiesToExpand));
		entityToExpand = entitiesToExpand[id];
		del entitiesToExpand[id];
		id = id.split("+")[1];
		entityToExpandSpaceCode = getSpace(entityToExpand);
		if entityToExpandSpaceCode == spaceCode:
			# Add entity without repetitions
			entitiesToFreeze[entityToExpand.__class__.__name__ +"+"+ entityToExpand.getPermId().getPermId()] = {
				"type" : entityToExpand.__class__.__name__,
				"permId" : entityToExpand.getPermId().getPermId(),
				"displayName" : getDisplayName(entityToExpand)
			};
			
			searchResults = None
			if isinstance(entityToExpand, Space):
				projectSearchCriteria = ProjectSearchCriteria();
				projectSearchCriteria.withSpace().withCode().thatEquals(id);
				projectFetchOptions = ProjectFetchOptions();
				projectFetchOptions.withSpace();
				searchResults = service.searchProjects(sessionToken, projectSearchCriteria, projectFetchOptions).getObjects();
			if isinstance(entityToExpand, Project):
				experimentSearchCriteria = ExperimentSearchCriteria();
				experimentSearchCriteria.withProject().withPermId().thatEquals(id);
				experimentFetchOptions = ExperimentFetchOptions();
				experimentFetchOptions.withProject().withSpace();
				experimentFetchOptions.withProperties();
				searchResults = service.searchExperiments(sessionToken, experimentSearchCriteria, experimentFetchOptions).getObjects();
			if isinstance(entityToExpand, Experiment):
				sampleSearchCriteria = SampleSearchCriteria();
				sampleSearchCriteria.withExperiment().withPermId().thatEquals(id);
				sampleFetchOptions = SampleFetchOptions();
				sampleFetchOptions.withSpace();
				sampleFetchOptions.withProperties();
				searchResults = service.searchSamples(sessionToken, sampleSearchCriteria, sampleFetchOptions).getObjects();
				
				dataSetSearchCriteria = DataSetSearchCriteria();
				dataSetSearchCriteria.withExperiment().withPermId().thatEquals(id);
				dataSetFetchOptions = DataSetFetchOptions();
				dataSetFetchOptions.withExperiment().withProject().withSpace();
				dataSetFetchOptions.withSample().withSpace();
				dataSetFetchOptions.withProperties();
				searchResults2 = service.searchDataSets(sessionToken, dataSetSearchCriteria, dataSetFetchOptions).getObjects();
				
				searchResults3 = ArrayList(searchResults)
				searchResults3.addAll(searchResults2)
				searchResults = searchResults3;
			if isinstance(entityToExpand, Sample):
				sampleSearchCriteria = SampleSearchCriteria();
				sampleSearchCriteria.withSpace().withCode().thatEquals(spaceCode);
				sampleSearchCriteria.withParents().withPermId().thatEquals(id);
				sampleFetchOptions = SampleFetchOptions();
				sampleFetchOptions.withSpace();
				sampleFetchOptions.withProperties();
				searchResults = service.searchSamples(sessionToken, sampleSearchCriteria, sampleFetchOptions).getObjects();
				
				dataSetSearchCriteria = DataSetSearchCriteria();
				dataSetSearchCriteria.withSample().withPermId().thatEquals(id);
				dataSetFetchOptions = DataSetFetchOptions();
				dataSetFetchOptions.withExperiment().withProject().withSpace();
				dataSetFetchOptions.withSample().withSpace();
				dataSetFetchOptions.withProperties();
				searchResults2 = service.searchDataSets(sessionToken, dataSetSearchCriteria, dataSetFetchOptions).getObjects();
				
				searchResults3 = ArrayList(searchResults)
				searchResults3.addAll(searchResults2)
				searchResults = searchResults3;
			if isinstance(entityToExpand, DataSet):
				dataSetSearchCriteria = DataSetSearchCriteria();
				dataSetSearchCriteria.withParents().withPermId().thatEquals(id);
				dataSetFetchOptions = DataSetFetchOptions();
				dataSetFetchOptions.withExperiment().withProject().withSpace();
				dataSetFetchOptions.withSample().withSpace();
				dataSetFetchOptions.withProperties();
				searchResults = service.searchDataSets(sessionToken, dataSetSearchCriteria, dataSetFetchOptions).getObjects();
			
			# Add results without repetitions
			for objectResult in searchResults:
				entitiesToExpand[objectResult.__class__.__name__ +"+"+ objectResult.getPermId().getPermId()] = objectResult;

	return entitiesToFreeze

def getEntity(service, sessionToken, type, permId):
	entity = None;
	if type == "SPACE":
		spaceFetchOptions = SpaceFetchOptions();
		id = SpacePermId(permId);
		entities = service.getSpaces(sessionToken, [id], spaceFetchOptions);
		entity = entities[id];
	if type == "PROJECT":
		projectFetchOptions = ProjectFetchOptions();
		projectFetchOptions.withSpace();
		id = ProjectPermId(permId)
		entities = service.getProjects(sessionToken, [id], projectFetchOptions);
		entity = entities[id];
	if type == "EXPERIMENT":
		experimentFetchOptions = ExperimentFetchOptions();
		experimentFetchOptions.withProject().withSpace();
		experimentFetchOptions.withProperties();
		id = ExperimentPermId(permId);
		entities = service.getExperiments(sessionToken, [id], experimentFetchOptions);
		entity = entities[id];
	if type == "SAMPLE":
		sampleFetchOptions = SampleFetchOptions();
		sampleFetchOptions.withSpace();
		sampleFetchOptions.withProperties();
		id = SamplePermId(permId);
		entities = service.getSamples(sessionToken, [id], sampleFetchOptions);
		entity = entities[id];
	if type == "DATASET":
		dataSetFetchOptions = DataSetFetchOptions();
		dataSetFetchOptions.withExperiment().withProject().withSpace();
		dataSetFetchOptions.withSample().withSpace();
		dataSetFetchOptions.withProperties();
		id = DataSetPermId(permId);
		entities = service.getDataSets(sessionToken, [id], dataSetFetchOptions);
		entity = entities[id];
	return entity

def getDisplayName(entity):
	displayName = None;
	if hasattr(entity, 'properties') and "$NAME" in entity.properties and entity.properties["$NAME"]:
		displayName = entity.properties["$NAME"];
	elif hasattr(entity, 'code') and entity.getCode():
		displayName = entity.getCode();
	elif hasattr(permId, 'permId'):
		displayName = entity.getPermId().getPermId();
	return displayName

def getSpace(entity):
	spaceCode = None;
	if isinstance(entity, Space):
		spaceCode = entity.getCode();
	if isinstance(entity, Project):
		spaceCode = entity.getSpace().getCode();
	if isinstance(entity, Experiment):
		spaceCode = entity.getProject().getSpace().getCode();
	if isinstance(entity, Sample):
		spaceCode = entity.getSpace().getCode();
	if isinstance(entity, DataSet):
		if entity.getSample() is not None:
			spaceCode = entity.getSample().getSpace().getCode();
		if entity.getExperiment() is not None:
			spaceCode = entity.getExperiment().getProject().getSpace().getCode();
	return spaceCode