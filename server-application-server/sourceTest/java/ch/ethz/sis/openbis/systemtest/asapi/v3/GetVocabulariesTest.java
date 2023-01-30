/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;

/**
 * @author Franz-Josef Elmer
 */
public class GetVocabulariesTest extends AbstractTest
{
    @Test
    public void testGetVocabularyWithTerms()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularyPermId id1 = new VocabularyPermId("ORGANISM");
        VocabularyPermId id2 = new VocabularyPermId("$STORAGE_FORMAT");
        VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms();

        // When
        Map<IVocabularyId, Vocabulary> result = v3api.getVocabularies(sessionToken, Arrays.asList(id1, id2), fetchOptions);

        // Then
        Vocabulary v1 = result.get(id1);
        assertEquals(v1.getCode(), id1.getPermId());
        assertEquals(v1.getDescription(), "available-organism");
        assertEquals(v1.isManagedInternally(), false);
        assertEquals(v1.isChosenFromList(), true);
        assertEquals(v1.getUrlTemplate(), null);
        assertEquals(v1.getTerms().toString(),
                "[VocabularyTerm DOG, VocabularyTerm FLY, VocabularyTerm GORILLA, VocabularyTerm HUMAN, VocabularyTerm RAT]");
        Vocabulary v2 = result.get(id2);
        assertEquals(v2.getCode(), id2.getPermId());
        assertEquals(v2.getDescription(), "The on-disk storage format of a data set");
        assertEquals(v2.isManagedInternally(), true);
        assertEquals(v2.isChosenFromList(), true);
        assertEquals(v2.getUrlTemplate(), null);
        assertEquals(v2.getTerms().toString(), "[VocabularyTerm BDS_DIRECTORY, VocabularyTerm PROPRIETARY]");
        assertEquals(v2.getTerms().get(1).getLabel(), "proprietary label");
        assertEquals(v2.getTerms().get(1).getDescription(), "proprietary description");
        assertEquals(v2.getTerms().get(1).getOrdinal().longValue(), 1);
        assertEquals(v2.getTerms().get(1).isOfficial(), Boolean.TRUE);
        assertEquals(result.size(), 2);
        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyFetchOptions fo = new VocabularyFetchOptions();
        fo.withRegistrator();
        fo.withTerms();

        v3api.getVocabularies(sessionToken, Arrays.asList(new VocabularyPermId("ORGANISM"), new VocabularyPermId("$STORAGE_FORMAT")), fo);

        assertAccessLog(
                "get-vocabularies  VOCABULARY_IDS('[ORGANISM, $STORAGE_FORMAT]') FETCH_OPTIONS('Vocabulary\n    with Registrator\n    with Terms\n')");
    }

}
