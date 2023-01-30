/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;

/**
 * @author Franz-Josef Elmer
 */
public class AtomicEntityOperationDetailsBuilder
{
    private final List<ExperimentUpdatesDTO> experimentUpdates =
            new ArrayList<ExperimentUpdatesDTO>();

    private final List<NewSpace> spaceRegistrations = new ArrayList<NewSpace>();

    private final List<NewProject> projectRegistrations = new ArrayList<NewProject>();

    private final List<ProjectUpdatesDTO> projectUpdates = new ArrayList<ProjectUpdatesDTO>();

    private final List<NewExperiment> experimentRegistrations = new ArrayList<NewExperiment>();

    private final List<SampleUpdatesDTO> sampleUpdates = new ArrayList<SampleUpdatesDTO>();

    private final List<NewSample> sampleRegistrations = new ArrayList<NewSample>();

    private final Map<String /* material type */, List<NewMaterial>> materialRegistrations =
            new HashMap<String, List<NewMaterial>>();

    private final List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();

    private final List<NewExternalData> dataSetRegistrations = new ArrayList<NewExternalData>();

    private final List<DataSetBatchUpdatesDTO> dataSetUpdates =
            new ArrayList<DataSetBatchUpdatesDTO>();

    private final List<NewMetaproject> metaprojectRegistrations = new ArrayList<NewMetaproject>();

    private final List<MetaprojectUpdatesDTO> metaprojectUpdates =
            new ArrayList<MetaprojectUpdatesDTO>();

    private final List<VocabularyUpdatesDTO> vocabularyUpdates =
            new ArrayList<VocabularyUpdatesDTO>();

    private TechId registrationIdOrNull;

    private String userIdOrNull;

    private Integer batchSizeOrNull;

    public AtomicEntityOperationDetailsBuilder user(String userId)
    {
        userIdOrNull = userId;
        return this;
    }

    public AtomicEntityOperationDetailsBuilder batchSize(int size)
    {
        batchSizeOrNull = size;
        return this;
    }

    public AtomicEntityOperationDetailsBuilder project(NewProject newProject)
    {
        projectRegistrations.add(newProject);
        return this;
    }

    public AtomicEntityOperationDetailsBuilder projectUpdate(
            ProjectUpdatesDTO projectUpdate)
    {
        projectUpdates.add(projectUpdate);
        return this;
    }

    public AtomicEntityOperationDetailsBuilder experiment(NewExperiment newExperiment)
    {
        experimentRegistrations.add(newExperiment);
        return this;
    }

    public AtomicEntityOperationDetailsBuilder experimentUpdate(
            ExperimentUpdatesDTO experimentUpdate)
    {
        experimentUpdates.add(experimentUpdate);
        return this;
    }

    public AtomicEntityOperationDetailsBuilder sample(NewSample newSample)
    {
        sampleRegistrations.add(newSample);
        return this;
    }

    public AtomicEntityOperationDetailsBuilder sampleUpdate(SampleUpdatesDTO sampleUpdate)
    {
        sampleUpdates.add(sampleUpdate);
        return this;
    }

    public AtomicEntityOperationDetailsBuilder dataSet(NewExternalData newDataSet)
    {
        dataSetRegistrations.add(newDataSet);
        return this;
    }

    public AtomicEntityOperationDetailsBuilder dataSetUpdate(DataSetBatchUpdatesDTO dataSetUpdate)
    {
        dataSetUpdates.add(dataSetUpdate);
        return this;
    }

    public AtomicEntityOperationDetailsBuilder metaProject(NewMetaproject metaProject)
    {
        metaprojectRegistrations.add(metaProject);
        return this;
    }

    public AtomicEntityOperationDetailsBuilder materialUpdate(MaterialUpdateDTO materialUpdate)
    {
        materialUpdates.add(materialUpdate);
        return this;
    }

    public AtomicEntityOperationDetailsBuilder metaProjectUpdate(
            MetaprojectUpdatesDTO metaProjectUpdate)
    {
        metaprojectUpdates.add(metaProjectUpdate);
        return this;
    }

    public AtomicEntityOperationDetails getDetails()
    {
        return new AtomicEntityOperationDetails(registrationIdOrNull, userIdOrNull,
                spaceRegistrations, projectRegistrations, projectUpdates, experimentRegistrations,
                experimentUpdates, sampleUpdates, sampleRegistrations, materialRegistrations,
                materialUpdates, dataSetRegistrations, dataSetUpdates, metaprojectRegistrations,
                metaprojectUpdates, vocabularyUpdates, batchSizeOrNull);
    }

    public AtomicEntityOperationDetailsBuilder material(NewMaterialWithType newMaterial)
    {
        String type = newMaterial.getType();
        List<NewMaterial> list = materialRegistrations.get(type);
        if (list == null)
        {
            list = new ArrayList<NewMaterial>();
            materialRegistrations.put(type, list);
        }
        list.add(newMaterial.getMaterial());
        return this;
    }

    public AtomicEntityOperationDetailsBuilder space(NewSpace newSpace)
    {
        spaceRegistrations.add(newSpace);
        return this;
    }

}
