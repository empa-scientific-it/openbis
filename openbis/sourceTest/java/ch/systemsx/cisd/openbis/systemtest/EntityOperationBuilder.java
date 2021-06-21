/*
 * Copyright 2013 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.systemtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpaceRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;

public final class EntityOperationBuilder
{
    private static long counter = 1000;

    private final List<NewSpace> spaces = new ArrayList<NewSpace>();

    private final List<NewProject> projects = new ArrayList<NewProject>();

    private final List<ProjectUpdatesDTO> projectUpdates = new ArrayList<ProjectUpdatesDTO>();

    private final List<NewExperiment> experiments = new ArrayList<NewExperiment>();

    private final List<ExperimentUpdatesDTO> experimentUpdates =
            new ArrayList<ExperimentUpdatesDTO>();

    private final List<NewSample> samples = new ArrayList<NewSample>();

    private final List<SampleUpdatesDTO> sampleUpdates = new ArrayList<SampleUpdatesDTO>();

    private final List<NewExternalData> dataSets = new ArrayList<NewExternalData>();

    private final List<DataSetBatchUpdatesDTO> dataSetUpdates =
            new ArrayList<DataSetBatchUpdatesDTO>();

    private final Map<String, List<NewMaterial>> materials =
            new HashMap<String, List<NewMaterial>>();

    private final List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();

    private final List<NewMetaproject> metaprojectRegistrations =
            new ArrayList<NewMetaproject>();

    private final List<MetaprojectUpdatesDTO> metaprojectUpdates =
            new ArrayList<MetaprojectUpdatesDTO>();

    private final List<VocabularyUpdatesDTO> vocabularyUpdates =
            new ArrayList<VocabularyUpdatesDTO>();

    private final List<SpaceRoleAssignment> spaceRoleAssignments = new ArrayList<SpaceRoleAssignment>();

    private final List<SpaceRoleAssignment> spaceRoleRevocations = new ArrayList<SpaceRoleAssignment>();

    private TechId registrationID = new TechId(counter++);

    private String userID;

    EntityOperationBuilder user(String user)
    {
        this.userID = user;
        return this;
    }

    EntityOperationBuilder space(String code)
    {
        return space(new NewSpace(code, null, null));
    }

    EntityOperationBuilder space(String code, String user)
    {
        return space(new NewSpace(code, null, user));
    }

    EntityOperationBuilder space(NewSpace space)
    {
        spaces.add(space);
        return this;
    }

    EntityOperationBuilder material(String materialTypeCode, Material material)
    {
        List<NewMaterial> list = materials.get(materialTypeCode);
        if (list == null)
        {
            list = new ArrayList<NewMaterial>();
            materials.put(materialTypeCode, list);
        }
        list.add(MaterialTranslator.translateToNewMaterial(material));
        return this;
    }

    EntityOperationBuilder project(SpaceIdentifier spaceIdentifier, String projectCode)
    {
        String projectIdentifier =
                new ProjectIdentifier(spaceIdentifier, projectCode).toString();
        return project(new NewProject(projectIdentifier, null));
    }

    EntityOperationBuilder project(NewProject project)
    {
        projects.add(project);
        return this;
    }

    @SuppressWarnings("unused")
    EntityOperationBuilder project(ProjectUpdatesDTO project)
    {
        projectUpdates.add(project);
        return this;
    }

    EntityOperationBuilder experiment(Experiment experiment)
    {
        NewExperiment newExperiment =
                new NewExperiment(experiment.getIdentifier(), experiment.getEntityType()
                        .getCode());
        newExperiment.setPermID(experiment.getPermId());
        newExperiment.setProperties(experiment.getProperties().toArray(new IEntityProperty[0]));
        experiments.add(newExperiment);
        return this;
    }

    EntityOperationBuilder sample(Sample sample)
    {
        NewSample newSample = new NewSample();
        newSample.setIdentifier(sample.getIdentifier());
        newSample.setSampleType(sample.getSampleType());
        Experiment experiment = sample.getExperiment();
        if (experiment != null)
        {
            newSample.setExperimentIdentifier(experiment.getIdentifier());
        }
        newSample.setProperties(sample.getProperties().toArray(new IEntityProperty[0]));
        samples.add(newSample);
        return this;
    }

    EntityOperationBuilder sampleUpdate(Sample sample)
    {
        ExperimentIdentifier experimentIdentifier = null;
        Experiment experiment = sample.getExperiment();
        if (experiment != null) {
            experimentIdentifier = ExperimentIdentifierFactory.parse(experiment.getIdentifier());
        }
        sampleUpdates.add(new SampleUpdatesDTO(new TechId(sample), sample.getProperties(),
                experimentIdentifier, null, null, sample.getVersion(), SampleIdentifierFactory.parse(sample
                        .getIdentifier()), null, null));
        return this;
    }

    EntityOperationBuilder dataSet(AbstractExternalData dataSet)
    {
        NewExternalData newExternalData = new NewExternalData();
        newExternalData.setCode(dataSet.getCode());
        newExternalData.setDataSetType(dataSet.getDataSetType());
        newExternalData.setDataSetKind(dataSet.getDataSetKind());
        newExternalData.setDataStoreCode(dataSet.getDataStore().getCode());
        if (dataSet instanceof PhysicalDataSet)
        {
            PhysicalDataSet realDataSet = (PhysicalDataSet) dataSet;
            newExternalData.setFileFormatType(realDataSet.getFileFormatType());
            newExternalData.setLocation(realDataSet.getLocation());
            newExternalData.setLocatorType(realDataSet.getLocatorType());
        }
        newExternalData.setStorageFormat(StorageFormat.PROPRIETARY);
        List<IEntityProperty> properties = dataSet.getProperties();
        List<NewProperty> newProperties = new ArrayList<NewProperty>();
        for (IEntityProperty property : properties)
        {
            newProperties.add(new NewProperty(property.getPropertyType().getCode(), property
                    .tryGetAsString()));
        }
        newExternalData.setDataSetProperties(newProperties);
        Sample sample = dataSet.getSample();
        if (sample != null)
        {
            newExternalData.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample
                    .getIdentifier()));
        }
        Experiment experiment = dataSet.getExperiment();
        if (experiment != null)
        {
            newExternalData.setExperimentIdentifierOrNull(ExperimentIdentifierFactory
                    .parse(experiment.getIdentifier()));
        }
        return dataSet(newExternalData);
    }

    public EntityOperationBuilder dataSet(NewExternalData newExternalData)
    {
        dataSets.add(newExternalData);
        return this;
    }

    EntityOperationBuilder dataSetUpdate(AbstractExternalData dataSet)
    {
        DataSetBatchUpdatesDTO dataSetUpdate = new DataSetBatchUpdatesDTO();
        dataSetUpdate.setDetails(new DataSetBatchUpdateDetails());
        dataSetUpdate.setDatasetId(new TechId(dataSet));
        dataSetUpdate.setDatasetCode(dataSet.getCode());
        dataSetUpdate.setVersion(dataSet.getVersion());
        if (dataSet instanceof PhysicalDataSet)
        {
            PhysicalDataSet realDataSet = (PhysicalDataSet) dataSet;
            dataSetUpdate.setFileFormatTypeCode(realDataSet.getFileFormatType().getCode());
        }
        dataSetUpdate.setProperties(dataSet.getProperties());

        // Request an update of all properties
        HashSet<String> propertiesToUpdate = new HashSet<String>();
        for (IEntityProperty property : dataSet.getProperties())
        {
            propertiesToUpdate.add(property.getPropertyType().getCode());
        }
        dataSetUpdate.getDetails().setPropertiesToUpdate(propertiesToUpdate);

        Sample sample = dataSet.getSample();
        if (sample != null)
        {
            dataSetUpdate.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample
                    .getIdentifier()));
        }
        Experiment experiment = dataSet.getExperiment();
        if (experiment != null)
        {
            dataSetUpdate.setExperimentIdentifierOrNull(ExperimentIdentifierFactory
                    .parse(experiment.getIdentifier()));
        }
        dataSetUpdates.add(dataSetUpdate);
        return this;
    }

    EntityOperationBuilder assignRoleToSpace(RoleCode roleCode, String spaceCode, List<String> userIds, List<String> groupCodes)
    {
        SpaceRoleAssignment assignment = createSpaceRoleAssignment(roleCode, spaceCode, userIds, groupCodes);
        spaceRoleAssignments.add(assignment);
        return this;
    }

    EntityOperationBuilder revokeRoleFromSpace(RoleCode roleCode, String spaceCode, List<String> userIds, List<String> groupCodes)
    {
        SpaceRoleAssignment assignment = createSpaceRoleAssignment(roleCode, spaceCode, userIds, groupCodes);
        spaceRoleRevocations.add(assignment);
        return this;
    }

    private SpaceRoleAssignment createSpaceRoleAssignment(RoleCode roleCode, String spaceIdentifier, List<String> userIds, List<String> groupCodes)
    {
        SpaceRoleAssignment assignment = new SpaceRoleAssignment();
        assignment.setRoleCode(roleCode);
        assignment.setSpaceIdentifier(new SpaceIdentifierFactory(spaceIdentifier).createIdentifier());
        ArrayList<Grantee> grantees = new ArrayList<Grantee>();
        if (userIds != null)
        {
            for (String userId : userIds)
            {
                grantees.add(Grantee.createPerson(userId));
            }
        }

        if (groupCodes != null)
        {
            for (String code : groupCodes)
            {
                grantees.add(Grantee.createAuthorizationGroup(code));
            }
        }
        assignment.setGrantees(grantees);
        return assignment;
    }

    AtomicEntityOperationDetails create()
    {
        return new AtomicEntityOperationDetails(registrationID, userID, spaces, projects,
                projectUpdates, experiments, experimentUpdates, sampleUpdates, samples,
                materials, materialUpdates, dataSets, dataSetUpdates, metaprojectRegistrations,
                metaprojectUpdates, vocabularyUpdates, spaceRoleAssignments, spaceRoleRevocations);
    }

}