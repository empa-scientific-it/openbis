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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.ImportScript;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.data.UncompressedImportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.importer.options.ImportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;

public class ImportTest extends AbstractTest
{

    private final Map<String, String> IMPORT_SCRIPT_MAP = Map.of("Script 1", "Value 1", "Script 2", "Value 2");

    private final Collection<ImportScript> IMPORT_SCRIPTS = IMPORT_SCRIPT_MAP.entrySet().stream()
            .map(entry -> new ImportScript(entry.getKey(), entry.getValue())).collect(Collectors.toList());

    private static byte[] fileContent;

    @BeforeClass
    public void setupClass()
    {
        try (final InputStream is = ImportTest.class.getResourceAsStream("test_files/xls/import.xlsx"))
        {
            if (is == null)
            {
                throw new RuntimeException();
            }

            fileContent = is.readAllBytes();
        } catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUncompressedDataImport()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ImportData importData = new UncompressedImportData(ImportFormat.XLS, fileContent, IMPORT_SCRIPTS);
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
    }

}
