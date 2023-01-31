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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IMetaprojectImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.Translator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewLinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Utility methods to convert transaction-layer DTOs to objects accepted by openBIS.
 * 
 * @author Kaloyan Enimanev
 */
public class ConversionUtils
{

    public static NewExperiment convertToNewExperiment(Experiment apiExperiment)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment =
                apiExperiment.getExperiment();
        NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(experiment.getIdentifier());
        newExperiment.setPermID(experiment.getPermId());
        if (experiment.getExperimentType() != null)
        {
            newExperiment.setExperimentTypeCode(experiment.getExperimentType().getCode());
        }
        IEntityProperty[] properties = experiment.getProperties().toArray(new IEntityProperty[0]);
        newExperiment.setProperties(properties);
        newExperiment.setAttachments(apiExperiment.getNewAttachments());

        return newExperiment;
    }

    public static ExperimentUpdatesDTO convertToExperimentUpdateDTO(
            ExperimentUpdatable apiExperiment)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment =
                apiExperiment.getExperiment();

        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();

        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment.getId()));
        updates.setAttachments(Collections.<NewAttachment> emptySet());
        updates.setProjectIdentifier(new ProjectIdentifierFactory(experiment.getProject()
                .getIdentifier()).createIdentifier());

        updates.setProperties(experiment.getProperties());
        updates.setAttachments(apiExperiment.getNewAttachments());

        return updates;
    }

    public static NewSample convertToNewSample(Sample apiSample)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample = apiSample.getSample();
        NewSample newSample = new NewSample();
        newSample.setIdentifier(sample.getIdentifier());
        newSample.setPermID(sample.getPermId());
        if (sample.getExperiment() != null)
        {
            newSample.setExperimentIdentifier(sample.getExperiment().getIdentifier());
        }
        if (sample.getContainer() != null)
        {
            newSample.setContainerIdentifier(sample.getContainer().getIdentifier());
        }

        Set<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> parentsSet =
                sample.getParents();
        if (parentsSet != null && parentsSet.size() > 0)
        {
            String[] parentIdentifiers = new String[parentsSet.size()];
            int i = 0;
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample parent : parentsSet)
            {
                parentIdentifiers[i++] = parent.getIdentifier();
            }
            newSample.setParentsOrNull(parentIdentifiers);
        }
        newSample.setSampleType(sample.getSampleType());

        final IEntityProperty[] properties = sample.getProperties().toArray(new IEntityProperty[0]);
        newSample.setProperties(properties);
        newSample.setAttachments(apiSample.getNewAttachments());

        return newSample;
    }

    public static SampleUpdatesDTO convertToSampleUpdateDTO(Sample apiSample)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample = apiSample.getSample();
        Set<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> sampleParents =
                sample.getParents();
        String[] parentIdentifiers;
        if (apiSample.getUpdateDetails().isParentsUpdateRequested())
        {
            parentIdentifiers = new String[sampleParents.size()];
            int i = 0;
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample parent : sampleParents)
            {
                parentIdentifiers[i++] = parent.getIdentifier();
            }
        } else
        {
            parentIdentifiers = null;
        }

        final List<NewAttachment> attachments = apiSample.getNewAttachments();
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(sample.getIdentifier());
        String containerIdentifier =
                (sample.getContainer() != null) ? sample.getContainer().getIdentifier() : null;
        SampleUpdatesDTO sampleUpdate =
                new SampleUpdatesDTO(TechId.create(sample), // db id
                        sample.getProperties(), // List<IEntityProperty>
                        ExperimentIdentifierFactory.tryGetExperimentIdentifier(sample), // ExperimentIdentifier
                        ProjectIdentifierFactory.tryGetProjectIdentifier(sample),
                        attachments, // Collection<NewAttachment>
                        sample.getVersion(), // Sample version
                        sampleIdentifier, // Sample Identifier
                        containerIdentifier, // Container Identifier
                        parentIdentifiers // Parent Identifiers
                );
        return sampleUpdate;
    }

    public static NewExternalData convertToNewExternalData(
            DataSetRegistrationDetails<?> registrationDetails, String dataStoreCode,
            StorageFormat storageFormat, String dataFileRelativePath)
    {
        DataSetInformation dataSetInformation = registrationDetails.getDataSetInformation();
        final NewExternalData data;
        if (dataSetInformation.isContainerDataSet())
        {
            data = new NewContainerDataSet();
            ((NewContainerDataSet) data).setContainedDataSetCodes(dataSetInformation
                    .getContainedDataSetCodes());
            if (null != dataFileRelativePath)
            {
                throw new IllegalArgumentException(
                        "A data set can contain files or other data sets, but not both. The data set specification is invalid: "
                                + dataSetInformation);
            }
        } else if (dataSetInformation.isLinkDataSet())
        {
            // TODO: ?
            throw new NotImplementedException();
        } else
        {
            data = new NewDataSet();
            data.setSpeedHint(dataSetInformation.getSpeedHint());
            final BooleanOrUnknown isCompleteFlag = dataSetInformation.getIsCompleteFlag();
            data.setComplete(isCompleteFlag);
            data.setLocatorType(registrationDetails.getLocatorType());
            data.setShareId(dataSetInformation.getShareId());
            data.setLocation(dataFileRelativePath.substring(data.getShareId().length() + 1));
            data.setFileFormatType(registrationDetails.getFileFormatType());
        }
        data.setUserId(dataSetInformation.getUploadingUserIdOrNull());
        data.setUserEMail(dataSetInformation.tryGetUploadingUserEmail());
        data.setExtractableData(dataSetInformation.getExtractableData());
        data.setDataSetType(registrationDetails.getDataSetType());
        data.setDataSetKind(registrationDetails.getDataSetKind());
        data.setMeasured(registrationDetails.isMeasuredData());
        data.setDataStoreCode(dataStoreCode);
        data.setExperimentIdentifierOrNull(dataSetInformation.getExperimentIdentifier());
        data.setSampleIdentifierOrNull(dataSetInformation.getSampleIdentifier());
        data.setSamplePermIdOrNull(getSamplePermIdOrNull(dataSetInformation));
        data.setParentDataSetCodes(dataSetInformation.getParentDataSetCodes());

        data.setStorageFormat(storageFormat);

        List<NewProperty> newProperties =
                dataSetInformation.getExtractableData().getDataSetProperties();
        data.getExtractableData().setDataSetProperties(newProperties);

        return data;
    }

    private static String getSamplePermIdOrNull(DataSetInformation dataSetInformation)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample = dataSetInformation.tryToGetSample();
        return sample == null ? null : sample.getPermId();
    }

    public static NewLinkDataSet convertToNewLinkDataSet(
            DataSetRegistrationDetails<?> registrationDetails, String dataStoreCode)
    {
        DataSetInformation dataSetInformation = registrationDetails.getDataSetInformation();

        NewLinkDataSet data = new NewLinkDataSet();
        data.setExternalDataManagementSystemCode(dataSetInformation
                .getExternalDataManagementSystem());
        data.setExternalCode(dataSetInformation.getExternalCode());

        data.setUserId(dataSetInformation.getUploadingUserIdOrNull());
        data.setUserEMail(dataSetInformation.tryGetUploadingUserEmail());
        data.setExtractableData(dataSetInformation.getExtractableData());
        data.setDataSetType(registrationDetails.getDataSetType());
        data.setDataSetKind(registrationDetails.getDataSetKind());
        data.setMeasured(registrationDetails.isMeasuredData());
        data.setDataStoreCode(dataStoreCode);
        data.setExperimentIdentifierOrNull(dataSetInformation.getExperimentIdentifier());
        data.setSampleIdentifierOrNull(dataSetInformation.getSampleIdentifier());
        data.setParentDataSetCodes(dataSetInformation.getParentDataSetCodes());

        List<NewProperty> newProperties =
                dataSetInformation.getExtractableData().getDataSetProperties();
        data.getExtractableData().setDataSetProperties(newProperties);

        return data;
    }

    public static NewContainerDataSet convertToNewContainerDataSet(
            DataSetRegistrationDetails<?> registrationDetails, String dataStoreCode)
    {
        DataSetInformation dataSetInformation = registrationDetails.getDataSetInformation();

        NewContainerDataSet data = new NewContainerDataSet();
        data.setContainedDataSetCodes(dataSetInformation.getContainedDataSetCodes());
        data.setUserId(dataSetInformation.getUploadingUserIdOrNull());
        data.setUserEMail(dataSetInformation.tryGetUploadingUserEmail());
        data.setExtractableData(dataSetInformation.getExtractableData());
        data.setDataSetType(registrationDetails.getDataSetType());
        data.setDataSetKind(registrationDetails.getDataSetKind());
        data.setMeasured(registrationDetails.isMeasuredData());
        data.setDataStoreCode(dataStoreCode);
        data.setExperimentIdentifierOrNull(dataSetInformation.getExperimentIdentifier());
        data.setSampleIdentifierOrNull(dataSetInformation.getSampleIdentifier());
        data.setParentDataSetCodes(dataSetInformation.getParentDataSetCodes());

        List<NewProperty> newProperties =
                dataSetInformation.getExtractableData().getDataSetProperties();
        data.getExtractableData().setDataSetProperties(newProperties);

        return data;
    }

    public static NewSpace convertToNewSpace(Space apiSpace)
    {
        return new NewSpace(apiSpace.getSpaceCode(), apiSpace.getDescription(),
                apiSpace.getSpaceAdminUserId());
    }

    public static NewProject convertToNewProject(Project apiProject)
    {
        final NewProject newProject =
                new NewProject(apiProject.getProjectIdentifier(), apiProject.getDescription());
        newProject.setAttachments(apiProject.getNewAttachments());
        return newProject;
    }

    public static ProjectUpdatesDTO convertToProjectUpdateDTO(Project apiProject)
    {
        final ProjectUpdatesDTO projectUpdate = new ProjectUpdatesDTO();
        projectUpdate.setIdentifier(apiProject.getProjectIdentifier());
        projectUpdate.setTechId(new TechId(apiProject.getId()));
        projectUpdate.setDescription(apiProject.getDescription());
        projectUpdate.setAttachments(apiProject.getNewAttachments());
        projectUpdate.setVersion(apiProject.getProject().getVersion());
        return projectUpdate;
    }

    public static DataSetUpdatesDTO convertToDataSetUpdatesDTO(DataSetUpdatable dataSet)
    {
        AbstractExternalData externalData = dataSet.getExternalData();

        DataSetUpdatesDTO dataSetUpdate = new DataSetUpdatesDTO();
        dataSetUpdate.setDatasetId(new TechId(externalData));
        dataSetUpdate.setVersion(externalData.getVersion());
        dataSetUpdate.setFileFormatTypeCode(dataSet.getFileFormatType());
        dataSetUpdate.setProperties(externalData.getProperties());

        if (externalData.getExperiment() != null)
        {
            String identifierString = externalData.getExperiment().getIdentifier();
            ExperimentIdentifier experimentIdentifier =
                    ExperimentIdentifierFactory.parse(identifierString);
            dataSetUpdate.setExperimentIdentifierOrNull(experimentIdentifier);
        }

        if (externalData.getSample() != null)
        {
            String identifierString = externalData.getSampleIdentifier();
            SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(identifierString);
            dataSetUpdate.setSampleIdentifierOrNull(sampleIdentifier);
        }

        if (externalData.isContainer())
        {
            ContainerDataSet container = externalData.tryGetAsContainerDataSet();
            String[] containedCodes = Code.extractCodesToArray(container.getContainedDataSets());
            dataSetUpdate.setModifiedContainedDatasetCodesOrNull(containedCodes);
        }

        if (externalData.isLinkData())
        {
            LinkDataSet link = externalData.tryGetAsLinkDataSet();
            dataSetUpdate.setExternalCode(link.getExternalCode());
            dataSetUpdate.setExternalDataManagementSystemCode(link
                    .getExternalDataManagementSystem().getCode());
        }

        return dataSetUpdate;
    }

    public static DataSetBatchUpdatesDTO convertToDataSetBatchUpdatesDTO(DataSetUpdatable dataSet)
    {
        return enrichUpdatesWithInformation(dataSet, dataSet.getUpdates());
    }

    private static DataSetBatchUpdatesDTO enrichUpdatesWithInformation(DataSetUpdatable dataSet,
            DataSetBatchUpdatesDTO dataSetUpdate)
    {
        AbstractExternalData externalData = dataSet.getExternalData();

        dataSetUpdate.setDatasetId(new TechId(externalData));
        dataSetUpdate.setVersion(externalData.getVersion());
        dataSetUpdate.setFileFormatTypeCode(dataSet.getFileFormatType());
        dataSetUpdate.setProperties(externalData.getProperties());

        if (externalData.getExperiment() != null)
        {
            String identifierString = externalData.getExperiment().getIdentifier();
            ExperimentIdentifier experimentIdentifier =
                    ExperimentIdentifierFactory.parse(identifierString);
            dataSetUpdate.setExperimentIdentifierOrNull(experimentIdentifier);
        }

        if (externalData.getSample() != null)
        {
            String identifierString = externalData.getSampleIdentifier();
            SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(identifierString);
            dataSetUpdate.setSampleIdentifierOrNull(sampleIdentifier);
        }

        if (externalData.isContainer())
        {
            ContainerDataSet container = externalData.tryGetAsContainerDataSet();
            String[] containedCodes = Code.extractCodesToArray(container.getContainedDataSets());
            dataSetUpdate.setModifiedContainedDatasetCodesOrNull(containedCodes);
        }

        if (externalData.isLinkData())
        {
            dataSetUpdate.setExternalCode(dataSet.getExternalCode());
            dataSetUpdate.setExternalDataManagementSystemCode(dataSet
                    .getExternalDataManagementSystem().getCode());
        }

        String[] parentCodes = Code.extractCodesToArray(externalData.getParents());
        dataSetUpdate.setModifiedParentDatasetCodesOrNull(parentCodes);

        return dataSetUpdate;
    }

    public static NewMaterial convertToNewMaterial(Material material)
    {
        NewMaterial newMaterial = new NewMaterial(material.getCode());
        IEntityProperty[] properties =
                material.getMaterial().getProperties().toArray(new IEntityProperty[0]);
        newMaterial.setProperties(properties);
        return newMaterial;
    }

    public static MaterialUpdateDTO convertToMaterialUpdateDTO(Material material)
    {
        return new MaterialUpdateDTO(new TechId(material.getMaterial().getId()), material
                .getMaterial().getProperties(), material.getMaterial().getModificationDate());
    }

    public static NewMetaproject convertToNewMetaproject(Metaproject metaproject)
    {
        NewMetaproject newMetaproject =
                new NewMetaproject(metaproject.getName(), metaproject.getDescription(),
                        metaproject.getOwnerId());
        newMetaproject.setEntities(Translator.translate(metaproject.getAddedEntities()));
        return newMetaproject;
    }

    public static MetaprojectUpdatesDTO convertToMetaprojectUpdatesDTO(Metaproject metaproject)
    {
        MetaprojectUpdatesDTO update = new MetaprojectUpdatesDTO();
        update.setMetaprojectId(new TechId(metaproject.getId()));
        update.setDescription(metaproject.getDescription());
        update.setAddedEntities(Translator.translate(metaproject.getAddedEntities()));
        update.setRemovedEntities(Translator.translate(metaproject.getRemovedEntities()));
        return update;
    }

    static NewAttachment createAttachment(String filePath, String title, String description,
            byte[] content)
    {
        final NewAttachment newAttachment = new NewAttachment();
        newAttachment.setFilePath(filePath);
        newAttachment.setTitle(title);
        newAttachment.setDescription(description);
        newAttachment.setContent(content);
        return newAttachment;
    }

    public static List<IMetaprojectImmutable> convertToMetaprojectsImmutable(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject> metaprojects)
    {
        Collection<IMetaprojectImmutable> converted =
                CollectionUtils
                        .collect(
                                metaprojects,
                                new Transformer<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject, IMetaprojectImmutable>()
                                    {
                                        @Override
                                        public IMetaprojectImmutable transform(
                                                ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject input)
                                        {
                                            return new MetaprojectImmutable(input);
                                        }
                                    });
        return new LinkedList<IMetaprojectImmutable>(converted);
    }
}
