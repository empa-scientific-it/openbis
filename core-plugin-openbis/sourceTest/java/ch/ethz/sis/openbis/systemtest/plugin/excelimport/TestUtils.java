/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.CustomASServiceCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;

public class TestUtils
{
    private static final String TEST_XLS = "TEST-XLS";

    private static final String XLS_NAME = "xls_name";

    private static final String UPDATE_MODE = "update_mode";

    private static final String XLS_PARAM = "xls";

    public static final String CSV_PARAM = "csv";

    private static final String SCRIPTS_PARAM = "scripts";

    private static final String XLS_IMPORT_API = "xls-import-api";

    static Vocabulary getVocabulary(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        VocabularySearchCriteria criteria = new VocabularySearchCriteria();
        criteria.withId().thatEquals(new VocabularyPermId(code));
        VocabularyFetchOptions fo = new VocabularyFetchOptions();
        fo.withTerms();

        SearchResult<Vocabulary> result = v3api.searchVocabularies(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static List<Vocabulary> getAllVocabularies(IApplicationServerInternalApi v3api, String sessionToken)
    {
        VocabularySearchCriteria criteria = new VocabularySearchCriteria();
        VocabularyFetchOptions fo = new VocabularyFetchOptions();
        fo.withTerms();

        SearchResult<Vocabulary> result = v3api.searchVocabularies(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects();
        } else
        {
            return null;
        }
    }

    static SampleType getSampleType(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
        criteria.withCode().thatEquals(code);

        SampleTypeFetchOptions fo = new SampleTypeFetchOptions();
        fo.withValidationPlugin().withScript();
        PropertyAssignmentFetchOptions propCriteria = fo.withPropertyAssignments();
        propCriteria.withPlugin().withScript();
        propCriteria.withPropertyType().withVocabulary();

        SearchResult<SampleType> result = v3api.searchSampleTypes(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static ExperimentType getExperimentType(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        ExperimentTypeSearchCriteria criteria = new ExperimentTypeSearchCriteria();
        criteria.withCode().thatEquals(code);

        ExperimentTypeFetchOptions fo = new ExperimentTypeFetchOptions();
        fo.withValidationPlugin().withScript();
        PropertyAssignmentFetchOptions propCriteria = fo.withPropertyAssignments();
        propCriteria.withPlugin().withScript();
        propCriteria.withPropertyType().withVocabulary();

        SearchResult<ExperimentType> result = v3api.searchExperimentTypes(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static DataSetType getDatasetType(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        DataSetTypeSearchCriteria criteria = new DataSetTypeSearchCriteria();
        criteria.withCode().thatEquals(code);

        DataSetTypeFetchOptions fo = new DataSetTypeFetchOptions();
        fo.withValidationPlugin().withScript();
        PropertyAssignmentFetchOptions propCriteria = fo.withPropertyAssignments();
        propCriteria.withPlugin().withScript();
        propCriteria.withPropertyType().withVocabulary();

        SearchResult<DataSetType> result = v3api.searchDataSetTypes(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static PropertyType getPropertyType(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        PropertyTypeSearchCriteria criteria = new PropertyTypeSearchCriteria();
        criteria.withCode().thatEquals(code);

        PropertyTypeFetchOptions fo = new PropertyTypeFetchOptions();
        fo.withVocabulary();

        SearchResult<PropertyType> result = v3api.searchPropertyTypes(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static Space getSpace(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withCode().thatEquals(code);

        SpaceFetchOptions fo = new SpaceFetchOptions();

        SearchResult<Space> result = v3api.searchSpaces(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static Project getProject(IApplicationServerInternalApi v3api, String sessionToken, String code)
    {
        return getProject(v3api, sessionToken, code, null);
    }

    static Project getProject(IApplicationServerInternalApi v3api, String sessionToken, String code, String spaceId)
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withCode().thatEquals(code);

        if (spaceId != null)
        {
            criteria.withSpace().withCode().thatEquals(spaceId);
        }

        ProjectFetchOptions fo = new ProjectFetchOptions();
        fo.withSpace();

        SearchResult<Project> result = v3api.searchProjects(sessionToken, criteria, fo);

        if (result.getObjects().size() > 0)
        {
            return result.getObjects().get(0);
        } else
        {
            return null;
        }
    }

    static Experiment getExperiment(IApplicationServerInternalApi v3api, String sessionToken, String experimentCode, String projectCode,
            String spaceCode)
    {
        List<IExperimentId> ids = new ArrayList<>();
        ids.add(new ExperimentIdentifier(spaceCode, projectCode, experimentCode));

        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withProject();
        fo.withProperties();
        fo.withType();

        List<Experiment> result = v3api.getExperiments(sessionToken, ids, fo).values().stream().collect(Collectors.toList());

        if (result.size() > 0)
        {
            return result.get(0);
        } else
        {
            return null;
        }
    }

    static Sample getSample(IApplicationServerInternalApi v3api, String sessionToken, String sampleCode, String spaceCode)
    {
        List<ISampleId> ids = new ArrayList<>();
        ids.add(new SampleIdentifier(spaceCode, null, null, sampleCode));

        return getSamples(v3api, sessionToken, ids);
    }

    static Sample getSampleById(IApplicationServerInternalApi v3api, String sessionToken, String id)
    {
        List<ISampleId> ids = new ArrayList<>();
        if (id.contains("/"))
        {
            ids.add(new SampleIdentifier(id));
        } else
        {
            ids.add(new SamplePermId(id));
        }
        return getSamples(v3api, sessionToken, ids);
    }

    public static List<Sample> getSamplesById(IApplicationServerInternalApi v3api, String sessionToken, List<ISampleId> ids)
    {
        SampleFetchOptions fo = new SampleFetchOptions();
        SampleFetchOptions childrenFo = fo.withChildren();
        childrenFo.withSpace();
        childrenFo.withExperiment();
        SampleFetchOptions parentsFo = fo.withParents();
        parentsFo.withSpace();
        parentsFo.withExperiment();
        fo.withExperiment();
        fo.withProject();
        fo.withProperties();
        fo.withSpace();
        fo.withType();

        List<Sample> result = v3api.getSamples(sessionToken, ids, fo).values().stream().collect(Collectors.toList());

        return result;
    }

    private static Sample getSamples(IApplicationServerInternalApi v3api, String sessionToken, List<ISampleId> ids)
    {
        SampleFetchOptions fo = new SampleFetchOptions();
        SampleFetchOptions childrenFo = fo.withChildren();
        childrenFo.withSpace();
        childrenFo.withExperiment();
        SampleFetchOptions parentsFo = fo.withParents();
        parentsFo.withSpace();
        parentsFo.withExperiment();
        fo.withExperiment();
        fo.withProject();
        fo.withProperties();
        fo.withSpace();
        fo.withType();

        List<Sample> result = v3api.getSamples(sessionToken, ids, fo).values().stream().collect(Collectors.toList());

        if (result.size() > 0)
        {
            return result.get(0);
        } else
        {
            return null;
        }
    }

    static String createFrom(IApplicationServerInternalApi v3api, String sessionToken, Path... xls_paths) throws IOException
    {
        return createFrom(v3api, sessionToken, UpdateMode.IGNORE_EXISTING, xls_paths).toString();
    }

    static List<IObjectId> createFrom(IApplicationServerInternalApi v3api, String sessionToken, UpdateMode updateMode, Path... xls_paths)
            throws IOException
    {
        List<byte[]> excels = new ArrayList<>();
        for (Path xls_path : xls_paths)
        {
            byte[] xls = readData(xls_path);
            excels.add(xls);
        }
        CustomASServiceExecutionOptions options = new CustomASServiceExecutionOptions();
        options.withParameter(XLS_PARAM, excels);
        options.withParameter(UPDATE_MODE, updateMode.name());
        options.withParameter(XLS_NAME, TEST_XLS);
        return (List<IObjectId>) v3api.executeCustomASService(sessionToken, new CustomASServiceCode(XLS_IMPORT_API), options);
    }

    static String createFromCsv(IApplicationServerInternalApi v3api, String sessionToken, Path... csv_paths) throws IOException
    {
        List<byte[]> csvs = new ArrayList<>();
        for (Path csv_path : csv_paths)
        {
            byte[] csv = readData(csv_path);
            csvs.add(csv);
        }
        CustomASServiceExecutionOptions options = new CustomASServiceExecutionOptions();
        options.withParameter(CSV_PARAM, csvs);
        options.withParameter(UPDATE_MODE, UpdateMode.IGNORE_EXISTING.name());
        options.withParameter(XLS_NAME, TEST_XLS);
        return v3api.executeCustomASService(sessionToken, new CustomASServiceCode(XLS_IMPORT_API), options).toString();
    }

    static String createFrom(IApplicationServerInternalApi v3api, String sessionToken, Map<String, String> scripts, Path... xls_paths)
            throws IOException
    {
        return TestUtils.createFrom(v3api, sessionToken, scripts, UpdateMode.IGNORE_EXISTING, xls_paths);
    }

    static String createFrom(IApplicationServerInternalApi v3api, String sessionToken, Map<String, String> scripts, UpdateMode updateMode,
            Path... xls_paths) throws IOException
    {
        List<byte[]> excels = new ArrayList<>();
        for (Path xls_path : xls_paths)
        {
            byte[] xls = readData(xls_path);
            excels.add(xls);
        }
        CustomASServiceExecutionOptions options = new CustomASServiceExecutionOptions();
        options.withParameter(XLS_PARAM, excels);
        options.withParameter(SCRIPTS_PARAM, scripts);
        options.withParameter(UPDATE_MODE, updateMode.name());
        options.withParameter(XLS_NAME, TEST_XLS);
        return v3api.executeCustomASService(sessionToken, new CustomASServiceCode(XLS_IMPORT_API), options).toString();
    }

    static String getValidationScript()
    {
        return "def validate(entity, isNew):\n  if isNew:\n    return";
    }

    static String getDynamicScript()
    {
        return "def calculate():\n    return 1";
    }

    static Map<String, String> getValidationPluginMap()
    {
        String dynamicScriptString = getValidationScript();
        Map<String, String> scriptsMap = new HashMap<>();
        scriptsMap.put("valid.py", dynamicScriptString);

        return scriptsMap;
    }

    static Map<String, String> getDynamicPluginMap()
    {
        String dynamicScriptString = getDynamicScript();
        Map<String, String> scriptsMap = new HashMap<>();
        scriptsMap.put("dynamic/dynamic.py", dynamicScriptString);

        return scriptsMap;
    }

    static VocabularyPermId createVocabulary(IApplicationServerInternalApi v3api, String sessionToken, String code, String description)
    {
        List<VocabularyCreation> newVocabularies = new ArrayList<>();
        VocabularyCreation creation = new VocabularyCreation();
        creation.setCode(code);
        creation.setDescription(description);
        newVocabularies.add(creation);
        List<VocabularyPermId> vocabularies = v3api.createVocabularies(sessionToken, newVocabularies);
        if (vocabularies.size() > 0)
        {
            return vocabularies.get(0);
        } else
        {
            return null;
        }
    }

    static String extractSamplePermIdFromResults(String result)
    {
        // Note this will work only if we created single sample!!
        int indexOfSamples = result.indexOf("CreateSamplesOperationResult");
        int permIdStart = indexOfSamples + "CreateSamplesOperationResult".length();
        int permIdEnd = result.indexOf("]", indexOfSamples);
        String permId = result.substring(permIdStart, permIdEnd);
        permId = StringUtils.strip(permId, "[]");
        return permId;
    }

    static List<PropertyAssignment> extractAndSortPropertyAssignmentsPerGivenPropertyName(IEntityType rawData, List<String> propertyNames)
            throws Exception
    {
        List<PropertyAssignment> propertyAssignments = rawData.getPropertyAssignments();
        List<PropertyAssignment> sortedPropertyAssignments = propertyNames.stream().map(propertyName ->
        {
            return propertyAssignments.stream().filter(prop -> prop.getPropertyType().getPermId().toString().equals(propertyName)).findFirst().get();
        }).collect(Collectors.toList());

        if (sortedPropertyAssignments.stream().anyMatch(Objects::isNull))
        {
            throw new Exception("Some properties are missing"
                    + "\nFollowing properties are expected " + Arrays.toString(propertyNames.toArray())
                    + "\n Available properties are: " + Arrays.toString(propertyAssignments.toArray()));
        }

        return sortedPropertyAssignments;
    }

    private static byte[] readData(Path xls_path) throws IOException
    {
        String path = xls_path.toString();
        try (InputStream resourceAsStream = TestUtils.class.getClassLoader().getResourceAsStream(path))
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(resourceAsStream, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

}
