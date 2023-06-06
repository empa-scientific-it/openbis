/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileSystemContentCopy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IContentCopy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UrlContentCopy;
import ch.systemsx.cisd.openbis.generic.shared.dto.ContentCopyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetTranslator
{
    private DataSetTranslator()
    {
    }

    public static DatasetDescription translateToDescription(AbstractExternalData data)
    {
        DatasetDescription description = new DatasetDescription();
        description.setDataSetCode(data.getCode());

        description.setDataStoreCode(data.getDataStore().getCode());
        description.setRegistrationTimestamp(data.getRegistrationDate());
        List<ContainerDataSet> containerDataSets = data.getContainerDataSets();
        for (ContainerDataSet containerDataSet : containerDataSets)
        {
            String containerDataSetCode = containerDataSet.getCode();
            description.addOrderInContainer(containerDataSetCode, data.getOrderInContainer(containerDataSetCode));
        }

        PhysicalDataSet dataSet = data.tryGetAsDataSet();
        if (dataSet != null)
        {
            description.setDataSetLocation(dataSet.getLocation());
            description.setSpeedHint(dataSet.getSpeedHint());
            description.setFileFormatType(dataSet.getFileFormatType().getCode());
            description.setH5Folders(dataSet.isH5Folders());
            description.setH5arFolders(dataSet.isH5arFolders());
            description.setArchivingRequested(dataSet.isArchivingRequested());
        }
        description.setDataSetSize(data.getSize());
        DataSetType dataSetType = data.getDataSetType();
        if (dataSetType != null)
        {
            description.setDatasetTypeCode(dataSetType.getCode());
        }
        Experiment experiment = data.getExperiment();
        if (experiment != null)
        {
            description.setExperimentCode(experiment.getCode());
            description.setExperimentIdentifier(experiment.getIdentifier());
            Project project = experiment.getProject();
            if (project != null)
            {
                description.setProjectCode(project.getCode());
                Space space = project.getSpace();
                if (space != null)
                {
                    description.setSpaceCode(space.getCode());
                }
            }
            ExperimentType experimentType = experiment.getExperimentType();
            if (experimentType != null)
            {
                description.setExperimentTypeCode(experimentType.getCode());
            }
        }
        Sample sample = data.getSample();
        if (sample != null)
        {
            description.setSampleCode(sample.getCode());
            description.setSampleIdentifier(sample.getIdentifier());
            SampleType sampleType = sample.getSampleType();
            if (sampleType != null)
            {
                description.setSampleTypeCode(sampleType.getCode());
            }
            Project project = sample.getProject();
            if (project != null)
            {
                description.setProjectCode(project.getCode());
            }
            Space space = sample.getSpace();
            if (space != null)
            {
                description.setSpaceCode(space.getCode());
            }
        }
        return description;
    }

    public static List<AbstractExternalData> translate(List<? extends DataPE> list,
            String defaultDataStoreBaseURL, String baseIndexURL,
            Map<Long, Set<Metaproject>> metaprojects,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        ArrayList<AbstractExternalData> result = new ArrayList<AbstractExternalData>(list.size());
        for (DataPE dataPE : list)
        {
            AbstractExternalData data =
                    translate(dataPE, baseIndexURL, true, metaprojects.get(dataPE.getId()),
                            managedPropertyEvaluatorFactory, samplePropertyAccessValidator,
                            ExperimentTranslator.LoadableFields.PROPERTIES);
            result.add(data);
        }
        return result;
    }

    public static AbstractExternalData translateWithoutRevealingData(AbstractExternalData data)
    {
        AbstractExternalData externalData = null;

        if (data.isContainer())
        {
            externalData = new ContainerDataSet(true);
        } else if (data.isLinkData())
        {
            externalData = new LinkDataSet(true);
        } else
        {
            externalData = new PhysicalDataSet(true);
        }

        externalData.setId(data.getId());
        externalData.setCode(data.getCode());
        externalData.setDataSetProperties(new ArrayList<IEntityProperty>());

        return externalData;
    }

    public static AbstractExternalData translateWithoutRevealingData(DataPE dataPE)
    {
        AbstractExternalData externalData = null;
        if (dataPE.isContainer())
        {
            externalData = new ContainerDataSet(true);
        } else if (dataPE.isLinkData())
        {
            externalData = new LinkDataSet(true);
        } else if (dataPE instanceof ExternalDataPE)
        {
            externalData = new PhysicalDataSet(true);
        } else
        {
            throw new IllegalArgumentException("Data set " + dataPE.getCode()
                    + " is neither a container nor a real data set.");
        }

        externalData.setId(HibernateUtils.getId(dataPE));
        externalData.setCode(dataPE.getCode());
        externalData.setDataSetProperties(new ArrayList<IEntityProperty>());

        return externalData;
    }

    public static AbstractExternalData translate(DataPE dataPE, String baseIndexURL,
            Collection<Metaproject> metaprojects,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator,
            final LoadableFields... withExperimentFields)
    {
        return translate(dataPE, baseIndexURL, true, metaprojects, managedPropertyEvaluatorFactory,
                samplePropertyAccessValidator, withExperimentFields);
    }

    public static AbstractExternalData translate(DataPE dataPE, String baseIndexURL,
            boolean withDetails, Collection<Metaproject> metaprojects,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator,
            final LoadableFields... withExperimentFields)
    {
        AbstractExternalData externalData = null;
        if (dataPE.isContainer())
        {
            externalData =
                    translateContainerDataSetProperties(dataPE, baseIndexURL, withDetails,
                            managedPropertyEvaluatorFactory, samplePropertyAccessValidator);
        } else if (dataPE.isLinkData())
        {
            externalData = translateLinkDataSetProperties(dataPE);
        } else if (dataPE instanceof ExternalDataPE)
        {
            externalData = translateDataSetProperties((ExternalDataPE) dataPE);
        } else
        {
            throw new IllegalArgumentException("Data set " + dataPE.getCode()
                    + " is neither a container nor a real data set.");
        }

        SamplePE sampleOrNull = dataPE.tryGetSample();
        ExperimentPE experiment = dataPE.getExperiment();
        Experiment translatedExperiment = experiment == null ? null : ExperimentTranslator.translate(experiment, baseIndexURL, null,
                managedPropertyEvaluatorFactory, samplePropertyAccessValidator, withExperimentFields);
        externalData.setId(HibernateUtils.getId(dataPE));
        externalData.setCode(dataPE.getCode());
        externalData.setDataProducerCode(dataPE.getDataProducerCode());
        externalData.setDataSetType(DataSetTypeTranslator.translate(dataPE.getDataSetType(),
                new HashMap<MaterialTypePE, MaterialType>(), new HashMap<PropertyTypePE, PropertyType>()));
        externalData.setDerived(dataPE.isDerived());
        addContainers(externalData, dataPE, baseIndexURL, managedPropertyEvaluatorFactory, samplePropertyAccessValidator);
        final Collection<AbstractExternalData> parents = new HashSet<AbstractExternalData>();
        externalData.setParents(parents);
        for (DataPE parentPE : dataPE.getParents())
        {
            parents.add(translateBasicProperties(parentPE));
        }
        setChildren(dataPE, externalData);
        externalData.setProductionDate(dataPE.getProductionDate());
        externalData.setModificationDate(dataPE.getModificationDate());
        externalData.setVersion(dataPE.getVersion());
        externalData.setRegistrator(PersonTranslator.translate(dataPE.getRegistrator()));
        externalData.setModifier(PersonTranslator.translate(dataPE.getModifier()));
        externalData.setRegistrationDate(dataPE.getRegistrationDate());
        externalData.setSample(sampleOrNull == null ? null : fillSample(new Sample(), sampleOrNull,
                translatedExperiment, withDetails, managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
        externalData.setDataStore(DataStoreTranslator.translate(dataPE.getDataStore()));
        externalData.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL,
                EntityKind.DATA_SET, externalData.getIdentifier()));
        setProperties(dataPE, externalData, managedPropertyEvaluatorFactory, samplePropertyAccessValidator);
        externalData.setExperiment(translatedExperiment);

        if (dataPE.isContainer() == false)
        {
            externalData.setPostRegistered(dataPE.getPostRegistration().size() == 0);
        }

        if (metaprojects != null)
        {
            externalData.setMetaprojects(metaprojects);
        }
        externalData.setDeletion(DeletionTranslator.translate(dataPE.getDeletion()));
        externalData.setMetaData(dataPE.getMetaData());
        return externalData;
    }

    private static void addContainers(AbstractExternalData externalData, DataPE dataPE, String baseIndexURL,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        List<DataSetRelationshipPE> containerComponentRelationships =
                RelationshipUtils.getContainerComponentRelationships(dataPE.getParentRelationships());
        for (DataSetRelationshipPE relationship : containerComponentRelationships)
        {
            ContainerDataSet container = tryToTranslateContainer(relationship.getParentDataSet(), baseIndexURL,
                    managedPropertyEvaluatorFactory, samplePropertyAccessValidator);
            externalData.addContainer(container, relationship.getOrdinal());
        }
    }

    private static ContainerDataSet tryToTranslateContainer(DataPE containerOrNull,
            String baseIndexURL, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        return containerOrNull != null ? (ContainerDataSet) translate(containerOrNull,
                baseIndexURL, false, null, managedPropertyEvaluatorFactory, samplePropertyAccessValidator) : null;
    }

    private static AbstractExternalData translateContainerDataSetProperties(DataPE dataPE,
            String baseIndexURL, boolean withComponents,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        ContainerDataSet containerDataSet = new ContainerDataSet();
        if (withComponents)
        {
            setContainedDataSets(dataPE, containerDataSet, baseIndexURL,
                    managedPropertyEvaluatorFactory, samplePropertyAccessValidator);
        }
        return containerDataSet;
    }

    private static LinkDataSet translateLinkDataSetProperties(DataPE dataPE)
    {
        LinkDataSet linkDataSet = new LinkDataSet();
        LinkDataPE linkDataPE = dataPE.tryAsLinkData();

        if (linkDataPE.getContentCopies().size() > 0)
        {
            ContentCopyPE copy = linkDataPE.getContentCopies().iterator().next();
            ExternalDataManagementSystemPE edms = copy.getExternalDataManagementSystem();
            linkDataSet.setExternalDataManagementSystem(ExternalDataManagementSystemTranslator
                    .translate(edms));
            linkDataSet.setExternalCode(copy.getExternalCode());
        }

        List<ContentCopyPE> pes = new ArrayList<>(linkDataPE.getContentCopies());
        Collections.sort(pes, new Comparator<ContentCopyPE>()
            {
                @Override
                public int compare(ContentCopyPE copy1, ContentCopyPE copy2)
                {
                    return copy1.getId().compareTo(copy2.getId());
                }
            });

        List<IContentCopy> translatedCopies = new ArrayList<>();
        for (ContentCopyPE copy : pes)
        {
            ExternalDataManagementSystemPE edms = copy.getExternalDataManagementSystem();
            String address = edms.getAddress();
            ExternalDataManagementSystemType type = edms.getAddressType();
            IContentCopy translatedCopy;
            if (ExternalDataManagementSystemType.FILE_SYSTEM.equals(type))
            {
                String[] split = address.split(":");
                translatedCopy =
                        new FileSystemContentCopy(edms.getCode(), edms.getLabel(), split[0], split[1], copy.getPath(), copy.getGitCommitHash(),
                                copy.getGitRepositoryId());
            } else
            {
                translatedCopy = new UrlContentCopy(edms.getCode(), edms.getLabel(),
                        address.replaceAll(Pattern.quote("${") + ".*" + Pattern.quote("}"), copy.getExternalCode()), copy.getExternalCode());
            }
            translatedCopies.add(translatedCopy);
        }

        linkDataSet.setCopies(translatedCopies);

        return linkDataSet;
    }

    private static AbstractExternalData translateDataSetProperties(ExternalDataPE externalDataPE)
    {
        PhysicalDataSet dataSet = new PhysicalDataSet();
        dataSet.setSize(externalDataPE.getSize());
        dataSet.setComplete(BooleanOrUnknown.tryToResolve(externalDataPE.getComplete()));
        dataSet.setStatus(externalDataPE.getStatus());
        dataSet.setPresentInArchive(externalDataPE.isPresentInArchive());
        dataSet.setStorageConfirmation(externalDataPE.isStorageConfirmation());
        dataSet.setSpeedHint(externalDataPE.getSpeedHint());
        dataSet.setFileFormatType(TypeTranslator.translate(externalDataPE.getFileFormatType()));
        dataSet.setLocation(externalDataPE.getLocation());
        dataSet.setAccessTimestamp(externalDataPE.getAccessDate());
        dataSet.setShareId(externalDataPE.getShareId());
        dataSet.setLocatorType(TypeTranslator.translate(externalDataPE.getLocatorType()));
        dataSet.setH5Folders(externalDataPE.isH5Folders());
        dataSet.setH5arFolders(externalDataPE.isH5arFolders());
        dataSet.setArchivingRequested(externalDataPE.isArchivingRequested());
        dataSet.setMetaData(externalDataPE.getMetaData());
        return dataSet;
    }

    private static void setProperties(DataPE dataPE, AbstractExternalData externalData,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        if (HibernateUtils.isInitialized(dataPE.getProperties()))
        {
            externalData.setDataSetProperties(EntityPropertyTranslator.translate(
                    dataPE.getProperties(), new HashMap<MaterialTypePE, MaterialType>(),
                    new HashMap<PropertyTypePE, PropertyType>(),
                    managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
        } else
        {
            externalData.setDataSetProperties(new ArrayList<IEntityProperty>());
        }
    }

    private static Sample fillSample(Sample sample, SamplePE samplePE, Experiment experiment,
            boolean loadSampleProperties,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        sample.setId(HibernateUtils.getId(samplePE));
        sample.setPermId(samplePE.getPermId());
        SampleTranslator.setCodes(sample, samplePE);
        sample.setSampleType(TypeTranslator.translate(samplePE.getSampleType()));
        sample.setIdentifier(samplePE.getSampleIdentifier().toString());
        sample.setRegistrationDate(samplePE.getRegistrationDate());
        sample.setRegistrator(PersonTranslator.translate(samplePE.getRegistrator()));
        sample.setModifier(PersonTranslator.translate(samplePE.getModifier()));
        sample.setSpace(SpaceTranslator.translate(samplePE.getSpace()));
        sample.setProject(ProjectTranslator.translate(samplePE.getProject()));
        sample.setExperiment(experiment);
        sample.setMetaData(samplePE.getMetaData());
        if (loadSampleProperties)
        {
            sample.setProperties(EntityPropertyTranslator.translate(samplePE.getProperties(),
                    new HashMap<MaterialTypePE, MaterialType>(), new HashMap<PropertyTypePE, PropertyType>(),
                    managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
        }
        return sample;
    }

    private static void setChildren(DataPE dataPE, AbstractExternalData externalData)
    {
        List<AbstractExternalData> children = new ArrayList<AbstractExternalData>();
        if (HibernateUtils.isInitialized(dataPE.getChildRelationships()))
        {
            for (DataPE childPE : dataPE.getChildren())
            {
                children.add(translateBasicProperties(childPE));
            }
        }
        externalData.setChildren(children);
    }

    private static void setContainedDataSets(DataPE dataPE, ContainerDataSet containerDataSet,
            String baseIndexURL, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        List<AbstractExternalData> containedDataSets = new ArrayList<AbstractExternalData>();
        if (HibernateUtils.isInitialized(dataPE.getContainedDataSets()))
        {
            for (DataPE childPE : dataPE.getContainedDataSets())
            {
                containedDataSets.add(translate(childPE, baseIndexURL, null,
                        managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
            }
        }
        containerDataSet.setContainedDataSets(containedDataSets);
    }

    /**
     * Creates an <var>externalData</var> from <var>dataPE</vra> an fills it with all data needed by {@link IEntityInformationHolder}.
     */
    public static AbstractExternalData translateBasicProperties(DataPE dataPE)
    {
        AbstractExternalData result = null;
        if (dataPE.isContainer())
        {
            result = new ContainerDataSet();
        } else if (dataPE.isExternalData())
        {
            result = new PhysicalDataSet();
        } else if (dataPE.isLinkData())
        {
            result = new LinkDataSet();
        }
        result.setId(HibernateUtils.getId(dataPE));
        result.setCode(dataPE.getCode());
        result.setDataSetType(DataSetTypeTranslator.translate(dataPE.getDataSetType(),
                new HashMap<MaterialTypePE, MaterialType>(), new HashMap<PropertyTypePE, PropertyType>()));
        return result;
    }

    public static List<DatasetDescription> translateToDescriptions(List<? extends DataPE> datasets)
    {
        List<DatasetDescription> result = new ArrayList<DatasetDescription>();
        for (DataPE dataset : datasets)
        {
            result.add(translateToDescription(dataset));
        }
        return result;
    }

    public static DatasetDescription translateToDescription(DataPE dataSet)
    {
        assert dataSet != null;

        DatasetDescription description = new DatasetDescription();
        description.setDataSetCode(dataSet.getCode());
        List<DataSetRelationshipPE> containerComponentRelationships =
                RelationshipUtils.getContainerComponentRelationships(dataSet.getParentRelationships());
        for (DataSetRelationshipPE relationship : containerComponentRelationships)
        {
            String containerDataSetCode = relationship.getParentDataSet().getCode();
            description.addOrderInContainer(containerDataSetCode, relationship.getOrdinal());
        }
        description.setRegistrationTimestamp(dataSet.getRegistrationDate());
        if (dataSet.isExternalData())
        {
            ExternalDataPE externalData = dataSet.tryAsExternalData();
            description.setDataSetLocation(externalData.getLocation());
            description.setDataSetSize(externalData.getSize());
            description.setSpeedHint(externalData.getSpeedHint());
            description.setFileFormatType(externalData.getFileFormatType().getCode());
            description.setStorageConfirmed(externalData.isStorageConfirmation());
            description.setH5Folders(externalData.isH5Folders());
            description.setH5arFolders(externalData.isH5arFolders());
        }
        SamplePE sample = dataSet.tryGetSample();
        if (sample != null)
        {
            description.setSampleCode(sample.getCode());
            description.setSampleIdentifier(sample.getIdentifier());
            description.setSampleTypeCode(sample.getSampleType().getCode());
            ProjectPE project = sample.getProject();
            if (project != null)
            {
                description.setProjectCode(project.getCode());
            }
            description.setSpaceCode(sample.getSpace().getCode());
        }
        ExperimentPE experiment = dataSet.getExperiment();
        if (experiment != null)
        {
            description.setExperimentIdentifier(experiment.getIdentifier());
            description.setExperimentTypeCode(experiment.getExperimentType().getCode());
            description.setExperimentCode(experiment.getCode());
            ProjectPE project = experiment.getProject();
            description.setProjectCode(project.getCode());
            SpacePE space = project.getSpace();
            description.setSpaceCode(space.getCode());
        }
        DataSetTypePE dataSetType = dataSet.getDataSetType();
        description.setMainDataSetPath(dataSetType.getMainDataSetPath());
        description.setMainDataSetPattern(dataSetType.getMainDataSetPattern());
        description.setDatasetTypeCode(dataSetType.getCode());
        description.setDataStoreCode(dataSet.getDataStore().getCode());

        return description;
    }
}
