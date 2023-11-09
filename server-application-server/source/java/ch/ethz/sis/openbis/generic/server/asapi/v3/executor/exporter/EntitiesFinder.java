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

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
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
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportablePermId;

public class EntitiesFinder
{

    public static Collection<ICodeHolder> getEntities(final IApplicationServerApi api, final String sessionToken,
            final Collection<ExportablePermId> permIds)
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
                    return Stream.of(getSampleTypes(api, sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case EXPERIMENT_TYPE:
                {
                    return Stream.of(getExperimentTypes(api, sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case DATASET_TYPE:
                {
                    return Stream.of(getDataSetTypes(api, sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case VOCABULARY_TYPE:
                {
                    return Stream.of(getVocabularies(api, sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case SPACE:
                {
                    return Stream.of(getSpaces(api, sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case PROJECT:
                {
                    return Stream.of(getProjects(api, sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case SAMPLE:
                {
                    return Stream.of(getSamples(api, sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case EXPERIMENT:
                {
                    return Stream.of(getExperiments(api, sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                case DATASET:
                {
                    return Stream.of(getDataSets(api, sessionToken, stringPermIds)).map(value -> (ICodeHolder) value);
                }
                default:
                {
                    throw new IllegalArgumentException();
                }
            }
        }).collect(Collectors.toList());
    }

    private static Collection<DataSetType> getDataSetTypes(final IApplicationServerApi api, final String sessionToken, final Collection<String> permIds)
    {
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

    private static Collection<DataSet> getDataSets(final IApplicationServerApi api, final String sessionToken, final Collection<String> permIds)
    {
        final List<DataSetPermId> dataSetPermIds = permIds.stream().map(DataSetPermId::new)
                .collect(Collectors.toList());
        final DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withSample();
        fetchOptions.withExperiment();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        fetchOptions.withPhysicalData();
        fetchOptions.withParents();
        fetchOptions.withChildren();
        return api.getDataSets(sessionToken, dataSetPermIds, fetchOptions).values();
    }

    private static Collection<Experiment> getExperiments(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<ExperimentPermId> experimentPermIds = permIds.stream().map(ExperimentPermId::new)
                .collect(Collectors.toList());
        final ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        return api.getExperiments(sessionToken, experimentPermIds, fetchOptions).values();
    }

    private static Collection<ExperimentType> getExperimentTypes(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
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

    private static Collection<Project> getProjects(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<ProjectPermId> projectPermIds = permIds.stream().map(ProjectPermId::new)
                .collect(Collectors.toList());
        final ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        return api.getProjects(sessionToken, projectPermIds, fetchOptions).values();
    }

    private static Collection<Sample> getSamples(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<SamplePermId> samplePermIds = permIds.stream().map(SamplePermId::new)
                .collect(Collectors.toList());
        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        fetchOptions.withExperiment();
        fetchOptions.withParents();
        fetchOptions.withChildren();
        fetchOptions.withType().withPropertyAssignments().withPropertyType();
        fetchOptions.withProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        return api.getSamples(sessionToken, samplePermIds, fetchOptions).values();
    }

    private static Collection<SampleType> getSampleTypes(final IApplicationServerApi api, final String sessionToken, final Collection<String> permIds)
    {
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

    private static Collection<Space> getSpaces(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<SpacePermId> spacePermIds = permIds.stream().map(SpacePermId::new).collect(Collectors.toList());
        final SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        fetchOptions.withRegistrator();
        return api.getSpaces(sessionToken, spacePermIds, fetchOptions).values();
    }

    private static Collection<Vocabulary> getVocabularies(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms();
        fetchOptions.withRegistrator();
        final Map<IVocabularyId, Vocabulary> vocabularies = api.getVocabularies(sessionToken,
                permIds.stream().map(VocabularyPermId::new).collect(Collectors.toList()), fetchOptions);

        assert vocabularies.size() <= 1;

        return vocabularies.values();
    }

}
