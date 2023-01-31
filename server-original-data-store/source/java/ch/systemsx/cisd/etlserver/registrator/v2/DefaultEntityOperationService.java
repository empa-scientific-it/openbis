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

package ch.systemsx.cisd.etlserver.registrator.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.registrator.IEntityOperationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpaceRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;

public class DefaultEntityOperationService<T extends DataSetInformation> implements
        IEntityOperationService<T>
{
    private final IOmniscientEntityRegistrator<T> registrator;

    private final ITopLevelDataSetRegistratorDelegate delegate;

    public DefaultEntityOperationService(IOmniscientEntityRegistrator<T> registrator,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        this.registrator = registrator;
        this.delegate = delegate;
    }

    @Override
    public AtomicEntityOperationResult performOperationsInApplicationServer(
            AtomicEntityOperationDetails<T> registrationDetails)
    {
        IEncapsulatedOpenBISService openBisService =
                registrator.getGlobalState().getOpenBisService();

        AtomicEntityOperationResult result =
                openBisService.performEntityOperations(convert(registrationDetails));

        ArrayList<DataSetInformation> registeredDataSets = new ArrayList<DataSetInformation>();
        for (DataSetRegistrationInformation<T> dsRegDetails : registrationDetails
                .getDataSetRegistrations())
        {
            registeredDataSets.add(dsRegDetails.getDataSetInformation());
        }

        delegate.didRegisterDataSets(registeredDataSets);

        return result;
    }

    private ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails convert(
            AtomicEntityOperationDetails<T> details)
    {

        List<NewSpace> spaceRegistrations = details.getSpaceRegistrations();
        List<NewProject> projectRegistrations = details.getProjectRegistrations();
        List<ProjectUpdatesDTO> projectUpdates = details.getProjectUpdates();
        List<NewExperiment> experimentRegistrations = details.getExperimentRegistrations();
        List<ExperimentUpdatesDTO> experimentUpdates = details.getExperimentUpdates();
        List<SampleUpdatesDTO> sampleUpdates = details.getSampleUpdates();
        List<NewSample> sampleRegistrations = details.getSampleRegistrations();
        Map<String, List<NewMaterial>> materialRegistrations = details.getMaterialRegistrations();
        List<MaterialUpdateDTO> materialUpdates = details.getMaterialUpdates();
        List<DataSetBatchUpdatesDTO> dataSetUpdates = details.getDataSetUpdates();
        List<NewMetaproject> metaprojectRegistrations = details.getMetaprojectRegistrations();
        List<MetaprojectUpdatesDTO> metaprojectUpdates = details.getMetaprojectUpdates();
        List<VocabularyUpdatesDTO> vocabularyUpdates = details.getVocabularyUpdates();
        List<SpaceRoleAssignment> spaceRoleAssignments = details.getSpaceRoleAssignments();
        List<SpaceRoleAssignment> spaceRoleRevocations = details.getSpaceRoleRevocations();
        boolean h5Folders = registrator.getGlobalState().getThreadParameters().hasH5AsFolders();
        boolean h5arFolders = registrator.getGlobalState().getThreadParameters().hasH5ArAsFolders();

        List<NewExternalData> dataSetRegistrations = new ArrayList<NewExternalData>();
        for (DataSetRegistrationInformation<?> dsRegistration : details.getDataSetRegistrations())
        {
            NewExternalData newExternalData = dsRegistration.getExternalData();
            newExternalData.setH5Folders(h5Folders);
            newExternalData.setH5arFolders(h5arFolders);
            dataSetRegistrations.add(newExternalData);
        }

        return new ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails(
                details.getRegistrationId(), details.tryUserIdOrNull(), spaceRegistrations,
                projectRegistrations, projectUpdates, experimentRegistrations, experimentUpdates,
                sampleUpdates, sampleRegistrations, materialRegistrations, materialUpdates,
                dataSetRegistrations, dataSetUpdates, metaprojectRegistrations, metaprojectUpdates,
                vocabularyUpdates, spaceRoleAssignments, spaceRoleRevocations);
    }

}