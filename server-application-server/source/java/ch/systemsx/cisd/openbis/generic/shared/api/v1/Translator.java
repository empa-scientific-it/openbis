/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1;

import static ch.systemsx.cisd.common.collection.CollectionUtils.nullSafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment.AttachmentInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType.ControlledVocabularyPropertyTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType.DataSetTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DeletedEntity;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment.ExperimentInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType.ExperimentTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material.MaterialInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType.PropertyTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup.PropertyTypeGroupInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType.SampleTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ValidationPluginInfo;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary.VocabularyInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.MaterialCodeAndTypeCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.MaterialTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.IProjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SamplePermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.AttachmentDownloadConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistrationAndModificationDate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSetUrl;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Franz-Josef Elmer
 */
public class Translator
{
    public static Role translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy role)
    {
        return translate(role.getRoleCode(), role.getRoleLevel().equals(RoleLevel.SPACE));
    }

    public static Role translate(RoleCode roleCode, boolean spaceLevel)
    {
        return new Role(roleCode.name(), spaceLevel);
    }

    public static List<Project> translateProjects(
            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project> projects)
    {
        ArrayList<Project> translated = new ArrayList<Project>();

        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project project : projects)
        {
            translated.add(translate(project));
        }

        return translated;
    }

    public static DataStore translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore dataStore)
    {
        return new DataStore(dataStore.getCode(), dataStore.getDownloadUrl(),
                dataStore.getHostUrl());
    }

    public static List<DataStore> translateDataStores(
            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore> dataStores)
    {
        ArrayList<DataStore> translated = new ArrayList<DataStore>();

        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore dataStore : dataStores)
        {
            translated.add(translate(dataStore));
        }

        return translated;
    }

    public static Project translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project project)
    {
        EntityRegistrationDetails registrationDetails = translateRegistrationDetails(project);
        return new Project(project.getId(), project.getPermId(), project.getSpace().getCode(),
                project.getCode(), project.getDescription(), registrationDetails);
    }

    public static List<Sample> translateSamples(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> privateSamples)
    {
        ArrayList<Sample> samples = new ArrayList<Sample>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample privateSample : privateSamples)
        {
            samples.add(Translator.translate(privateSample));
        }
        return samples;
    }

    public static Sample translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample privateSample)
    {
        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(privateSample.getId());
        initializer.setStub(privateSample.isStub());
        initializer.setSpaceCode(privateSample.getSpace() != null ? privateSample.getSpace()
                .getCode() : null);
        initializer.setPermId(privateSample.getPermId());
        initializer.setCode(privateSample.getCode());
        initializer.setIdentifier(privateSample.getIdentifier());
        if (privateSample.getSampleType() != null)
        {
            initializer.setSampleTypeId(privateSample.getSampleType().getId());
            initializer.setSampleTypeCode(privateSample.getSampleType().getCode());
        }
        List<IEntityProperty> properties = privateSample.getProperties();
        for (IEntityProperty prop : properties)
        {
            initializer.putProperty(prop.getPropertyType().getCode(), prop.tryGetAsString());
        }
        initializer.setRetrievedFetchOptions(EnumSet.of(SampleFetchOption.BASIC,
                SampleFetchOption.PROPERTIES, SampleFetchOption.METAPROJECTS));

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experimentOrNull =
                privateSample.getExperiment();
        if (null != experimentOrNull)
        {
            initializer.setExperimentIdentifierOrNull(experimentOrNull.getIdentifier());
        }

        EntityRegistrationDetails registrationDetails =
                translateRegistrationDetailsWithModificationDate(privateSample);
        initializer.setRegistrationDetails(registrationDetails);

        if (privateSample.getMetaprojects() != null)
        {
            for (Metaproject m : privateSample.getMetaprojects())
            {
                initializer.addMetaproject(m);
            }
        }
        return new Sample(initializer);
    }

    public static List<Experiment> translateExperiments(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment> privateExperiments)
    {
        ArrayList<Experiment> experiments = new ArrayList<Experiment>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment privateExpeiment : privateExperiments)
        {
            experiments.add(Translator.translate(privateExpeiment));
        }
        return experiments;
    }

    public static Experiment translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment privateExperiment)
    {
        ExperimentInitializer initializer = new ExperimentInitializer();
        initializer.setId(privateExperiment.getId());
        initializer.setPermId(privateExperiment.getPermId());
        initializer.setCode(privateExperiment.getCode());
        initializer.setIdentifier(privateExperiment.getIdentifier());
        initializer.setIsStub(privateExperiment.isStub());
        if (privateExperiment.getExperimentType() != null)
        {
            initializer.setExperimentTypeCode(privateExperiment.getExperimentType().getCode());
        }
        List<IEntityProperty> properties = privateExperiment.getProperties();
        for (IEntityProperty prop : properties)
        {
            initializer.putProperty(prop.getPropertyType().getCode(), prop.tryGetAsString());
        }

        EntityRegistrationDetails registrationDetails =
                translateRegistrationDetailsWithModificationDate(privateExperiment);
        initializer.setRegistrationDetails(registrationDetails);

        if (privateExperiment.getMetaprojects() != null)
        {
            for (Metaproject metaproject : privateExperiment.getMetaprojects())
            {
                initializer.addMetaproject(metaproject);
            }
        }

        return new Experiment(initializer);
    }

    public static DataSetType translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType dataSetType,
            HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms)
    {
        DataSetTypeInitializer initializer = new DataSetTypeInitializer();
        fillEntityTypeInitializerWithCommon(dataSetType, vocabTerms, initializer);
        initializer.setDeletionDisallowed(dataSetType.isDeletionDisallow());
        initializer.setMainDataSetPattern(dataSetType.getMainDataSetPattern());
        initializer.setMainDataSetPath(dataSetType.getMainDataSetPath());
        return new DataSetType(initializer);
    }

    private static DataSetKind translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind dataSetKind)
    {
        return dataSetKind == null ? null : DataSetKind.valueOf(dataSetKind.name());
    }

    public static SampleType translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType,
            HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms)
    {
        SampleTypeInitializer initializer = new SampleTypeInitializer();
        fillEntityTypeInitializerWithCommon(sampleType, vocabTerms, initializer);
        initializer.setListable(sampleType.isListable());
        initializer.setShowContainer(sampleType.isShowContainer());
        initializer.setShowParents(sampleType.isShowParents());
        initializer.setShowParentMetaData(sampleType.isShowParentMetadata());
        initializer.setUniqueSubcodes(sampleType.isSubcodeUnique());
        initializer.setAutomaticCodeGeneration(sampleType.isAutoGeneratedCode());
        initializer.setCodePrefix(sampleType.getGeneratedCodePrefix());
        return new SampleType(initializer);
    }

    public static ExperimentType translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType experimentType,
            HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms)
    {
        ExperimentTypeInitializer initializer = new ExperimentTypeInitializer();
        fillEntityTypeInitializerWithCommon(experimentType, vocabTerms, initializer);
        return new ExperimentType(initializer);
    }

    private static void fillEntityTypeInitializerWithCommon(
            EntityType entityType,
            HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms,
            EntityTypeInitializer initializer)
    {
        initializer.setCode(entityType.getCode());
        initializer.setDescription(entityType.getDescription());
        initializer.setValidationPluginInfo(translate(entityType.getValidationScript()));

        List<? extends EntityTypePropertyType<?>> etpts = entityType.getAssignedPropertyTypes();
        Collections.sort(etpts);

        String sectionName = null;
        PropertyTypeGroupInitializer groupInitializer = new PropertyTypeGroupInitializer();
        for (EntityTypePropertyType<?> etpt : etpts)
        {

            String thisSectionName = etpt.getSection();
            if (false == equals(sectionName, thisSectionName))
            {
                // Start a new section
                addGroup(initializer, groupInitializer);
                groupInitializer = new PropertyTypeGroupInitializer();
                sectionName = thisSectionName;
                groupInitializer.setName(sectionName);
            }
            PropertyTypeInitializer ptInitializer;
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType propertyType =
                    etpt.getPropertyType();

            boolean isControlledVocabulary =
                    propertyType.getDataType().getCode() == DataTypeCode.CONTROLLEDVOCABULARY;
            if (isControlledVocabulary)
            {
                ControlledVocabularyPropertyTypeInitializer cvptInitializer =
                        new ControlledVocabularyPropertyTypeInitializer();

                cvptInitializer.setVocabulary(propertyType.getVocabulary());
                cvptInitializer.setTerms(vocabTerms.get(propertyType.getVocabulary()));
                ptInitializer = cvptInitializer;
            } else
            {
                ptInitializer = new PropertyTypeInitializer();
            }

            ptInitializer.setDataType(propertyType.getDataType().getCode());
            if (propertyType.getSampleType() != null) {
                ptInitializer.setSampleTypeCode(propertyType.getSampleType().getCode());
            }
            if (propertyType.getMaterialType() != null) {
                ptInitializer.setMaterialTypeCode(propertyType.getMaterialType().getCode());
            }
            ptInitializer.setCode(propertyType.getCode());
            ptInitializer.setLabel(propertyType.getLabel());
            ptInitializer.setDescription(propertyType.getDescription());
            ptInitializer.setMandatory(etpt.isMandatory());

            ptInitializer.setManaged(etpt.isManaged());
            ptInitializer.setDinamic(etpt.isDynamic());
            ptInitializer.setShowInEditViews(etpt.isShownInEditView());

            if (isControlledVocabulary)
            {
                groupInitializer.addPropertyType(new ControlledVocabularyPropertyType(
                        (ControlledVocabularyPropertyTypeInitializer) ptInitializer));
            } else
            {
                groupInitializer.addPropertyType(new PropertyType(ptInitializer));
            }
        }
        // Finally set the group
        addGroup(initializer, groupInitializer);
    }

    private static void addGroup(EntityTypeInitializer initializer,
            PropertyTypeGroupInitializer groupInitializer)
    {
        if (groupInitializer.getPropertyTypes().isEmpty() == false)
        {
            initializer.addPropertyTypeGroup(new PropertyTypeGroup(groupInitializer));
        }
    }

    private static boolean equals(String sectionName, String currentSectionName)
    {
        return sectionName == null ? sectionName == currentSectionName : sectionName
                .equals(currentSectionName);
    }

    private static ValidationPluginInfo translate(Script script)
    {
        if (script == null)
        {
            return null;
        }
        return new ValidationPluginInfo(script.getName(), script.getDescription());
    }

    public static List<ControlledVocabularyPropertyType.VocabularyTerm> translatePropertyTypeTerms(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> privateTerms)
    {
        ArrayList<ControlledVocabularyPropertyType.VocabularyTerm> terms =
                new ArrayList<ControlledVocabularyPropertyType.VocabularyTerm>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm privateTerm : sortPrivateVocabularyTerms(privateTerms))
        {
            EntityRegistrationDetails registrationDetails =
                    translateRegistrationDetails(privateTerm);
            terms.add(new ControlledVocabularyPropertyType.VocabularyTerm(privateTerm.getCode(),
                    privateTerm.getCodeOrLabel(), privateTerm.getDescription(), privateTerm.getOrdinal(), privateTerm
                            .isOfficial(), registrationDetails));
        }

        return terms;
    }

    public static List<VocabularyTerm> translate(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> privateTerms)
    {
        ArrayList<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm privateTerm : sortPrivateVocabularyTerms(privateTerms))
        {
            EntityRegistrationDetails registrationDetails =
                    translateRegistrationDetails(privateTerm);
            terms.add(new VocabularyTerm(privateTerm.getCode(), privateTerm.getCodeOrLabel(), privateTerm.getDescription(),
                    privateTerm.getOrdinal(), privateTerm.isOfficial(), registrationDetails));
        }

        return terms;
    }

    private static ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> sortPrivateVocabularyTerms(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> privateTerms)
    {
        ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> sortedTerms =
                new ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm>(
                        privateTerms);
        Collections.sort(sortedTerms,
                new Comparator<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm>()
                    {
                        @Override
                        public int compare(
                                ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm o1,
                                ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm o2)
                        {
                            if (o1.isOfficial() != o2.isOfficial())
                            {
                                return o1.isOfficial() ? -1 : 1;
                            } else
                            {
                                return o1.getOrdinal().compareTo(o2.getOrdinal());
                            }
                        }
                    });
        return sortedTerms;
    }

    /**
     * Translates specified iterable collection of {@link AbstractExternalData} into a list of {@link DataSet} instance.
     * 
     * @param connectionsToGet Set of data set connections which should also be translated. This assumes that the {@link AbstractExternalData}
     *            instances are populated with these connections.
     */
    public static List<DataSet> translate(Iterable<AbstractExternalData> dataSets,
            EnumSet<Connections> connectionsToGet)
    {
        ArrayList<DataSet> translated = new ArrayList<DataSet>();
        Map<String, DataSet> alreadyTranslatedMap = new HashMap<String, DataSet>();

        for (AbstractExternalData dataSet : dataSets)
        {
            translated.add(translate(dataSet, connectionsToGet, alreadyTranslatedMap));
        }
        return translated;
    }

    /**
     * Translates the specified {@link AbstractExternalData} instance into a {@link DataSet} instance.
     * 
     * @param connectionsToGet Set of data set connections which should also be translated. This assumes that the {@link AbstractExternalData}
     *            instance is populated with these connections.
     */
    public static DataSet translate(AbstractExternalData externalDatum,
            EnumSet<Connections> connectionsToGet, Map<String, DataSet> alreadyTranslatedMap)
    {
        return translate(externalDatum, connectionsToGet, true, alreadyTranslatedMap);
    }

    /**
     * Translates the specified {@link AbstractExternalData} instance into a {@link DataSet} instance.
     * 
     * @param connectionsToGet Set of data set connections which should also be translated. This assumes that the {@link AbstractExternalData}
     *            instance is populated with these connections.
     * @param doRecurseIntoContainedDataSets If <code>true</code>, the translation will recurse into contained dataset, if <code>false</code>,
     *            contained datasets will not be set.
     */
    private static DataSet translate(AbstractExternalData externalDatum,
            EnumSet<Connections> connectionsToGet, boolean doRecurseIntoContainedDataSets, Map<String, DataSet> alreadyTranslatedMap)
    {
        String alreadyTranslatedKey = externalDatum.getCode() + " " + connectionsToGet + " " + doRecurseIntoContainedDataSets;
        DataSet alreadyTranslated = alreadyTranslatedMap.get(alreadyTranslatedKey);

        if (alreadyTranslated != null)
        {
            return alreadyTranslated;
        }

        DataSetInitializer initializer = new DataSetInitializer();
        initializer.setId(externalDatum.getId());
        initializer.setCode(externalDatum.getCode());
        if (externalDatum.getExperiment() != null)
        {
            initializer.setExperimentIdentifier(externalDatum.getExperiment().getIdentifier());
        }
        initializer.setSampleIdentifierOrNull(externalDatum.getSampleIdentifier());
        if (externalDatum.getDataSetType() != null)
        {
            initializer.setDataSetTypeCode(externalDatum.getDataSetType().getCode());
        }
        initializer.setStorageConfirmed(externalDatum.isStorageConfirmation());
        initializer.setStub(externalDatum.isStub());
        if (externalDatum.isContainer() == false)
        {
            initializer.setPostRegistered(externalDatum.isPostRegistered());
        }

        List<IEntityProperty> properties = externalDatum.getProperties();
        for (IEntityProperty prop : properties)
        {
            initializer.putProperty(prop.getPropertyType().getCode(), prop.tryGetAsString());
        }
        List<ContainerDataSet> containerDataSets = externalDatum.getContainerDataSets();
        List<DataSet> translatedContainerDataSets = new ArrayList<DataSet>();
        for (ContainerDataSet containerDataSet : containerDataSets)
        {
            translatedContainerDataSets.add(translate(containerDataSet, connectionsToGet, false, alreadyTranslatedMap));
        }
        initializer.setContainerDataSets(translatedContainerDataSets);

        initializer.setContainerDataSet(externalDatum.isContainer());
        if (externalDatum.isContainer() && doRecurseIntoContainedDataSets)
        {
            // Recursively translate any contained data sets
            ContainerDataSet containerDataSet = externalDatum.tryGetAsContainerDataSet();

            ArrayList<DataSet> containedDataSets =
                    new ArrayList<DataSet>(containerDataSet.getContainedDataSets().size());
            for (AbstractExternalData containedDataSet : containerDataSet.getContainedDataSets())
            {
                containedDataSets.add(translate(containedDataSet, connectionsToGet, true, alreadyTranslatedMap));
            }
            initializer.setContainedDataSets(containedDataSets);
        }
        initializer.setLinkDataSet(externalDatum.isLinkData());
        if (externalDatum.isLinkData())
        {
            LinkDataSet linkDataSet = externalDatum.tryGetAsLinkDataSet();
            initializer.setExternalDataSetCode(linkDataSet.getExternalCode());
            LinkDataSetUrl linkDataSetUrl = new LinkDataSetUrl(linkDataSet);
            initializer.setExternalDataSetLink(linkDataSetUrl.toString());
            initializer.setExternalDataManagementSystem(linkDataSet
                    .getExternalDataManagementSystem());
        }

        initializer.setRetrievedConnections(connectionsToGet);
        for (Connections connection : connectionsToGet)
        {
            switch (connection)
            {
                case PARENTS:
                    Collection<AbstractExternalData> parents = externalDatum.getParents();
                    ArrayList<String> parentCodes = new ArrayList<String>();
                    for (AbstractExternalData parentDatum : nullSafe(parents))
                    {
                        parentCodes.add(parentDatum.getCode());
                    }
                    initializer.setParentCodes(parentCodes);
                    break;
                case CHILDREN:
                    Collection<AbstractExternalData> children = externalDatum.getChildren();
                    ArrayList<String> childrenCodes = new ArrayList<String>();
                    for (AbstractExternalData parentDatum : nullSafe(children))
                    {
                        childrenCodes.add(parentDatum.getCode());
                    }
                    initializer.setChildrenCodes(childrenCodes);
                    break;
            }
        }
        EntityRegistrationDetails registrationDetails =
                translateRegistrationDetailsWithModificationDate(externalDatum);
        initializer.setRegistrationDetails(registrationDetails);

        if (externalDatum.getMetaprojects() != null)
        {
            for (Metaproject mp : externalDatum.getMetaprojects())
            {
                initializer.addMetaproject(mp);
            }
        }

        DataSet newlyTranslated = new DataSet(initializer);
        alreadyTranslatedMap.put(alreadyTranslatedKey, newlyTranslated);
        return newlyTranslated;
    }

    private static EntityRegistrationDetails translateRegistrationDetails(
            CodeWithRegistrationAndModificationDate<?> thingWithRegistrationDetails)
    {
        EntityRegistrationDetails.EntityRegistrationDetailsInitializer initializer =
                createInitializer(thingWithRegistrationDetails);
        return new EntityRegistrationDetails(initializer);
    }

    private static EntityRegistrationDetails translateRegistrationDetailsWithModificationDate(
            CodeWithRegistrationAndModificationDate<?> thingWithRegistrationDetails)
    {
        EntityRegistrationDetails.EntityRegistrationDetailsInitializer initializer =
                createInitializer(thingWithRegistrationDetails);
        initializer.setModificationDate(thingWithRegistrationDetails.getModificationDate());
        return new EntityRegistrationDetails(initializer);
    }

    private static EntityRegistrationDetails.EntityRegistrationDetailsInitializer createInitializer(
            CodeWithRegistrationAndModificationDate<?> thingWithRegistrationDetails)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person registrator = thingWithRegistrationDetails.getRegistrator();
        EntityRegistrationDetails.EntityRegistrationDetailsInitializer initializer =
                new EntityRegistrationDetails.EntityRegistrationDetailsInitializer();
        if (registrator != null)
        {
            initializer.setEmail(registrator.getEmail());
            initializer.setFirstName(registrator.getFirstName());
            initializer.setLastName(registrator.getLastName());
            initializer.setUserId(registrator.getUserId());
        }

        Person modifier = thingWithRegistrationDetails.getModifier();
        if (modifier != null)
        {
            initializer.setModifierEmail(modifier.getEmail());
            initializer.setModifierFirstName(modifier.getFirstName());
            initializer.setModifierLastName(modifier.getLastName());
            initializer.setModifierUserId(modifier.getUserId());
        }

        initializer.setModificationDate(thingWithRegistrationDetails.getModificationDate());
        initializer.setRegistrationDate(thingWithRegistrationDetails.getRegistrationDate());
        return initializer;
    }

    private Translator()
    {
    }

    public static Vocabulary translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary privateVocabulary)
    {
        VocabularyInitializer initializer = new VocabularyInitializer();
        initializer.setId(privateVocabulary.getId());
        initializer.setCode(privateVocabulary.getCode());
        initializer.setDescription(privateVocabulary.getDescription());
        initializer.setChosenFromList(privateVocabulary.isChosenFromList());
        initializer.setManagedInternally(privateVocabulary.isManagedInternally());
        initializer.setUrlTemplate(privateVocabulary.getURLTemplate());
        initializer.setTerms(translate(privateVocabulary.getTerms()));
        return new Vocabulary(initializer);
    }

    public static Material translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material,
            Map<Long, Material> materialsCache)
    {
        MaterialInitializer mi = new MaterialInitializer();

        mi.setId(material.getId());
        mi.setMaterialCode(material.getCode());

        MaterialTypeIdentifier typeIdentifier =
                new MaterialTypeIdentifier(material.getMaterialType().getCode());

        mi.setMaterialTypeIdentifier(typeIdentifier);

        material.getProperties();

        List<IEntityProperty> originalProperties = material.getProperties();
        Map<String, String> properties = EntityHelper.convertToStringMap(originalProperties);
        Map<String, Material> materialProperties =
                convertMaterialProperties(originalProperties, materialsCache);

        mi.setMaterialProperties(materialProperties);

        mi.setProperties(properties);

        mi.setRegistrationDetails(translateRegistrationDetails(material));

        if (material.getMetaprojects() != null)
        {
            for (Metaproject mp : material.getMetaprojects())
            {
                mi.addMetaproject(mp);
            }
        }

        return new Material(mi);
    }

    private static Map<String, Material> convertMaterialProperties(
            List<IEntityProperty> properties, Map<Long, Material> materialsCache)
    {
        HashMap<String, Material> result = new HashMap<String, Material>();
        if (properties != null)
        {
            for (IEntityProperty property : properties)
            {
                ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material =
                        property.getMaterial();
                if (material != null)
                {
                    Material apiMaterial = materialsCache.get(material.getId());
                    if (apiMaterial == null)
                    {
                        apiMaterial = translate(material, materialsCache);
                        // FIXME: Caching disabled because a not fully filled Material could be
                        // cached
                        // materialsCache.put(material.getId(), apiMaterial);
                    }
                    String propCode = property.getPropertyType().getCode();
                    result.put(propCode, apiMaterial);
                }
            }
        }
        return result;
    }

    public static List<Material> translateMaterials(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> materials)
    {
        Map<Long, Material> materialsCache = new HashMap<Long, Material>();
        List<Material> list = new LinkedList<Material>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material : materials)
        {
            list.add(translate(material, materialsCache));
        }
        return list;
    }

    public static List<Attachment> translateAttachments(String sessionToken,
            IObjectId attachmentHolderId, AttachmentHolderPE attachmentHolderPE,
            List<AttachmentPE> attachments, boolean allVersions)
    {
        Collections.sort(attachments, new Comparator<AttachmentPE>()
            {
                @Override
                public int compare(AttachmentPE a1, AttachmentPE a2)
                {
                    int fileNameCompare = a1.getFileName().compareTo(a2.getFileName());
                    // Newest version first.
                    return fileNameCompare == 0 ? a2.getVersion() - a1.getVersion()
                            : fileNameCompare;
                }
            });
        List<Attachment> result = new ArrayList<Attachment>();
        String lastFilenameSeen = null;
        for (AttachmentPE attachmentPE : attachments)
        {
            // The newest version will be first. If allVersions == false, skip all the older
            // versions.
            String fileName = attachmentPE.getFileName();
            if (allVersions == false && ObjectUtils.equals(lastFilenameSeen, fileName))
            {
                continue;
            }
            result.add(translate(sessionToken, attachmentHolderId, attachmentHolderPE, attachmentPE));
            lastFilenameSeen = fileName;
        }
        return result;
    }

    private static Attachment translate(String sessionToken, IObjectId attachmentHolderId,
            AttachmentHolderPE attachmentHolderPE, AttachmentPE attachment)
    {
        AttachmentInitializer initializer = new AttachmentInitializer();
        initializer.setAttachmentHolderId(attachmentHolderId);
        initializer.setFileName(attachment.getFileName());
        initializer.setVersion(attachment.getVersion());
        initializer.setTitle(attachment.getTitle());
        initializer.setDescription(attachment.getDescription());
        EntityRegistrationDetailsInitializer regDetailsInitializer =
                new EntityRegistrationDetailsInitializer();
        regDetailsInitializer.setRegistrationDate(attachment.getRegistrationDate());
        regDetailsInitializer.setUserId(attachment.getRegistrator().getUserId());
        regDetailsInitializer.setFirstName(attachment.getRegistrator().getFirstName());
        regDetailsInitializer.setLastName(attachment.getRegistrator().getLastName());
        regDetailsInitializer.setEmail(attachment.getRegistrator().getEmail());
        initializer.setRegistrationDetails(new EntityRegistrationDetails(regDetailsInitializer));
        URLMethodWithParameters url =
                new URLMethodWithParameters("/openbis/openbis/"
                        + AttachmentDownloadConstants.ATTACHMENT_DOWNLOAD_SERVLET_NAME);
        url.addParameter(GenericSharedConstants.SESSION_ID_PARAMETER, sessionToken);
        url.addParameter(AttachmentDownloadConstants.ATTACHMENT_HOLDER_PARAMETER,
                attachmentHolderPE.getAttachmentHolderKind().toString());
        if (attachmentHolderPE instanceof IIdHolder)
        {
            IIdHolder idHolder = (IIdHolder) attachmentHolderPE;
            url.addParameter(AttachmentDownloadConstants.TECH_ID_PARAMETER, idHolder.getId()
                    .toString());
        } else
        {
            url.addParameter(PermlinkUtilities.PERM_ID_PARAMETER_KEY,
                    attachmentHolderPE.getPermId());
        }
        url.addParameter(AttachmentDownloadConstants.FILE_NAME_PARAMETER, attachment.getFileName());
        url.addParameter(AttachmentDownloadConstants.VERSION_PARAMETER,
                Integer.toString(attachment.getVersion()));
        initializer.setDownloadLink(url.toString());
        return new Attachment(initializer);
    }

    public static PropertyType translate(ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType propertyType,
            HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms)
    {
        PropertyTypeInitializer ptInitializer;

        boolean isControlledVocabulary = propertyType.getDataType().getCode() == DataTypeCode.CONTROLLEDVOCABULARY;
        if (isControlledVocabulary)
        {
            ControlledVocabularyPropertyTypeInitializer cvptInitializer = new ControlledVocabularyPropertyTypeInitializer();

            cvptInitializer.setVocabulary(propertyType.getVocabulary());
            cvptInitializer.setTerms(vocabTerms.get(propertyType.getVocabulary()));
            ptInitializer = cvptInitializer;
        } else
        {
            ptInitializer = new PropertyTypeInitializer();
        }
        ptInitializer.setDataType(propertyType.getDataType().getCode());
        if (propertyType.getSampleType() != null) {
            ptInitializer.setSampleTypeCode(propertyType.getSampleType().getCode());
        }
        if (propertyType.getMaterialType() != null) {
            ptInitializer.setMaterialTypeCode(propertyType.getMaterialType().getCode());
        }
        ptInitializer.setCode(propertyType.getCode());
        ptInitializer.setLabel(propertyType.getLabel());
        ptInitializer.setDescription(propertyType.getDescription());

        if (isControlledVocabulary)
        {
            return new ControlledVocabularyPropertyType((ControlledVocabularyPropertyTypeInitializer) ptInitializer);
        } else
        {
            return new PropertyType(ptInitializer);
        }
    }

    public static Set<ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId> translate(Set<IObjectId> ids)
    {
        if (ids == null)
        {
            return null;
        }

        Set<ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId> result =
                new HashSet<ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId>();

        for (IObjectId id : ids)
        {
            result.add(translate(id));
        }

        return result;
    }

    public static ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId translate(IObjectId id)
    {
        if (id == null)
        {
            return null;
        }
        if (id instanceof IProjectId)
        {
            return translate((IProjectId) id);
        } else if (id instanceof IExperimentId)
        {
            return translate((IExperimentId) id);
        } else if (id instanceof ISampleId)
        {
            return translate((ISampleId) id);
        } else if (id instanceof IDataSetId)
        {
            return translate((IDataSetId) id);
        } else if (id instanceof IMaterialId)
        {
            return translate((IMaterialId) id);
        } else if (id instanceof IMetaprojectId)
        {
            return translate((IMetaprojectId) id);
        } else
        {
            throw new IllegalArgumentException("Unsupported object id: " + id.getClass());
        }
    }

    public static ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.project.IProjectId translate(IProjectId id)
    {
        if (id == null)
        {
            return null;
        }
        if (id instanceof ProjectTechIdId)
        {
            ProjectTechIdId techIdId = (ProjectTechIdId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.project.ProjectTechIdId(techIdId.getTechId());
        } else if (id instanceof ProjectIdentifierId)
        {
            ProjectIdentifierId identifierId = (ProjectIdentifierId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.project.ProjectIdentifierId(identifierId.getIdentifier());
        } else if (id instanceof ProjectPermIdId)
        {
            ProjectPermIdId permIdId = (ProjectPermIdId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.project.ProjectPermIdId(permIdId.getPermId());
        } else
        {
            throw new IllegalArgumentException("Unsupported project id: " + id.getClass());
        }
    }

    public static ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.IExperimentId translate(IExperimentId id)
    {
        if (id == null)
        {
            return null;
        }
        if (id instanceof ExperimentTechIdId)
        {
            ExperimentTechIdId techIdId = (ExperimentTechIdId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.ExperimentTechIdId(techIdId.getTechId());
        } else if (id instanceof ExperimentIdentifierId)
        {
            ExperimentIdentifierId identifierId = (ExperimentIdentifierId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.ExperimentIdentifierId(identifierId.getIdentifier());
        } else if (id instanceof ExperimentPermIdId)
        {
            ExperimentPermIdId permIdId = (ExperimentPermIdId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.ExperimentPermIdId(permIdId.getPermId());
        } else
        {
            throw new IllegalArgumentException("Unsupported experiment id: " + id.getClass());
        }
    }

    public static ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.ISampleId translate(ISampleId id)
    {
        if (id == null)
        {
            return null;
        }
        if (id instanceof SampleTechIdId)
        {
            SampleTechIdId techIdId = (SampleTechIdId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.SampleTechIdId(techIdId.getTechId());
        } else if (id instanceof SampleIdentifierId)
        {
            SampleIdentifierId identifierId = (SampleIdentifierId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.SampleIdentifierId(identifierId.getIdentifier());
        } else if (id instanceof SamplePermIdId)
        {
            SamplePermIdId permIdId = (SamplePermIdId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.SamplePermIdId(permIdId.getPermId());
        } else
        {
            throw new IllegalArgumentException("Unsupported sample id: " + id.getClass());
        }
    }

    public static ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.IDataSetId translate(IDataSetId id)
    {
        if (id == null)
        {
            return null;
        }
        if (id instanceof DataSetTechIdId)
        {
            DataSetTechIdId techIdId = (DataSetTechIdId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.DataSetTechIdId(techIdId.getTechId());
        } else if (id instanceof DataSetCodeId)
        {
            DataSetCodeId codeId = (DataSetCodeId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.DataSetCodeId(codeId.getCode());
        } else
        {
            throw new IllegalArgumentException("Unsupported data set id: " + id.getClass());
        }
    }

    public static ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.IMaterialId translate(IMaterialId id)
    {
        if (id == null)
        {
            return null;
        }
        if (id instanceof MaterialTechIdId)
        {
            MaterialTechIdId techIdId = (MaterialTechIdId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.MaterialTechIdId(techIdId.getTechId());
        } else if (id instanceof MaterialCodeAndTypeCodeId)
        {
            MaterialCodeAndTypeCodeId codeAndTypeCodeId = (MaterialCodeAndTypeCodeId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.MaterialCodeAndTypeCodeId(codeAndTypeCodeId.getCode(),
                    codeAndTypeCodeId.getTypeCode());
        } else
        {
            throw new IllegalArgumentException("Unsupported material id: " + id.getClass());
        }
    }

    public static ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.IMetaprojectId translate(IMetaprojectId id)
    {
        if (id == null)
        {
            return null;
        }
        if (id instanceof MetaprojectTechIdId)
        {
            MetaprojectTechIdId techIdId = (MetaprojectTechIdId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectTechIdId(techIdId.getTechId());
        } else if (id instanceof MetaprojectIdentifierId)
        {
            MetaprojectIdentifierId identifierId = (MetaprojectIdentifierId) id;
            return new ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectIdentifierId(identifierId.getIdentifier());
        } else
        {
            throw new IllegalArgumentException("Unsupported metaproject id: " + id.getClass());
        }
    }

    public static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsIds translate(MetaprojectAssignmentsIds assignments)
    {
        if (assignments == null)
        {
            return null;
        }

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsIds result =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsIds();

        for (IExperimentId experiment : assignments.getExperiments())
        {
            result.addExperiment(translate(experiment));
        }
        for (ISampleId sample : assignments.getSamples())
        {
            result.addSample(translate(sample));
        }
        for (IDataSetId dataSet : assignments.getDataSets())
        {
            result.addDataSet(translate(dataSet));
        }
        for (IMaterialId material : assignments.getMaterials())
        {
            result.addMaterial(translate(material));
        }

        return result;
    }

    public static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType translate(DeletionType deletionType)
    {
        switch (deletionType)
        {
            case PERMANENT:
                return ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType.PERMANENT;
            case TRASH:
                return ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType.TRASH;
            default:
                throw new IllegalArgumentException("Unknown deletion type: " + deletionType);
        }
    }

    public static List<Deletion> translate(List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion> deletions)
    {
        List<Deletion> result = new LinkedList<Deletion>();

        if (deletions != null)
        {
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion deletion : deletions)
            {
                result.add(translate(deletion));
            }
        }

        return result;
    }

    public static Deletion translate(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion deletion)
    {
        Deletion result = new Deletion();
        result.setId(deletion.getId());
        result.setReasonOrNull(deletion.getReason());
        result.setTotalExperimentsCount(deletion.getTotalExperimentsCount());
        result.setTotalDatasetsCount(deletion.getTotalDatasetsCount());
        result.setTotalSamplesCount(deletion.getTotalSamplesCount());

        List<DeletedEntity> entities = new LinkedList<DeletedEntity>();
        for (IEntityInformationHolderWithIdentifier entity : deletion.getDeletedEntities())
        {
            entities.add(translate((ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedEntity) entity));
        }
        result.setDeletedEntities(entities);

        return result;
    }

    public static DeletedEntity translate(ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedEntity entity)
    {
        DeletedEntity result = new DeletedEntity();
        result.setId(entity.getId());
        result.setCode(entity.getCode());
        result.setPermId(entity.getPermId());
        result.setIdentifier(entity.getIdentifier());
        result.setEntityType(entity.getEntityType().getCode());
        result.setEntityKind(translate(entity.getEntityKind()));
        return result;
    }

    public static EntityKind translate(ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind kind)
    {
        switch (kind)
        {
            case EXPERIMENT:
                return EntityKind.EXPERIMENT;
            case SAMPLE:
                return EntityKind.SAMPLE;
            case DATA_SET:
                return EntityKind.DATA_SET;
            case MATERIAL:
                return EntityKind.MATERIAL;
            default:
                throw new IllegalArgumentException("Unknown entity kind: " + kind);
        }
    }
}
