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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportScript;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.UncompressedImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class ImportTest extends AbstractTest
{

    private static final String VERSIONING_JSON = "./versioning.json";

    private static final String XLS_VERSIONING_DIR = "xls-import.version-data-file";

    private final Map<String, String> IMPORT_SCRIPT_MAP = Map.of("Script 1", "Value 1", "Script 2", "Value 2");

    private final Collection<ImportScript> IMPORT_SCRIPTS = IMPORT_SCRIPT_MAP.entrySet().stream()
            .map(entry -> new ImportScript(entry.getKey(), entry.getValue())).collect(Collectors.toList());


    @BeforeSuite
    public void setupSuite()
    {
        System.setProperty(XLS_VERSIONING_DIR, VERSIONING_JSON);
    }

    @AfterMethod
    public void afterTest()
    {
        new File(VERSIONING_JSON).delete();
    }

    @Test
    public void testUncompressedDataImport()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("import.xlsx"), null);
        final ImportOptions importOptions = new ImportOptions(ImportMode.UPDATE_IF_EXISTS);

        v3api.executeImport(sessionToken, importData, importOptions);

        final VocabularySearchCriteria vocabularySearchCriteria = new VocabularySearchCriteria();
        vocabularySearchCriteria.withCode().thatEquals("DETECTION");

        final VocabularyFetchOptions vocabularyFetchOptions = new VocabularyFetchOptions();
        vocabularyFetchOptions.withTerms();

        final SearchResult<Vocabulary> vocabularySearchResult =
                v3api.searchVocabularies(sessionToken, vocabularySearchCriteria, vocabularyFetchOptions);

        assertEquals(1, vocabularySearchResult.getTotalCount());

        final List<VocabularyTerm> vocabularyTerms = vocabularySearchResult.getObjects().get(0).getTerms();
        assertEquals(2, vocabularyTerms.size());
        assertEquals(Set.of("HRP", "AAA"), vocabularyTerms.stream().map(VocabularyTerm::getCode).collect(Collectors.toSet()));

        v3api.logout(sessionToken);
    }

    @Test
    public void testImportOptionsUpdateIfExists()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("existing_vocabulary.xlsx"), null);
        final ImportOptions importOptions = new ImportOptions(ImportMode.UPDATE_IF_EXISTS);

        v3api.executeImport(sessionToken, importData, importOptions);

        final VocabularySearchCriteria vocabularySearchCriteria = new VocabularySearchCriteria();
        vocabularySearchCriteria.withCode().thatEquals("TEST_VOCABULARY");

        final VocabularyFetchOptions vocabularyFetchOptions = new VocabularyFetchOptions();
        vocabularyFetchOptions.withTerms();

        final SearchResult<Vocabulary> vocabularySearchResult =
                v3api.searchVocabularies(sessionToken, vocabularySearchCriteria, vocabularyFetchOptions);

        assertEquals(1, vocabularySearchResult.getTotalCount());
        assertEquals("Test vocabulary with modifications", vocabularySearchResult.getObjects().get(0).getDescription());

        final List<VocabularyTerm> vocabularyTerms = vocabularySearchResult.getObjects().get(0).getTerms();
        assertEquals(3, vocabularyTerms.size());
        assertEquals(Set.of("TEST_TERM_A", "TEST_TERM_B", "TEST_TERM_C"), vocabularyTerms.stream().map(VocabularyTerm::getCode)
                .collect(Collectors.toSet()));
        assertEquals(Set.of("Test term A", "Test term B", "Test term C"), vocabularyTerms.stream().map(VocabularyTerm::getLabel)
                .collect(Collectors.toSet()));

        v3api.logout(sessionToken);
    }

    @Test
    public void testImportOptionsIgnoreExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("existing_vocabulary.xlsx"), null);
        final ImportOptions importOptions = new ImportOptions(ImportMode.IGNORE_EXISTING);

        v3api.executeImport(sessionToken, importData, importOptions);

        final VocabularySearchCriteria vocabularySearchCriteria = new VocabularySearchCriteria();
        vocabularySearchCriteria.withCode().thatEquals("TEST_VOCABULARY");

        final VocabularyFetchOptions vocabularyFetchOptions = new VocabularyFetchOptions();
        vocabularyFetchOptions.withTerms();

        final SearchResult<Vocabulary> vocabularySearchResult =
                v3api.searchVocabularies(sessionToken, vocabularySearchCriteria, vocabularyFetchOptions);

        assertEquals(1, vocabularySearchResult.getTotalCount());
        assertEquals("Test vocabulary", vocabularySearchResult.getObjects().get(0).getDescription());

        final List<VocabularyTerm> vocabularyTerms = vocabularySearchResult.getObjects().get(0).getTerms();
        assertEquals(3, vocabularyTerms.size());
        assertEquals(Set.of("TEST_TERM_A", "TEST_TERM_B", "TEST_TERM_C"), vocabularyTerms.stream().map(VocabularyTerm::getCode)
                .collect(Collectors.toSet()));
        final List<String> descriptions = vocabularyTerms.stream().map(VocabularyTerm::getLabel).collect(Collectors.toList());
        assertTrue(descriptions.containsAll(Arrays.asList(null, null, "Test term C")));

        v3api.logout(sessionToken);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*FAIL_IF_EXISTS.*")
    public void testImportOptionsFailIfExists()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("existing_vocabulary.xlsx"), null);
        final ImportOptions importOptions = new ImportOptions(ImportMode.FAIL_IF_EXISTS);

        try
        {
            v3api.executeImport(sessionToken, importData, importOptions);
        } finally
        {
            v3api.logout(sessionToken);
        }
    }

    @Test
    public void testWithValidationScript()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final String name = "valid.py";
        final String source = "print 'Test validation script'";
        final ImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("with_validation_script.xls"),
                List.of(new ImportScript(name, source)));
        final ImportOptions importOptions = new ImportOptions(ImportMode.UPDATE_IF_EXISTS);

        v3api.executeImport(sessionToken, importData, importOptions);

        final SampleTypeSearchCriteria sampleTypeSearchCriteria = new SampleTypeSearchCriteria();
        sampleTypeSearchCriteria.withCode().thatEquals("ANTIBODY");

        final SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
        sampleTypeFetchOptions.withValidationPlugin().withScript();

        final SearchResult<SampleType> sampleTypeSearchResult =
                v3api.searchSampleTypes(sessionToken, sampleTypeSearchCriteria, sampleTypeFetchOptions);

        assertEquals(1, sampleTypeSearchResult.getTotalCount());

        final SampleType sampleType = sampleTypeSearchResult.getObjects().get(0);
        final Plugin validationPlugin = sampleType.getValidationPlugin();
        final String validationPluginBareName = name.substring(0, name.lastIndexOf("."));

        assertEquals(sampleType.getCode() + "." + validationPluginBareName, validationPlugin.getName());
        assertEquals(source, validationPlugin.getScript());

        v3api.logout(sessionToken);
    }

//    public void testWithDynamicScript()
//    {
//        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
//
//        final String name = "dynamic.py";
//        final String source = "print 'Test dynamic script'";
//        final ImportData importData = new UncompressedImportData(ImportFormat.XLS, getFileContent("with_dynamic_script.xls"),
//                List.of(new ImportScript(name, source)));
//        final ImportOptions importOptions = new ImportOptions(ImportMode.UPDATE_IF_EXISTS);
//
//        v3api.executeImport(sessionToken, importData, importOptions);
//
//        final SampleTypeSearchCriteria sampleTypeSearchCriteria = new SampleTypeSearchCriteria();
//        sampleTypeSearchCriteria.withCode().thatEquals("ANTIBODY");
//
//        final SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
//        sampleTypeFetchOptions.withPropertyAssignmentsUsing(new PropertyAssignmentFetchOptions());
//
//        final SearchResult<SampleType> sampleTypeSearchResult =
//                v3api.searchSampleTypes(sessionToken, sampleTypeSearchCriteria, sampleTypeFetchOptions);
//
//        assertEquals(1, sampleTypeSearchResult.getTotalCount());
//
//        sampleTypeSearchResult.getObjects().get(0).
//
//        v3api.logout(sessionToken);
//    }

    private byte[] getFileContent(final String fileName)
    {
        try (final InputStream is = ImportTest.class.getResourceAsStream("test_files/xls/" + fileName))
        {
            if (is == null)
            {
                throw new RuntimeException();
            }

            return is.readAllBytes();
        } catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
