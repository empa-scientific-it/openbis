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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.DynamicTransactionQuery;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.DynamicTransactionQueryFactory;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.ITransactionalCommand;
import ch.systemsx.cisd.etlserver.registrator.api.impl.MkdirsCommand;
import ch.systemsx.cisd.etlserver.registrator.api.impl.MoveFileCommand;
import ch.systemsx.cisd.etlserver.registrator.api.impl.NewFileCommand;
import ch.systemsx.cisd.etlserver.registrator.api.impl.RollbackStack;
import ch.systemsx.cisd.etlserver.registrator.api.impl.RollbackStack.IRollbackStackDelegate;
import ch.systemsx.cisd.etlserver.registrator.api.impl.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperiment;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IMaterial;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IMetaproject;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IProject;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISpace;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IVocabularyTerm;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetStorageAlgorithm;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IMaterialImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IProjectImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpaceRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Abstract superclass for the states a DataSetRegistrationTransaction can be in.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractTransactionState<T extends DataSetInformation>
{
    protected final DataSetRegistrationTransaction<T> parent;

    protected AbstractTransactionState(DataSetRegistrationTransaction<T> parent)
    {
        this.parent = parent;
    }

    public boolean isCommitted()
    {
        return false;
    }

    public boolean isRolledback()
    {
        return false;
    }

    /**
     * The state where the transaction is still modifyiable.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class LiveTransactionState<T extends DataSetInformation> extends
            AbstractTransactionState<T>
    {

        // Default to polling every 10 seconds and waiting for up to 5 minutes
        private static int fileSystemAvailablityWaitCount = 6 * 5;

        private static int fileSystemAvailablityPollingWaitTimeMs = 10 * 1000;

        /**
         * These two variables determine together how long the rollback mechanism waits for a file system that has become unavailable and how often it
         * checks for the file system to become available.
         * <p>
         * The duration the rollback mechanism will wait before giving up equals waitTimeMS * waitCount;
         * <p>
         * Made public for testing.
         */
        public static void setFileSystemAvailabilityPollingWaitTimeAndWaitCount(int waitTimeMS,
                int waitCount)
        {
            fileSystemAvailablityWaitCount = waitTimeMS;
            fileSystemAvailablityPollingWaitTimeMs = waitCount;
        }

        // Keeps track of steps that have been executed and may need to be reverted. Elements are
        // kept in the order they need to be reverted.
        private final RollbackStack rollbackStack;

        // The directory to use as "local" for paths
        private final File workingDirectory;

        // The directory in which new data sets get staged
        private final File stagingDirectory;

        // The registration service that owns this transaction
        private final DataSetRegistrationService<T> registrationService;

        // The interface to openBIS
        private final IEncapsulatedOpenBISService openBisService;

        private final IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory;

        private final List<DataSet<? extends T>> registeredDataSets =
                new ArrayList<DataSet<? extends T>>();

        private final List<DataSetUpdatable> dataSetsToBeUpdated =
                new ArrayList<DataSetUpdatable>();

        private final List<Experiment> experimentsToBeRegistered = new ArrayList<Experiment>();

        private final List<ExperimentUpdatable> experimentsToBeUpdated =
                new ArrayList<ExperimentUpdatable>();

        private final List<Space> spacesToBeRegistered = new ArrayList<Space>();

        private final List<Project> projectsToBeRegistered = new ArrayList<Project>();

        private final List<Project> projectsToBeUpdated = new ArrayList<Project>();

        private final List<Sample> samplesToBeRegistered = new ArrayList<Sample>();

        private final List<Sample> samplesToBeUpdated = new ArrayList<Sample>();

        private final List<Material> materialsToBeRegistered = new ArrayList<Material>();

        private final List<Material> materialsToBeUpdated = new ArrayList<Material>();

        private final List<Metaproject> metaprojectsToBeRegistered = new ArrayList<Metaproject>();

        private final List<Metaproject> metaprojectsToBeUpdated = new ArrayList<Metaproject>();

        private final List<Vocabulary> vocabulariesToBeUpdated = new ArrayList<Vocabulary>();

        private final Map<String, DynamicTransactionQuery> queriesToCommit =
                new HashMap<String, DynamicTransactionQuery>();

        private String userIdOrNull = null;

        public LiveTransactionState(DataSetRegistrationTransaction<T> parent,
                RollbackStack rollbackStack, File workingDirectory, File stagingDirectory,
                DataSetRegistrationService<T> registrationService,
                IDataSetRegistrationDetailsFactory<T> registrationDetailsFactory)
        {
            super(parent);
            this.rollbackStack = rollbackStack;
            this.workingDirectory = workingDirectory;
            this.stagingDirectory = stagingDirectory;
            this.registrationService = registrationService;
            this.openBisService =
                    this.registrationService.getRegistratorContext().getGlobalState()
                            .getOpenBisService();
            this.registrationDetailsFactory = registrationDetailsFactory;
            this.userIdOrNull = registrationDetailsFactory.getUserIdOrNull();
        }

        public String getUserId()
        {
            return userIdOrNull;
        }

        public void setUserId(String userIdOrNull)
        {
            this.userIdOrNull = userIdOrNull;
        }

        public IDataSet createNewDataSet()
        {
            return createNewDataSet(registrationDetailsFactory, null, null);
        }

        public IDataSet createNewDataSet(String dataSetType)
        {
            return createNewDataSet(registrationDetailsFactory, dataSetType, null);
        }

        public IDataSet createNewDataSet(String dataSetType, DataSetKind dataSetKindOrNull)
        {
            return createNewDataSet(registrationDetailsFactory, dataSetType, null, dataSetKindOrNull);
        }

        public IDataSet createNewDataSet(String dataSetType, String dataSetCode)
        {
            return createNewDataSet(registrationDetailsFactory, dataSetType, dataSetCode);
        }

        public IDataSet createNewDataSet(String dataSetType, String dataSetCode, DataSetKind dataSetKindOrNull)
        {
            return createNewDataSet(registrationDetailsFactory, dataSetType, dataSetCode, dataSetKindOrNull);
        }

        public IDataSet createNewDataSet(DataSetRegistrationDetails<T> registrationDetails)
        {
            // Request a code, so we can keep the staging file name and the data set code in sync
            return createNewDataSet(registrationDetails, registrationDetails
                    .getDataSetInformation().getDataSetCode());
        }

        public IDataSet createNewDataSet(DataSetRegistrationDetails<T> registrationDetails,
                String specifiedCodeOrNull)
        {
            return createNewDataSet(registrationDetailsFactory, registrationDetails, null,
                    specifiedCodeOrNull, null);
        }

        public IDataSet createNewDataSet(IDataSetRegistrationDetailsFactory<T> factory,
                String dataSetTypeOrNull, String dataSetCodeOrNull)
        {
            return createNewDataSet(factory, null, dataSetTypeOrNull, dataSetCodeOrNull, null);
        }

        public IDataSet createNewDataSet(IDataSetRegistrationDetailsFactory<T> factory,
                String dataSetTypeOrNull, String dataSetCodeOrNull, DataSetKind dataSetKindOrNull)
        {
            return createNewDataSet(factory, null, dataSetTypeOrNull, dataSetCodeOrNull, dataSetKindOrNull);
        }

        private IDataSet createNewDataSet(IDataSetRegistrationDetailsFactory<T> factory,
                DataSetRegistrationDetails<T> registrationDetailsOrNull, String dataSetTypeOrNull,
                String specifiedCodeOrNull, DataSetKind dataSetKindOrNull)
        {
            DataSetRegistrationDetails<T> registrationDetails = registrationDetailsOrNull;
            if (null == registrationDetails)
            {
                // Create registration details for the new data set
                registrationDetails = factory.createDataSetRegistrationDetails();
            }
            if (null != dataSetTypeOrNull)
            {
                registrationDetails.setDataSetType(dataSetTypeOrNull);
            }
            if (null != dataSetKindOrNull)
            {
            	registrationDetails.setDataSetKind(ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind.valueOf(dataSetKindOrNull.name()));
            }
            final String dataSetCode;
            if (null == specifiedCodeOrNull)
            {
                dataSetCode = generateDataSetCode(registrationDetails);
                registrationDetails.getDataSetInformation().setDataSetCode(dataSetCode);
            } else
            {
                dataSetCode = specifiedCodeOrNull.toUpperCase();
            }
            registrationDetails.getDataSetInformation().setDataSetCode(dataSetCode);

            // Create a directory for the data set
            File stagingFolder = new File(stagingDirectory, dataSetCode);
            MkdirsCommand cmd = new MkdirsCommand(stagingFolder.getAbsolutePath());
            executeCommand(cmd);

            DataSet<T> dataSet = factory.createDataSet(registrationDetails, stagingFolder);

            // If the registration details already contains a sample or experiment, set it on the
            // new data set.

            SampleIdentifier sampleId =
                    registrationDetails.getDataSetInformation().getSampleIdentifier();
            if (null != sampleId)
            {
                ISampleImmutable sample = tryFindSampleToRegister(sampleId);
                if (sample == null)
                {
                    sample = parent.getSample(sampleId.toString());
                }
                dataSet.setSample(sample);
            }
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                    registrationDetails.getDataSetInformation().tryToGetSample();
            if (sample != null)
            {
                dataSet.setSample(new SampleImmutable(sample));
            }

            ExperimentIdentifier experimentId =
                    registrationDetails.getDataSetInformation().getExperimentIdentifier();
            if (null != experimentId)
            {
                IExperimentImmutable exp = tryFindExperimentToRegister(experimentId);
                if (exp == null)
                {
                    exp = parent.getExperiment(experimentId.toString());
                }
                dataSet.setExperiment(exp);
            }
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment =
                    registrationDetails.getDataSetInformation().tryToGetExperiment();
            if (null != experiment)
            {
                dataSet.setExperiment(new ExperimentImmutable(experiment));
            }

            List<String> parents =
                    registrationDetails.getDataSetInformation().getParentDataSetCodes();
            if (registrationDetails.getDataSetInformation().getParentDataSetCodes() != null)
            {
                dataSet.setParentDatasets(parents);
            }

            registeredDataSets.add(dataSet);
            return dataSet;
        }

        private IExperimentImmutable tryFindExperimentToRegister(ExperimentIdentifier experimentId)
        {
            for (Experiment expToBeRegistered : experimentsToBeRegistered)
            {
                if (isPointingTo(expToBeRegistered, experimentId))
                {
                    return expToBeRegistered;
                }
            }
            return null;
        }

        private static boolean isPointingTo(Experiment experiment, ExperimentIdentifier experimentId)
        {
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment expDTO =
                    experiment.getExperiment();
            if (expDTO.getIdentifier() != null
                    && expDTO.getIdentifier().equalsIgnoreCase(experimentId.toString()))
            {
                return true;
            }
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project project = expDTO.getProject();
            if (expDTO.getCode() == null || project == null || project.getSpace() == null)
            {
                return false;
            }
            return expDTO.getCode().equals(experimentId.getExperimentCode())
                    && project.getCode().equals(experimentId.getProjectCode())
                    && project.getSpace().getCode().equals(experimentId.getSpaceCode());
        }

        private SampleImmutable tryFindSampleToRegister(SampleIdentifier sampleId)
        {
            for (Sample sampleToBeRegistered : samplesToBeRegistered)
            {
                if (isPointingTo(sampleToBeRegistered, sampleId))
                {
                    return sampleToBeRegistered;
                }
            }
            return null;
        }

        private static boolean isPointingTo(Sample sample, SampleIdentifier sampleIdentifier)
        {
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sampleDTO = sample.getSample();
            if (sampleIdentifier.getSampleCode().equals(sampleDTO.getCode()) == false)
            {
                return false;
            }
            if (sampleIdentifier.isSpaceLevel())
            {
                return sampleIdentifier.getSpaceLevel().getSpaceCode()
                        .equals(sampleDTO.getSpace().getCode());
            }
            {
                return sampleDTO.getSpace() == null;
            }
        }

        public IDataSetImmutable getDataSet(String dataSetCode)
        {
            AbstractExternalData dataSet = openBisService.tryGetDataSet(dataSetCode);
            if (dataSet == null)
            {
                return null;
            } else
            {
                return new DataSetImmutable(dataSet, openBisService);
            }
        }

        public IDataSetUpdatable getDataSetForUpdate(String dataSetCode)
        {
            // See if we already have an updatable version of the data set
            DataSetUpdatable result = findDataSetLocally(dataSetCode);
            if (result != null)
            {
                return result;
            }

            AbstractExternalData dataSet = openBisService.tryGetDataSet(dataSetCode);
            if (dataSet == null)
            {
                return null;
            } else
            {
                result = new DataSetUpdatable(dataSet, openBisService);
                dataSetsToBeUpdated.add(result);
                return result;
            }
        }

        public IDataSetUpdatable makeDataSetMutable(IDataSetImmutable dataSet)
        {
            if (dataSet == null)
            {
                return null;
            }
            // Check if we already have an updatable dataSet for this one
            DataSetUpdatable result = findDataSetLocally(dataSet);
            if (result != null)
            {
                return result;
            }

            if (dataSet instanceof DataSetImmutable)
            {
                result = new DataSetUpdatable((DataSetImmutable) dataSet);
                dataSetsToBeUpdated.add(result);

                return result;
            }

            return null;
        }

        private DataSetUpdatable findDataSetLocally(String dataSetCode)
        {
            // This is a slow implementation. Could be sped up by using a hashmap in the future.
            for (DataSetUpdatable dataSet : dataSetsToBeUpdated)
            {
                if (dataSet.getDataSetCode().equalsIgnoreCase(dataSetCode))
                {
                    return dataSet;
                }
            }
            return null;
        }

        private DataSetUpdatable findDataSetLocally(IDataSetImmutable dataSetToFind)
        {
            // This is a slow implementation. Could be sped up by using a hashmap in the future.
            for (DataSetUpdatable dataSet : dataSetsToBeUpdated)
            {
                if (dataSet.equals(dataSetToFind))
                {
                    return dataSet;
                }
            }
            return null;
        }

        public ISample getSampleForUpdate(String sampleIdentifierString)
        {
            SampleIdentifier sampleIdentifier =
                    new SampleIdentifierFactory(sampleIdentifierString).createIdentifier();

            // Check if we already have an updatable sample for this one
            Sample result = findSampleLocally(sampleIdentifier);
            if (result != null)
            {
                return result;
            }

            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                    openBisService.tryGetSampleWithExperiment(sampleIdentifier);

            if (sample != null)
            {
                result = new Sample(sample);
                samplesToBeUpdated.add(result);
            }

            return result;
        }

        public ISample makeSampleMutable(ISampleImmutable sample)
        {
            if (sample == null)
            {
                return null;
            }
            // Check if we already have an updatable sample for this one
            Sample result = findSampleLocally(sample);
            if (result != null)
            {
                return result;
            }

            if (sample instanceof SampleImmutable)
            {
                result = new Sample((SampleImmutable) sample);
                samplesToBeUpdated.add(result);

                return result;
            }

            return null;
        }

        private Sample findSampleLocally(SampleIdentifier sampleIdentifier)
        {
            String sampleIdentifierString = sampleIdentifier.toString();
            // This is a slow implementation. Could be sped up by using a hashmap in the future.
            for (Sample sample : samplesToBeUpdated)
            {
                if (sample.getSampleIdentifier().equalsIgnoreCase(sampleIdentifierString))
                {
                    return sample;
                }
            }
            return null;
        }

        private Sample findSampleLocally(ISampleImmutable sampleToFind)
        {
            for (Sample sample : samplesToBeUpdated)
            {
                if (sample.equals(sampleToFind))
                {
                    return sample;
                }
            }
            return null;
        }

        public ISample createNewSample(String sampleIdentifierString, String sampleTypeCode)
        {
            String permId = openBisService.createPermId();
            Sample sample = new Sample(sampleIdentifierString, permId);
            sample.setSampleType(sampleTypeCode);
            samplesToBeRegistered.add(sample);
            return sample;
        }

        public ISample createNewSampleWithGeneratedCode(String spaceCode, String sampleTypeCode)
        {
            String permId = openBisService.createPermId();
            SampleType sampleType = openBisService.getSampleType(sampleTypeCode);

            String sampleIdentifierString;
            if (spaceCode == null || spaceCode.length() == 0)
            {
                sampleIdentifierString =
                        "/"
                                + openBisService.generateCodes(sampleType.getGeneratedCodePrefix(),
                                        EntityKind.SAMPLE, 1).get(0);
            } else
            {
                sampleIdentifierString =
                        "/"
                                + spaceCode
                                + "/"
                                + openBisService.generateCodes(sampleType.getGeneratedCodePrefix(),
                                        EntityKind.SAMPLE, 1).get(0);
            }

            Sample sample = new Sample(sampleIdentifierString, permId);
            sample.setSampleType(sampleTypeCode);
            samplesToBeRegistered.add(sample);
            return sample;
        }

        public IExperimentUpdatable getExperimentForUpdate(String experimentIdentifierString)
        {
            ExperimentIdentifier identifier =
                    ExperimentIdentifierFactory.parse(experimentIdentifierString);

            // Check if we already have an updatable experiment for this one
            ExperimentUpdatable result = findExperimentLocally(identifier);
            if (result != null)
            {
                return result;
            }

            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experimentOrNull =
                    openBisService.tryGetExperiment(identifier);

            if (experimentOrNull != null)
            {
                result = new ExperimentUpdatable(experimentOrNull);
                experimentsToBeUpdated.add(result);
            }

            return result;
        }

        public IExperimentUpdatable makeExperimentMutable(IExperimentImmutable experiment)
        {
            if (experiment == null)
            {
                return null;
            }
            // Check if we already have an updatable experiment for this one
            ExperimentUpdatable result = findExperimentLocally(experiment);
            if (result != null)
            {
                return result;
            }

            if (experiment instanceof ExperimentImmutable)
            {
                result =
                        new ExperimentUpdatable(((ExperimentImmutable) experiment).getExperiment());
                experimentsToBeUpdated.add(result);
                return result;
            }

            return null;
        }

        private ExperimentUpdatable findExperimentLocally(ExperimentIdentifier experimentIdentifier)
        {
            String experimentIdentifierString = experimentIdentifier.toString();
            // This is a slow implementation. Could be sped up by using a hashmap in the future.
            for (ExperimentUpdatable experiment : experimentsToBeUpdated)
            {
                if (experiment.getExperimentIdentifier().equalsIgnoreCase(experimentIdentifierString))
                {
                    return experiment;
                }
            }
            return null;
        }

        private ExperimentUpdatable findExperimentLocally(IExperimentImmutable experimentToFind)
        {
            for (ExperimentUpdatable experiment : experimentsToBeUpdated)
            {
                if (experiment.equals(experimentToFind))
                {
                    return experiment;
                }
            }
            return null;
        }

        public IExperiment createNewExperiment(String experimentIdentifierString,
                String experimentTypeCode)
        {
            String permId = openBisService.createPermId();
            Experiment experiment = new Experiment(experimentIdentifierString, permId);
            experiment.setExperimentType(experimentTypeCode);
            experimentsToBeRegistered.add(experiment);
            return experiment;
        }

        public ISpace createNewSpace(String spaceCode, String spaceAdminUserIdOrNull)
        {
            Space space = new Space(spaceCode, spaceAdminUserIdOrNull);
            spacesToBeRegistered.add(space);
            return space;
        }

        public IProject createNewProject(String projectIdentifier)
        {
            Project project = new Project(projectIdentifier);
            projectsToBeRegistered.add(project);
            return project;
        }

        public IProject getProjectForUpdate(String projectIdentifier)
        {
            final ProjectIdentifier identifier = ProjectIdentifierFactory.parse(projectIdentifier);

            // Check if we already have an updatable project for this one
            Project result = findProjectLocally(identifier);
            if (result != null)
            {
                return result;
            }

            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project projectOrNull =
                    openBisService.tryGetProject(identifier);

            if (projectOrNull != null)
            {
                result = new Project(projectOrNull);
                projectsToBeUpdated.add(result);
            }

            return result;
        }

        public IProject makeProjectMutable(IProjectImmutable project)
        {
            if (project == null)
            {
                return null;
            }
            // Check if we already have an updatable project for this one
            Project result = findProjectLocally(project);
            if (result != null)
            {
                return result;
            }

            if (project instanceof ProjectImmutable)
            {
                result = new Project(((ProjectImmutable) project).getProject());
                projectsToBeUpdated.add(result);
                return result;
            }

            return null;
        }

        private Project findProjectLocally(ProjectIdentifier projectIdentifier)
        {
            String projectIdentifierString = projectIdentifier.toString();
            // This is a slow implementation. Could be sped up by using a hashmap in the future.
            for (Project project : projectsToBeUpdated)
            {
                if (project.getProjectIdentifier().equalsIgnoreCase(projectIdentifierString))
                {
                    return project;
                }
            }
            return null;
        }

        private Project findProjectLocally(IProjectImmutable projectToFind)
        {
            for (Project project : projectsToBeUpdated)
            {
                if (project.equals(projectToFind))
                {
                    return project;
                }
            }
            return null;
        }

        public IMaterial createNewMaterial(String materialCode, String materialType)
        {
            Material material = new Material(materialCode, materialType);
            materialsToBeRegistered.add(material);
            return material;
        }

        public IMaterial getMaterialForUpdate(String materialCode, String materialType)
        {

            MaterialIdentifier materialIdentifier =
                    new MaterialIdentifier(materialCode, materialType);

            // Check if we already have an updatable material for this one
            Material result = findMaterialLocally(materialIdentifier);
            if (result != null)
            {
                return result;
            }

            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material materialOrNull =
                    openBisService.tryGetMaterial(materialIdentifier);

            if (materialOrNull != null)
            {
                result = new Material(materialOrNull);
                materialsToBeUpdated.add(result);
            }

            return result;
        }

        public IMaterial makeMaterialMutable(IMaterialImmutable material)
        {
            if (material == null)
            {
                return null;
            }
            // Check if we already have an updatable material for this one
            Material result = findMaterialLocally(material);
            if (result != null)
            {
                return result;
            }

            if (material instanceof MaterialImmutable)
            {
                result = new Material(((MaterialImmutable) material).getMaterial());
                materialsToBeUpdated.add(result);
                return result;
            }

            return null;
        }

        private Material findMaterialLocally(MaterialIdentifier materialIdentifier)
        {
            String materialIdentifierString = materialIdentifier.toString();
            // This is a slow implementation. Could be sped up by using a hashmap in the future.
            for (Material material : materialsToBeUpdated)
            {
                if (material.getMaterialIdentifier().equalsIgnoreCase(materialIdentifierString))
                {
                    return material;
                }
            }
            return null;
        }

        private Material findMaterialLocally(IMaterialImmutable materialToFind)
        {
            for (Material material : materialsToBeUpdated)
            {
                if (material.equals(materialToFind))
                {
                    return material;
                }
            }
            return null;
        }

        public IMetaproject createNewMetaproject(String name, String description, String ownerId)
        {
            Metaproject metaproject = Metaproject.createMetaproject(name, description, ownerId);
            metaprojectsToBeRegistered.add(metaproject);
            return metaproject;
        }

        public Metaproject getMetaproject(String name, String ownerId)
        {
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject dto =
                    openBisService.tryGetMetaproject(name, ownerId);
            if (dto == null)
            {
                return null;
            }

            Metaproject metaproject = findExistingMetaprojectLocally(dto);
            if (metaproject == null)
            {
                metaproject = new Metaproject(dto);
                metaprojectsToBeUpdated.add(metaproject);
            }

            return metaproject;
        }

        private Metaproject findExistingMetaprojectLocally(
                ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject dto)
        {
            for (Metaproject m : metaprojectsToBeUpdated)
            {
                if (m.getId() == dto.getId())
                {
                    return m;
                }
            }
            return null;
        }

        public Vocabulary getVocabularyForUpdate(String code)
        {
            for (Vocabulary v : vocabulariesToBeUpdated)
            {
                if (v.getCode().equals(code))
                {
                    return v;
                }
            }
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary vocabulary =
                    openBisService.tryGetVocabulary(code);
            if (vocabulary != null)
            {
                Vocabulary apiVocabulary = new Vocabulary(vocabulary);
                vocabulariesToBeUpdated.add(apiVocabulary);
                return apiVocabulary;
            }
            return null;
        }

        public String moveFile(String src, IDataSet dst)
        {
            File srcFile = new File(src);
            return moveFile(src, dst, srcFile.getName());
        }

        public String moveFile(String src, IDataSet dst, String dstInDataset)
        {
            @SuppressWarnings("unchecked")
            DataSet<T> dataSet = (DataSet<T>) dst;

            // See if this is an absolute path
            File srcFile = new File(src);
            if (false == srcFile.exists())
            {
                // Try it relative
                srcFile = new File(workingDirectory, src);
                if (false == srcFile.exists())
                {
                    // The file could not be found
                    throw CheckedExceptionTunnel.wrapIfNecessary(new FileNotFoundException(
                            "Neither '" + src + "' nor '" + srcFile.getAbsolutePath()
                                    + "' were found."));
                }
            }

            File dataSetFolder = dataSet.getDataSetStagingFolder();
            File dstFile = new File(dataSetFolder, dstInDataset);

            FileUtilities.checkInputFile(srcFile);

            // Make parent directories if necessary
            File dstFolder = dstFile.getParentFile();
            mkdirsIfNeeded(dstFolder);

            MoveFileCommand cmd =
                    new MoveFileCommand(srcFile.getParentFile().getAbsolutePath(),
                            srcFile.getName(), dstFolder.getAbsolutePath(), dstFile.getName());
            executeCommand(cmd);
            return dstFile.getAbsolutePath();
        }

        /**
         * Recursively add folder creation commands to the rollback stack as necessary.
         * <p>
         * Discussion: The operation needs to be recursive so that on a rollback, children will have been deleted before the parent gets deleted. This
         * is required because the folder must be empty for delete to succeed.
         */
        private void mkdirsIfNeeded(File dstFolder)
        {
            File parentDir = dstFolder.getParentFile();
            if (false == parentDir.exists())
            {
                mkdirsIfNeeded(parentDir);
            }
            if (false == dstFolder.exists())
            {
                MkdirsCommand cmd = new MkdirsCommand(dstFolder.getAbsolutePath());
                executeCommand(cmd);
            }
        }

        public String createNewDirectory(IDataSet dst, String dirName)
        {
            @SuppressWarnings("unchecked")
            DataSet<T> dataSet = (DataSet<T>) dst;
            File dataSetFolder = dataSet.getDataSetStagingFolder();
            File dstFile = new File(dataSetFolder, dirName);
            MkdirsCommand cmd = new MkdirsCommand(dstFile.getAbsolutePath());
            executeCommand(cmd);
            return dstFile.getAbsolutePath();
        }

        public String createNewFile(IDataSet dst, String fileName)
        {
            return createNewFile(dst, "/", fileName);
        }

        public String createNewFile(IDataSet dst, String dstInDataset, String fileName)
        {
            @SuppressWarnings("unchecked")
            DataSet<T> dataSet = (DataSet<T>) dst;
            File dataSetFolder = dataSet.getDataSetStagingFolder();
            File dstFolder = new File(dataSetFolder, dstInDataset);
            File dstFile = new File(dstFolder, fileName);
            NewFileCommand cmd = new NewFileCommand(dstFile.getAbsolutePath());
            executeCommand(cmd);
            return dstFile.getAbsolutePath();
        }

        public DynamicTransactionQuery getDatabaseQuery(String dataSourceName)
        {
            DynamicTransactionQuery query = queriesToCommit.get(dataSourceName);
            if (null == query)
            {
                DynamicTransactionQueryFactory factory =
                        registrationService.getRegistratorContext().getGlobalState()
                                .getDynamicTransactionQueryFactory();
                query = factory.createDynamicTransactionQuery(dataSourceName);
                queriesToCommit.put(dataSourceName, query);
            }
            return query;
        }

        public void deleteFile(String src)
        {
            throw new NotImplementedException();
        }

        /**
         * Commit the transaction.
         * 
         * @return true if any datasets has been commited
         */
        public boolean commit()
        {
            ArrayList<DataSetStorageAlgorithm<T>> algorithms = createStorageAlgorithmsForDataSets();

            DataSetStorageAlgorithmRunner<T> runner =
                    new DataSetStorageAlgorithmRunner<T>(algorithms, parent, rollbackStack,
                            registrationService.getDssRegistrationLog(), openBisService,
                            registrationService, registrationService.getRegistratorContext()
                                    .getGlobalState());

            boolean storageAlgorithmsSucceeded = runner.prepareAndRunStorageAlgorithms();

            reactToSecondaryTransactionErrors(storageAlgorithmsSucceeded);

            return storageAlgorithmsSucceeded;
        }

        private void reactToSecondaryTransactionErrors(boolean storageAlgorithmsSucceeded)
        {
            // The queries are optional parts of the commit; catch any errors and inform the
            // invoker
            ArrayList<SecondaryTransactionFailure> encounteredErrors =
                    new ArrayList<SecondaryTransactionFailure>();
            for (DynamicTransactionQuery query : queriesToCommit.values())
            {
                try
                {
                    if (storageAlgorithmsSucceeded)
                    {
                        query.commit();
                    } else
                    {
                        query.rollback();
                    }
                    query.close(false);
                } catch (Throwable e)
                {
                    encounteredErrors.add(new SecondaryTransactionFailure(query, e));
                }
            }

            if (false == encounteredErrors.isEmpty())
            {
                parent.invokeDidEncounterSecondaryTransactionErrors(encounteredErrors);
            }
        }

        private ArrayList<DataSetStorageAlgorithm<T>> createStorageAlgorithmsForDataSets()
        {
            ArrayList<DataSetStorageAlgorithm<T>> algorithms =
                    new ArrayList<DataSetStorageAlgorithm<T>>(registeredDataSets.size());
            for (DataSet<? extends T> dataSet : registeredDataSets)
            {
                File contents = dataSet.tryDataSetContents();
                DataSetRegistrationDetails<? extends T> details = dataSet.getRegistrationDetails();

                // Decide how to create the storage algorithm depending on whether the
                // experiment/sample exist or not
                IExperimentImmutable experiment = dataSet.getExperiment();
                ISampleImmutable sample = dataSet.getSample();
                if (experimentsToBeRegistered.contains(experiment)
                        || samplesToBeRegistered.contains(sample))
                {
                    // Sample/Experiment exists
                    algorithms.add(registrationService
                            .createStorageAlgorithmWithIdentifiedStrategy(contents, details));
                } else
                {
                    // The experiment/sample does not yet exist
                    algorithms.add(registrationService.createStorageAlgorithm(contents, details));
                }
            }
            return algorithms;
        }

        /**
         * Rollback any commands that have been executed. Rollback is done in the reverse order of execution.
         */
        public void rollback()
        {
            rollbackStack.rollbackAll(new LiveTransactionRollbackDelegate(stagingDirectory));
            registeredDataSets.clear();
            for (DynamicTransactionQuery query : queriesToCommit.values())
            {
                query.rollback();
                query.close(false);
            }
        }

        /**
         * Execute the command and add it to the list of commands that have been executed.
         */
        private void executeCommand(ITransactionalCommand cmd)
        {
            rollbackStack.pushAndExecuteCommand(cmd);
        }

        /**
         * Generate a data set code for the registration details. Just calls openBisService to get a data set code by default.
         * 
         * @return A data set code
         */
        private String generateDataSetCode(
                DataSetRegistrationDetails<? extends T> registrationDetails)
        {
            return openBisService.createPermId();
        }

        AtomicEntityOperationDetails<T> createEntityOperationDetails(TechId registrationId,
                List<DataSetRegistrationInformation<T>> dataSetRegistrations)
        {
            for (DataSetRegistrationInformation<T> dataSetRegistrationInformation : dataSetRegistrations)
            {
                DataSetInformation dsInfo = dataSetRegistrationInformation.getDataSetInformation();
                if (dsInfo.isLinkSample() == false)
                {
                    // A storage processor might need the sample even though the data sets should
                    // not be linked to the sample because they are data sets of a container data
                    // set.
                    dsInfo.setSample(null);
                    dsInfo.setSampleCode(null);
                    dataSetRegistrationInformation.getExternalData()
                            .setSampleIdentifierOrNull(null);
                }
            }

            List<NewSpace> spaceRegistrations = convertSpacesToBeRegistered();
            List<NewProject> projectRegistrations = convertProjectsToBeRegistered();
            List<ProjectUpdatesDTO> projectUpdates = convertProjectsToBeUpdated();
            List<NewExperiment> experimentRegistrations = convertExperimentsToBeRegistered();
            List<ExperimentUpdatesDTO> experimentUpdates = convertExperimentsToBeUpdated();
            List<SampleUpdatesDTO> sampleUpdates = convertSamplesToBeUpdated();
            List<NewSample> sampleRegistrations = convertSamplesToBeRegistered();
            Map<String, List<NewMaterial>> materialRegistrations = convertMaterialsToBeRegistered();
            List<MaterialUpdateDTO> materialUpdates = convertMaterialsToBeUpdated();
            List<DataSetBatchUpdatesDTO> dataSetUpdates = convertDataSetsToBeUpdated();
            List<NewMetaproject> metaprojectRegistrations = convertMetaprojectsToBeRegistered();
            List<MetaprojectUpdatesDTO> metaprojectUpdates = convertMetaprojectsToBeUpdated();
            List<VocabularyUpdatesDTO> vocabularyUpdates = covertVocabulariesToBeUpdated();
            List<SpaceRoleAssignment> emptyAssignments = Collections.emptyList();

            AtomicEntityOperationDetails<T> registrationDetails =
                    new AtomicEntityOperationDetails<T>(registrationId, getUserId(),
                            spaceRegistrations, projectUpdates, projectRegistrations,
                            experimentUpdates, experimentRegistrations, sampleUpdates,
                            sampleRegistrations, materialRegistrations, materialUpdates,
                            dataSetRegistrations, dataSetUpdates, metaprojectRegistrations,
                            metaprojectUpdates, vocabularyUpdates, emptyAssignments, emptyAssignments);
            return registrationDetails;
        }

        private List<NewProject> convertProjectsToBeRegistered()
        {
            List<NewProject> result = new ArrayList<NewProject>();
            for (Project apiProject : projectsToBeRegistered)
            {
                result.add(ConversionUtils.convertToNewProject(apiProject));
            }
            return result;
        }

        private List<ProjectUpdatesDTO> convertProjectsToBeUpdated()
        {
            final List<ProjectUpdatesDTO> result =
                    new ArrayList<ProjectUpdatesDTO>(projectsToBeUpdated.size());
            for (Project apiProject : projectsToBeUpdated)
            {
                result.add(ConversionUtils.convertToProjectUpdateDTO(apiProject));
            }
            return result;
        }

        private List<NewSpace> convertSpacesToBeRegistered()
        {
            List<NewSpace> result = new ArrayList<NewSpace>();
            for (Space apiSpace : spacesToBeRegistered)
            {
                result.add(ConversionUtils.convertToNewSpace(apiSpace));
            }
            return result;
        }

        private List<NewExperiment> convertExperimentsToBeRegistered()
        {
            List<NewExperiment> result = new ArrayList<NewExperiment>();
            for (Experiment apiExperiment : experimentsToBeRegistered)
            {
                result.add(ConversionUtils.convertToNewExperiment(apiExperiment));
            }
            return result;
        }

        private List<ExperimentUpdatesDTO> convertExperimentsToBeUpdated()
        {
            List<ExperimentUpdatesDTO> result = new ArrayList<ExperimentUpdatesDTO>();
            for (ExperimentUpdatable experiment : experimentsToBeUpdated)
            {
                result.add(ConversionUtils.convertToExperimentUpdateDTO(experiment));
            }
            return result;
        }

        private List<NewSample> convertSamplesToBeRegistered()
        {
            List<NewSample> result = new ArrayList<NewSample>();
            for (Sample apiSample : samplesToBeRegistered)
            {
                result.add(ConversionUtils.convertToNewSample(apiSample));
            }
            return result;
        }

        private List<SampleUpdatesDTO> convertSamplesToBeUpdated()
        {
            List<SampleUpdatesDTO> result = new ArrayList<SampleUpdatesDTO>();
            for (Sample apiSample : samplesToBeUpdated)
            {
                result.add(ConversionUtils.convertToSampleUpdateDTO(apiSample));
            }
            return result;
        }

        private List<DataSetBatchUpdatesDTO> convertDataSetsToBeUpdated()
        {
            List<DataSetBatchUpdatesDTO> result = new ArrayList<DataSetBatchUpdatesDTO>();
            for (DataSetUpdatable dataSet : dataSetsToBeUpdated)
            {
                result.add(ConversionUtils.convertToDataSetBatchUpdatesDTO(dataSet));
            }
            return result;
        }

        private Map<String, List<NewMaterial>> convertMaterialsToBeRegistered()
        {
            Map<String, List<NewMaterial>> result = new HashMap<String, List<NewMaterial>>();
            for (Material material : materialsToBeRegistered)
            {
                NewMaterial converted = ConversionUtils.convertToNewMaterial(material);
                String materialType = material.getMaterialType();
                List<NewMaterial> materialsOfSameType = result.get(materialType);
                if (materialsOfSameType == null)
                {
                    materialsOfSameType = new ArrayList<NewMaterial>();
                    result.put(materialType, materialsOfSameType);
                }
                materialsOfSameType.add(converted);
            }
            return result;
        }

        private List<MaterialUpdateDTO> convertMaterialsToBeUpdated()
        {
            List<MaterialUpdateDTO> result = new ArrayList<MaterialUpdateDTO>();
            for (Material material : materialsToBeUpdated)
            {
                MaterialUpdateDTO converted = ConversionUtils.convertToMaterialUpdateDTO(material);
                result.add(converted);
            }
            return result;
        }

        private List<NewMetaproject> convertMetaprojectsToBeRegistered()
        {
            List<NewMetaproject> result = new ArrayList<NewMetaproject>();
            for (Metaproject apiMetaproject : metaprojectsToBeRegistered)
            {
                result.add(ConversionUtils.convertToNewMetaproject(apiMetaproject));
            }
            return result;
        }

        private List<MetaprojectUpdatesDTO> convertMetaprojectsToBeUpdated()
        {
            List<MetaprojectUpdatesDTO> result = new ArrayList<MetaprojectUpdatesDTO>();
            for (Metaproject apiMetaproject : metaprojectsToBeUpdated)
            {
                result.add(ConversionUtils.convertToMetaprojectUpdatesDTO(apiMetaproject));
            }
            return result;
        }

        private List<VocabularyUpdatesDTO> covertVocabulariesToBeUpdated()
        {
            List<VocabularyUpdatesDTO> result = new ArrayList<VocabularyUpdatesDTO>();
            for (Vocabulary v : vocabulariesToBeUpdated)
            {
                List<NewVocabularyTerm> newTerms = new LinkedList<NewVocabularyTerm>();

                for (IVocabularyTerm t : v.getNewTerms())
                {
                    NewVocabularyTerm newTerm =
                            new NewVocabularyTerm(t.getCode(), t.getDescription(), t.getLabel(),
                                    t.getOrdinal());
                    newTerms.add(newTerm);
                }

                VocabularyUpdatesDTO updates =
                        new VocabularyUpdatesDTO(v.getVocabulary().getId(), v.getCode(),
                                v.getDescription(), v.isManagedInternally(),
                                v.isChosenFromList(), v.getUrlTemplate(),
                                newTerms);

                result.add(updates);
            }
            return result;
        }

        @Override
        public boolean isCommitted()
        {
            return false;
        }

        @Override
        public boolean isRolledback()
        {
            return false;
        }
    }

    /**
     * Rollback stack delegate that checks whether the given filesystem is accessible before letting the rollback continue.
     */
    public static class LiveTransactionRollbackDelegate implements IRollbackStackDelegate
    {
        private final File stagingDirectory;

        private final int fileSystemAvailablityWaitCount;

        private final int fileSystemAvailablityPollingWaitTimeMs;

        /**
         * @param stagingDirectory Expects a staging directory
         */
        public LiveTransactionRollbackDelegate(File stagingDirectory)
        {
            this.stagingDirectory = stagingDirectory;
            this.fileSystemAvailablityWaitCount =
                    LiveTransactionState.fileSystemAvailablityWaitCount;
            this.fileSystemAvailablityPollingWaitTimeMs =
                    LiveTransactionState.fileSystemAvailablityPollingWaitTimeMs;
        }

        @Override
        public void willContinueRollbackAll(RollbackStack stack)
        {
            // Stop rolling back if the thread was interrupted
            InterruptedExceptionUnchecked.check();

            // Poll until the folder becomes accessible
            if (null != FileUtilities.checkDirectoryFullyAccessible(stagingDirectory, "staging"))
            {
                boolean keepPolling = true;
                for (int waitCount = 0; waitCount < fileSystemAvailablityWaitCount && keepPolling; ++waitCount)
                {
                    try
                    {
                        Thread.sleep(fileSystemAvailablityPollingWaitTimeMs);
                        // If the directory is not accessible (i.e., return not null), wait again
                        keepPolling =
                                (null != FileUtilities.checkDirectoryFullyAccessible(
                                        stagingDirectory, "staging"));
                    } catch (InterruptedException e)
                    {
                        throw new InterruptedExceptionUnchecked(e);
                    }
                }

                // The file never became available -- throw an exception
                if (null != FileUtilities
                        .checkDirectoryFullyAccessible(stagingDirectory, "staging"))
                {
                    throw new IOExceptionUnchecked("The staging directory "
                            + stagingDirectory.getAbsolutePath()
                            + " is not available. Could not rollback transaction.");
                }
            }
        }
    }

    private static abstract class TerminalTransactionState<T extends DataSetInformation> extends
            AbstractTransactionState<T>
    {
        private final LiveTransactionState<T> liveState;

        protected TerminalTransactionState(LiveTransactionState<T> liveState)
        {
            super(liveState.parent);
            this.liveState = liveState;
            deleteStagingFolders();
            this.liveState.rollbackStack.discard();
        }

        private void deleteStagingFolders()
        {
            for (DataSet<? extends T> dataSet : liveState.registeredDataSets)
            {
                dataSet.getDataSetStagingFolder().delete();
            }
        }

    }

    /**
     * State where the transaction has been committed.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class CommitedTransactionState<T extends DataSetInformation> extends
            TerminalTransactionState<T>
    {

        public CommitedTransactionState(LiveTransactionState<T> liveState)
        {
            super(liveState);
        }

        @Override
        public boolean isCommitted()
        {
            return true;
        }
    }

    static class RolledbackTransactionState<T extends DataSetInformation> extends
            TerminalTransactionState<T>
    {
        public RolledbackTransactionState(LiveTransactionState<T> liveState)
        {
            super(liveState);
        }

        @Override
        public boolean isRolledback()
        {
            return true;
        }
    }

}
