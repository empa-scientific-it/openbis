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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.IImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportScript;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.UncompressedImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class UncompressedImportTest extends AbstractImportTest
{

    @Test
    public void testDataImport()
    {
        final IImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("import.xlsx"), null);
        final ImportOptions importOptions = new ImportOptions(ImportMode.UPDATE_IF_EXISTS);

        v3api.executeImport(sessionToken, importData, importOptions);

        final VocabularySearchCriteria vocabularySearchCriteria = new VocabularySearchCriteria();
        vocabularySearchCriteria.withCode().thatEquals("DETECTION");

        final VocabularyFetchOptions vocabularyFetchOptions = new VocabularyFetchOptions();
        vocabularyFetchOptions.withTerms();

        final SearchResult<Vocabulary> vocabularySearchResult =
                v3api.searchVocabularies(sessionToken, vocabularySearchCriteria, vocabularyFetchOptions);

        assertEquals(vocabularySearchResult.getTotalCount(), 1);

        final List<VocabularyTerm> vocabularyTerms = vocabularySearchResult.getObjects().get(0).getTerms();
        assertEquals(vocabularyTerms.size(), 2);
        assertEquals(vocabularyTerms.stream().map(VocabularyTerm::getCode).collect(Collectors.toSet()), Set.of("HRP", "AAA"));
    }

    @Test
    public void testImportOptionsUpdateIfExists()
    {
        final IImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("existing_vocabulary.xlsx"), null);
        final ImportOptions importOptions = new ImportOptions(ImportMode.UPDATE_IF_EXISTS);

        v3api.executeImport(sessionToken, importData, importOptions);

        final VocabularySearchCriteria vocabularySearchCriteria = new VocabularySearchCriteria();
        vocabularySearchCriteria.withCode().thatEquals("TEST_VOCABULARY");

        final VocabularyFetchOptions vocabularyFetchOptions = new VocabularyFetchOptions();
        vocabularyFetchOptions.withTerms();

        final SearchResult<Vocabulary> vocabularySearchResult =
                v3api.searchVocabularies(sessionToken, vocabularySearchCriteria, vocabularyFetchOptions);

        assertEquals(vocabularySearchResult.getTotalCount(), 1);
        assertEquals(vocabularySearchResult.getObjects().get(0).getDescription(), "Test vocabulary with modifications");

        final List<VocabularyTerm> vocabularyTerms = vocabularySearchResult.getObjects().get(0).getTerms();
        assertEquals(vocabularyTerms.stream().map(VocabularyTerm::getCode).collect(Collectors.toSet()),
                Set.of("TEST_TERM_A", "TEST_TERM_B", "TEST_TERM_C"));
        assertEquals(vocabularyTerms.stream().map(VocabularyTerm::getLabel).collect(Collectors.toSet()),
                Set.of("Test term A", "Test term B", "Test term C"));
    }

    @Test
    public void testImportOptionsIgnoreExisting()
    {
        final IImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("existing_vocabulary.xlsx"), null);
        final ImportOptions importOptions = new ImportOptions(ImportMode.IGNORE_EXISTING);

        v3api.executeImport(sessionToken, importData, importOptions);

        final VocabularySearchCriteria vocabularySearchCriteria = new VocabularySearchCriteria();
        vocabularySearchCriteria.withCode().thatEquals("TEST_VOCABULARY");

        final VocabularyFetchOptions vocabularyFetchOptions = new VocabularyFetchOptions();
        vocabularyFetchOptions.withTerms();

        final SearchResult<Vocabulary> vocabularySearchResult =
                v3api.searchVocabularies(sessionToken, vocabularySearchCriteria, vocabularyFetchOptions);

        assertEquals(vocabularySearchResult.getTotalCount(), 1);
        assertEquals(vocabularySearchResult.getObjects().get(0).getDescription(), "Test vocabulary");

        final List<VocabularyTerm> vocabularyTerms = vocabularySearchResult.getObjects().get(0).getTerms();
        assertEquals(vocabularyTerms.stream().map(VocabularyTerm::getCode).collect(Collectors.toSet()),
                Set.of("TEST_TERM_A", "TEST_TERM_B", "TEST_TERM_C"));
        final List<String> descriptions = vocabularyTerms.stream().map(VocabularyTerm::getLabel).collect(Collectors.toList());
        assertTrue(descriptions.containsAll(Arrays.asList(null, null, "Test term C")));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*FAIL_IF_EXISTS.*")
    public void testImportOptionsFailIfExists()
    {
        final IImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("existing_vocabulary.xlsx"), null);
        final ImportOptions importOptions = new ImportOptions(ImportMode.FAIL_IF_EXISTS);

        v3api.executeImport(sessionToken, importData, importOptions);
    }

    @Test
    public void testWithValidationScript()
    {
        final String name = "valid.py";
        final String source = "print 'Test validation script'";
        final IImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("validation_script.xls"),
                List.of(new ImportScript(name, source)));
        final ImportOptions importOptions = new ImportOptions(ImportMode.UPDATE_IF_EXISTS);

        v3api.executeImport(sessionToken, importData, importOptions);

        final SampleTypeSearchCriteria sampleTypeSearchCriteria = new SampleTypeSearchCriteria();
        sampleTypeSearchCriteria.withCode().thatEquals("ANTIBODY");

        final SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
        sampleTypeFetchOptions.withValidationPlugin().withScript();

        final SearchResult<SampleType> sampleTypeSearchResult =
                v3api.searchSampleTypes(sessionToken, sampleTypeSearchCriteria, sampleTypeFetchOptions);

        assertEquals(sampleTypeSearchResult.getTotalCount(), 1);

        final SampleType sampleType = sampleTypeSearchResult.getObjects().get(0);
        final Plugin validationPlugin = sampleType.getValidationPlugin();
        final String validationPluginBareName = name.substring(0, name.lastIndexOf("."));

        assertEquals(validationPlugin.getName(), sampleType.getCode() + "." + validationPluginBareName);
        assertEquals(validationPlugin.getScript(), source);
    }

    @Test
    public void testWithDynamicScript()
    {
        final String name = "dynamic.py";
        final String source = "1+1";
        final IImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("dynamic_script.xls"),
                List.of(new ImportScript(name, source)));
        final ImportOptions importOptions = new ImportOptions(ImportMode.UPDATE_IF_EXISTS);

        v3api.executeImport(sessionToken, importData, importOptions);

        final SampleTypeSearchCriteria sampleTypeSearchCriteria = new SampleTypeSearchCriteria();
        sampleTypeSearchCriteria.withCode().thatEquals("ANTIBODY");

        final SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = sampleTypeFetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        propertyAssignmentFetchOptions.withPropertyType();

        final SearchResult<SampleType> sampleTypeSearchResult =
                v3api.searchSampleTypes(sessionToken, sampleTypeSearchCriteria, sampleTypeFetchOptions);
        assertEquals(sampleTypeSearchResult.getTotalCount(), 1);

        final SampleType sampleType = sampleTypeSearchResult.getObjects().get(0);
        assertEquals(sampleType.getPropertyAssignments().size(), 1);

        final PropertyAssignment propertyAssignment = sampleType.getPropertyAssignments().get(0);
        final Plugin plugin = propertyAssignment.getPlugin();

        final String pluginBareName = name.substring(0, name.lastIndexOf("."));

        assertEquals(plugin.getName(), propertyAssignment.getPropertyType().getCode() + "." + pluginBareName);
        assertEquals(plugin.getScript(), source);
    }

}
