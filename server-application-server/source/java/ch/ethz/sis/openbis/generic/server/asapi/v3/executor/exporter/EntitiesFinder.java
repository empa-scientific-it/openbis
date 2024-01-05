/*
 * Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportablePermId;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

class EntitiesFinder
{

    public static Collection<ICodeHolder> getEntities(final String sessionToken, final Collection<ExportablePermId> permIds)
    {
        final Map<ExportableKind, List<ExportablePermId>> groupedExportables =
                permIds.stream().collect(Collectors.groupingBy(ExportablePermId::getExportableKind));

        return groupedExportables.entrySet().stream().flatMap(entry ->
        {
            final Collection<String> stringPermIds = entry.getValue().stream().map(permId -> permId.getPermId().getPermId())
                    .collect(Collectors.toList());
            switch (entry.getKey())
            {
                case SAMPLE_TYPE:
                {
                    return Stream.of(getSampleTypes(sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case EXPERIMENT_TYPE:
                {
                    return Stream.of(getExperimentTypes(sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case DATASET_TYPE:
                {
                    return Stream.of(getDataSetTypes(sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case VOCABULARY_TYPE:
                {
                    return Stream.of(getVocabularies(sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case SPACE:
                {
                    return Stream.of(getSpaces(sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case PROJECT:
                {
                    return Stream.of(getProjects(sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case SAMPLE:
                {
                    return Stream.of(getSamples(sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case EXPERIMENT:
                {
                    return Stream.of(getExperiments(sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case DATASET:
                {
                    return Stream.of(getDataSets(sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                default:
                {
                    throw new IllegalArgumentException();
                }
            }
        }).collect(Collectors.toList());
    }

    public static Collection<DataSetType> getDataSetTypes(final String sessionToken, final Collection<String> permIds)
    {
        final IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        final DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPropertyType().withSampleType();
        propertyAssignmentFetchOptions.withPropertyType().withMaterialType();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        final Map<IEntityTypeId, DataSetType> dataSetTypes = api.getDataSetTypes(sessionToken,
                permIds.stream().map(permId -> new EntityTypePermId(permId, EntityKind.DATA_SET)).collect(Collectors.toList()), fetchOptions);

        assert dataSetTypes.size() <= 1;

        return dataSetTypes.values();
    }

    public static Collection<DataSet> getDataSets(final String sessionToken, final Collection<String> permIds)
    {
        final IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        final List<DataSetPermId> dataSetPermIds = permIds.stream().map(DataSetPermId::new)
                .collect(Collectors.toList());
        final DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        final SampleFetchOptions sampleFetchOptions = fetchOptions.withSample();
        sampleFetchOptions.withSpace();
        sampleFetchOptions.withProject().withSpace();
        sampleFetchOptions.withContainer();
        sampleFetchOptions.withProperties();

        final ExperimentFetchOptions sampleExperimentFetchOptions = sampleFetchOptions.withExperiment();
        sampleExperimentFetchOptions.withProject().withSpace();
        sampleExperimentFetchOptions.withProperties();

        fetchOptions.withExperiment().withProject().withSpace();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        fetchOptions.withPhysicalData();
        fetchOptions.withParents().withProperties();
        fetchOptions.withChildren().withProperties();
        return api.getDataSets(sessionToken, dataSetPermIds, fetchOptions).values();
    }

    public static Collection<Experiment> getExperiments(final String sessionToken, final Collection<String> permIds)
    {
        final IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        final List<ExperimentPermId> experimentPermIds = permIds.stream().map(ExperimentPermId::new)
                .collect(Collectors.toList());
        final ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        final ProjectFetchOptions projectFetchOptions = fetchOptions.withProject();
        projectFetchOptions.withSpace();
        projectFetchOptions.withRegistrator();
        projectFetchOptions.withModifier();

        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        fetchOptions.withDataSets().withType();
        return api.getExperiments(sessionToken, experimentPermIds, fetchOptions).values();
    }

    public static Collection<ExperimentType> getExperimentTypes(final String sessionToken, final Collection<String> permIds)
    {
        final IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        final ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPropertyType().withSampleType();
        propertyAssignmentFetchOptions.withPropertyType().withMaterialType();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        final Map<IEntityTypeId, ExperimentType> experimentTypes = api.getExperimentTypes(sessionToken,
                permIds.stream().map(permId -> new EntityTypePermId(permId, EntityKind.EXPERIMENT)).collect(Collectors.toList()), fetchOptions);

        assert experimentTypes.size() <= 1;

        return experimentTypes.values();
    }

    public static Collection<Project> getProjects(final String sessionToken, final Collection<String> permIds)
    {
        final IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        final List<ProjectPermId> projectPermIds = permIds.stream().map(ProjectPermId::new)
                .collect(Collectors.toList());
        final ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        return api.getProjects(sessionToken, projectPermIds, fetchOptions).values();
    }

    public static Collection<Sample> getSamples(final String sessionToken, final Collection<String> permIds)
    {
        final IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        final List<SamplePermId> samplePermIds = permIds.stream().map(SamplePermId::new)
                .collect(Collectors.toList());
        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        final ExperimentFetchOptions experimentFetchOptions = fetchOptions.withExperiment();
        experimentFetchOptions.withProperties();
        experimentFetchOptions.withProject().withSpace();
        fetchOptions.withSpace();
        fetchOptions.withProject().withSpace();
        fetchOptions.withParents().withProperties();
        fetchOptions.withChildren().withProperties();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        fetchOptions.withDataSets().withType();
        fetchOptions.withContainer();
        return api.getSamples(sessionToken, samplePermIds, fetchOptions).values();
    }

    public static Collection<SampleType> getSampleTypes(final String sessionToken, final Collection<String> permIds)
    {
        final IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        final SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPropertyType().withSampleType();
        propertyAssignmentFetchOptions.withPropertyType().withMaterialType();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        final Map<IEntityTypeId, SampleType> sampleTypes = api.getSampleTypes(sessionToken,
                permIds.stream().map(permId -> new EntityTypePermId(permId, EntityKind.SAMPLE)).collect(Collectors.toList()), fetchOptions);

        assert sampleTypes.size() <= 1;

        return sampleTypes.values();
    }

    public static Collection<Space> getSpaces(final String sessionToken, final Collection<String> permIds)
    {
        final IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        final List<SpacePermId> spacePermIds = permIds.stream().map(SpacePermId::new).collect(Collectors.toList());
        final SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        fetchOptions.withRegistrator();
        return api.getSpaces(sessionToken, spacePermIds, fetchOptions).values();
    }

    public static Collection<Vocabulary> getVocabularies(final String sessionToken, final Collection<String> permIds)
    {
        final IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        final VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms();
        fetchOptions.withRegistrator();
        final Map<IVocabularyId, Vocabulary> vocabularies = api.getVocabularies(sessionToken,
                permIds.stream().map(VocabularyPermId::new).collect(Collectors.toList()), fetchOptions);

        assert vocabularies.size() <= 1;

        return vocabularies.values();
    }

}
