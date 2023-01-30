/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;

/**
 * An object that captures the state for performing the registration of one or many openBIS entities atomically.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicEntityOperationDetails implements Serializable
{
    private static final long serialVersionUID = 1L;

    // The unique identifier for this registration
    private final TechId registrationIdOrNull;

    // The userid on whose behalf the operations are done, or null if not done on behalf of another
    // user
    private final String userIdOrNull;

    private final List<ExperimentUpdatesDTO> experimentUpdates;

    private final List<NewSpace> spaceRegistrations;

    private final List<NewProject> projectRegistrations;

    private final List<ProjectUpdatesDTO> projectUpdates;

    private final List<NewExperiment> experimentRegistrations;

    private final List<SampleUpdatesDTO> sampleUpdates;

    private final List<NewSample> sampleRegistrations;

    private final List<NewMetaproject> metaprojectRegistrations;

    private final List<MetaprojectUpdatesDTO> metaprojectUpdates;

    private final Map<String /* material type */, List<NewMaterial>> materialRegistrations;

    private final List<MaterialUpdateDTO> materialUpdates;

    private final List<? extends NewExternalData> dataSetRegistrations;

    private final List<DataSetBatchUpdatesDTO> dataSetUpdates;

    private final List<VocabularyUpdatesDTO> vocabularyUpdates;

    private final List<SpaceRoleAssignment> spaceRoleAssignments;

    private final List<SpaceRoleAssignment> spaceRoleRevocations;

    private Integer batchSizeOrNull;

    public AtomicEntityOperationDetails(TechId registrationId, String userIdOrNull,
            List<NewSpace> spaceRegistrations, List<NewProject> projectRegistrations,
            List<ProjectUpdatesDTO> projectUpdates, List<NewExperiment> experimentRegistrations,
            List<ExperimentUpdatesDTO> experimentUpdates, List<SampleUpdatesDTO> sampleUpdates,
            List<NewSample> sampleRegistrations,
            Map<String, List<NewMaterial>> materialRegistrations,
            List<MaterialUpdateDTO> materialUpdates,
            List<? extends NewExternalData> dataSetRegistrations,
            List<DataSetBatchUpdatesDTO> dataSetUpdates,
            List<NewMetaproject> metaprojectRegistrations,
            List<MetaprojectUpdatesDTO> metaprojectUpdates,
            List<VocabularyUpdatesDTO> vocabularyUpdates,
            List<SpaceRoleAssignment> spaceRoleAssignments,
            List<SpaceRoleAssignment> spaceRoleRevocations)
    {
        this.registrationIdOrNull = registrationId;
        this.userIdOrNull = userIdOrNull;
        this.spaceRegistrations = new ArrayList<NewSpace>(spaceRegistrations);
        this.projectRegistrations = new ArrayList<NewProject>(projectRegistrations);
        this.projectUpdates = new ArrayList<ProjectUpdatesDTO>(projectUpdates);
        this.experimentUpdates = new ArrayList<ExperimentUpdatesDTO>(experimentUpdates);
        this.experimentRegistrations = new ArrayList<NewExperiment>(experimentRegistrations);
        this.sampleUpdates = new ArrayList<SampleUpdatesDTO>(sampleUpdates);
        this.sampleRegistrations = new ArrayList<NewSample>(sampleRegistrations);
        this.materialRegistrations = new TreeMap<String, List<NewMaterial>>(materialRegistrations);
        this.materialUpdates = new ArrayList<MaterialUpdateDTO>(materialUpdates);
        this.dataSetRegistrations = new ArrayList<NewExternalData>(dataSetRegistrations);
        this.dataSetUpdates = new ArrayList<DataSetBatchUpdatesDTO>(dataSetUpdates);
        this.metaprojectRegistrations = new ArrayList<NewMetaproject>(metaprojectRegistrations);
        this.metaprojectUpdates = new ArrayList<MetaprojectUpdatesDTO>(metaprojectUpdates);
        this.vocabularyUpdates = new ArrayList<VocabularyUpdatesDTO>(vocabularyUpdates);
        this.spaceRoleAssignments = new ArrayList<SpaceRoleAssignment>(spaceRoleAssignments);
        this.spaceRoleRevocations = new ArrayList<SpaceRoleAssignment>(spaceRoleRevocations);
    }

    public AtomicEntityOperationDetails(TechId registrationId, String userIdOrNull,
            List<NewSpace> spaceRegistrations, List<NewProject> projectRegistrations,
            List<ProjectUpdatesDTO> projectUpdates, List<NewExperiment> experimentRegistrations,
            List<ExperimentUpdatesDTO> experimentUpdates, List<SampleUpdatesDTO> sampleUpdates,
            List<NewSample> sampleRegistrations,
            Map<String, List<NewMaterial>> materialRegistrations,
            List<MaterialUpdateDTO> materialUpdates,
            List<? extends NewExternalData> dataSetRegistrations,
            List<DataSetBatchUpdatesDTO> dataSetUpdates,
            List<NewMetaproject> metaprojectRegistrations,
            List<MetaprojectUpdatesDTO> metaprojectUpdates,
            List<VocabularyUpdatesDTO> vocabularyUpdates)
    {
        this(registrationId, userIdOrNull, spaceRegistrations, projectRegistrations,
                projectUpdates, experimentRegistrations, experimentUpdates, sampleUpdates,
                sampleRegistrations, materialRegistrations, materialUpdates, dataSetRegistrations,
                dataSetUpdates, metaprojectRegistrations, metaprojectUpdates, vocabularyUpdates, new ArrayList<SpaceRoleAssignment>(),
                new ArrayList<SpaceRoleAssignment>());
    }

    public AtomicEntityOperationDetails(TechId registrationId, String userIdOrNull,
            List<NewSpace> spaceRegistrations, List<NewProject> projectRegistrations,
            List<ProjectUpdatesDTO> projectUpdates, List<NewExperiment> experimentRegistrations,
            List<ExperimentUpdatesDTO> experimentUpdates, List<SampleUpdatesDTO> sampleUpdates,
            List<NewSample> sampleRegistrations,
            Map<String, List<NewMaterial>> materialRegistrations,
            List<MaterialUpdateDTO> materialUpdates,
            List<? extends NewExternalData> dataSetRegistrations,
            List<DataSetBatchUpdatesDTO> dataSetUpdates,
            List<NewMetaproject> metaprojectRegistrations,
            List<MetaprojectUpdatesDTO> metaprojectUpdates,
            List<VocabularyUpdatesDTO> vocabularyUpdates, Integer batchSizeOrNull)
    {
        this(registrationId, userIdOrNull, spaceRegistrations, projectRegistrations,
                projectUpdates, experimentRegistrations, experimentUpdates, sampleUpdates,
                sampleRegistrations, materialRegistrations, materialUpdates, dataSetRegistrations,
                dataSetUpdates, metaprojectRegistrations, metaprojectUpdates, vocabularyUpdates);
        this.batchSizeOrNull = batchSizeOrNull;
    }

    public TechId getRegistrationIdOrNull()
    {
        return registrationIdOrNull;
    }

    public String tryUserIdOrNull()
    {
        return userIdOrNull;
    }

    public List<ExperimentUpdatesDTO> getExperimentUpdates()
    {
        return experimentUpdates;
    }

    public List<NewExperiment> getExperimentRegistrations()
    {
        return experimentRegistrations;
    }

    public List<SampleUpdatesDTO> getSampleUpdates()
    {
        return sampleUpdates;
    }

    public List<NewSample> getSampleRegistrations()
    {
        return sampleRegistrations;
    }

    public List<NewMetaproject> getMetaprojectRegistrations()
    {
        return metaprojectRegistrations;
    }

    public List<? extends NewExternalData> getDataSetRegistrations()
    {
        return dataSetRegistrations;
    }

    public List<DataSetBatchUpdatesDTO> getDataSetUpdates()
    {
        return dataSetUpdates;
    }

    public List<NewSpace> getSpaceRegistrations()
    {
        return spaceRegistrations;
    }

    public List<NewProject> getProjectRegistrations()
    {
        return projectRegistrations;
    }

    public List<ProjectUpdatesDTO> getProjectUpdates()
    {
        return projectUpdates;
    }

    public Map<String, List<NewMaterial>> getMaterialRegistrations()
    {
        return materialRegistrations;
    }

    public List<MaterialUpdateDTO> getMaterialUpdates()
    {
        return materialUpdates;
    }

    public List<MetaprojectUpdatesDTO> getMetaprojectUpdates()
    {
        return metaprojectUpdates;
    }

    public List<VocabularyUpdatesDTO> getVocabularyUpdates()
    {
        return vocabularyUpdates;
    }

    public Integer getBatchSizeOrNull()
    {
        return batchSizeOrNull;
    }

    public List<SpaceRoleAssignment> getSpaceRoleAssignments()
    {
        return spaceRoleAssignments;
    }

    public List<SpaceRoleAssignment> getSpaceRoleRevocations()
    {
        return spaceRoleRevocations;
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        appendTo(sb, "registrationIdOrNull", registrationIdOrNull);
        appendTo(sb, "userIdOrNull", userIdOrNull);
        appendTo(sb, "spaceRegistrations", spaceRegistrations);
        appendTo(sb, "projectRegistrations", projectRegistrations);
        appendTo(sb, "projectUpdates", projectUpdates);
        appendTo(sb, "experimentUpdates", experimentUpdates);
        appendTo(sb, "experimentRegistrations", experimentRegistrations);
        appendTo(sb, "sampleUpdates", sampleUpdates);
        appendTo(sb, "sampleRegistrations", sampleRegistrations);
        appendTo(sb, "materialRegistrations", materialRegistrations);
        appendTo(sb, "dataSetRegistrations", dataSetRegistrations);
        appendTo(sb, "dataSetUpdates", dataSetUpdates);
        appendTo(sb, "metaprojectRegistrations", metaprojectRegistrations);
        appendTo(sb, "metaprojectUpdates", metaprojectUpdates);
        appendTo(sb, "vocabularyUpdates", vocabularyUpdates);
        appendTo(sb, "spaceRoleAssignments", spaceRoleAssignments);
        appendTo(sb, "spaceRoleRevocations", spaceRoleRevocations);
        return sb.toString();
    }
    
    private void appendTo(ToStringBuilder builder, String name, Object object)
    {
        if (object == null)
        {
            return;
        }
        if (object instanceof Map<?, ?>)
        {
            object = ((Map<?, ?>) object).entrySet();
        }
        if (object instanceof Collection<?>)
        {
            Collection<?> collection = (Collection<?>) object;
            if (collection.isEmpty())
            {
                return;
            }
            builder.append(name, CollectionUtils.abbreviate(collection, 100));
        } else
        {
            builder.append(name, object);
        }
    }

}
