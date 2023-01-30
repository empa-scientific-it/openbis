/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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
import java.util.Iterator;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;

/**
 * @author pkupczyk
 */
@Test(groups = { "before remote api" })
public class GetVocabularyTermTest extends AbstractVocabularyTest
{

    @Test
    public void testGetByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermPermId permId1 = new VocabularyTermPermId("DOG", "ORGANISM");
        VocabularyTermPermId permId2 = new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT");

        Map<IVocabularyTermId, VocabularyTerm> map =
                v3api.getVocabularyTerms(sessionToken, Arrays.asList(permId1, permId2),
                        new VocabularyTermFetchOptions());

        assertEquals(2, map.size());

        Iterator<VocabularyTerm> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByPermIdCaseInsensitive()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermPermId permId1 = new VocabularyTermPermId("Dog", "OrGaNiSm");
        VocabularyTermPermId permId2 = new VocabularyTermPermId("proPRIETARY", "$storage_FORMAT");

        Map<IVocabularyTermId, VocabularyTerm> map =
                v3api.getVocabularyTerms(sessionToken, Arrays.asList(permId1, permId2),
                        new VocabularyTermFetchOptions());

        assertEquals(2, map.size());

        Iterator<VocabularyTerm> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId().getCode(), "DOG");
        assertEquals(map.get(permId1).getPermId().getVocabularyCode(), "ORGANISM");
        assertEquals(map.get(new VocabularyTermPermId("DOG", "ORGANISM")).getPermId().getCode(), "DOG");
        assertEquals(map.get(new VocabularyTermPermId("DOG", "ORGANISM")).getPermId().getVocabularyCode(), "ORGANISM");

        assertEquals(map.get(permId2).getPermId().getCode(), "PROPRIETARY");
        assertEquals(map.get(permId2).getPermId().getVocabularyCode(), "$STORAGE_FORMAT");
        assertEquals(map.get(new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT")).getPermId().getCode(), "PROPRIETARY");
        assertEquals(map.get(new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT")).getPermId().getVocabularyCode(), "$STORAGE_FORMAT");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermPermId permId1 = new VocabularyTermPermId("DOG", "ORGANISM");
        VocabularyTermPermId permId2 = new VocabularyTermPermId("IDONTEXIST", "MENEITHER");
        VocabularyTermPermId permId3 = new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT");

        Map<IVocabularyTermId, VocabularyTerm> map =
                v3api.getVocabularyTerms(sessionToken,
                        Arrays.asList(permId1, permId2, permId3),
                        new VocabularyTermFetchOptions());

        assertEquals(2, map.size());

        Iterator<VocabularyTerm> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermPermId permId1 = new VocabularyTermPermId("HUMAN", "ORGANISM");
        VocabularyTermPermId permId2 = new VocabularyTermPermId("human", "organism");

        Map<IVocabularyTermId, VocabularyTerm> map =
                v3api.getVocabularyTerms(sessionToken, Arrays.asList(permId1, permId2), new VocabularyTermFetchOptions());

        assertEquals(1, map.size());

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithEmptyFetchOptions()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermPermId id = new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT");
        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();

        Map<IVocabularyTermId, VocabularyTerm> terms =
                v3api.getVocabularyTerms(sessionToken, Arrays.asList(id), fetchOptions);

        VocabularyTerm term = terms.get(id);
        assertEquals(term.getPermId(), new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"));
        assertEquals(term.getCode(), "PROPRIETARY");
        assertEquals(term.getDescription(), "proprietary description");
        assertEquals(term.getLabel(), "proprietary label");
        assertEquals(term.getOrdinal(), Long.valueOf(1));
        assertEquals(term.isOfficial(), Boolean.TRUE);
        assertEqualsDate(term.getModificationDate(), "2008-11-05 09:18:00");
        assertEqualsDate(term.getRegistrationDate(), "2008-11-05 09:18:00");
        assertRegistratorNotFetched(term);
        assertVocabularyNotFetched(term);
    }

    @Test
    public void testGetWithVocabularyFetched()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermPermId id = new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT");
        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.withVocabulary().withRegistrator();

        Map<IVocabularyTermId, VocabularyTerm> terms =
                v3api.getVocabularyTerms(sessionToken, Arrays.asList(id), fetchOptions);

        VocabularyTerm term = terms.get(id);
        assertEquals(term.getPermId(), new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"));
        assertEquals(term.getCode(), "PROPRIETARY");
        assertEquals(term.getDescription(), "proprietary description");
        assertEquals(term.getLabel(), "proprietary label");
        assertEquals(term.getOrdinal(), Long.valueOf(1));
        assertEquals(term.isOfficial(), Boolean.TRUE);
        assertEqualsDate(term.getModificationDate(), "2008-11-05 09:18:00");
        assertEqualsDate(term.getRegistrationDate(), "2008-11-05 09:18:00");
        assertRegistratorNotFetched(term);

        assertEquals(term.getVocabulary().getCode(), "$STORAGE_FORMAT");
        assertEquals(term.getVocabulary().getDescription(), "The on-disk storage format of a data set");
        assertEqualsDate(term.getVocabulary().getRegistrationDate(), "2008-11-05 09:18:00");
        assertEqualsDate(term.getVocabulary().getModificationDate(), "2009-03-23 15:34:44");
        assertEquals(term.getVocabulary().getRegistrator().getUserId(), "system");
    }

    @Test
    public void testGetWithRegistratorFetched()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermPermId id = new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT");
        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.withRegistrator();

        Map<IVocabularyTermId, VocabularyTerm> terms =
                v3api.getVocabularyTerms(sessionToken, Arrays.asList(id), fetchOptions);

        VocabularyTerm term = terms.get(id);
        assertEquals(term.getPermId(), new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"));
        assertEquals(term.getCode(), "PROPRIETARY");
        assertEquals(term.getDescription(), "proprietary description");
        assertEquals(term.getLabel(), "proprietary label");
        assertEquals(term.getOrdinal(), Long.valueOf(1));
        assertEquals(term.isOfficial(), Boolean.TRUE);
        assertEqualsDate(term.getModificationDate(), "2008-11-05 09:18:00");
        assertEqualsDate(term.getRegistrationDate(), "2008-11-05 09:18:00");
        assertEquals(term.getRegistrator().getUserId(), "system");
        assertVocabularyNotFetched(term);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermFetchOptions fo = new VocabularyTermFetchOptions();
        fo.withRegistrator();
        fo.withVocabulary();

        v3api.getVocabularyTerms(sessionToken,
                Arrays.asList(new VocabularyTermPermId("DOG", "ORGANISM"), new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT")), fo);

        assertAccessLog(
                "get-vocabulary-terms  VOCABULARY_TERM_IDS('[DOG (ORGANISM), PROPRIETARY ($STORAGE_FORMAT)]') FETCH_OPTIONS('VocabularyTerm\n    with Vocabulary\n    with Registrator\n')");
    }

}
