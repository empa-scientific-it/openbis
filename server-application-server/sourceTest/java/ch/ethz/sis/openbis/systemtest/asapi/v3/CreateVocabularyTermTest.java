/*
 * Copyright 2016 ETH Zuerich, CISD
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
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
@Test(groups = { "before remote api" })
public class CreateVocabularyTermTest extends AbstractVocabularyTest
{

    @DataProvider
    private Object[][] providerTestCreateAuthorization()
    {
        return new Object[][] {
                { "ORGANISM", SYSTEM_USER, true, null },
                { "ORGANISM", SYSTEM_USER, false, null },

                { "ORGANISM", TEST_USER, true, null },
                { "ORGANISM", TEST_USER, false, null },

                { "ORGANISM", TEST_POWER_USER_CISD, true,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_role'" },
                { "ORGANISM", TEST_POWER_USER_CISD, false, null },

                { "$PLATE_GEOMETRY", SYSTEM_USER, true, null },
                { "$PLATE_GEOMETRY", SYSTEM_USER, false, null },

                { "$PLATE_GEOMETRY", TEST_USER, true, null },
                { "$PLATE_GEOMETRY", TEST_USER, false, null },

                { "$PLATE_GEOMETRY", TEST_POWER_USER_CISD, true,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user 'test_role'" },
                { "$PLATE_GEOMETRY", TEST_POWER_USER_CISD, false, null },
        };
    }

    @Test(dataProvider = "providerTestCreateAuthorization")
    public void testCreateAuthorization(String vocabularyCode, String termRegistrator, boolean termOfficial,
            String expectedError)
    {
        String sessionToken = termRegistrator.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(termRegistrator, PASSWORD);

        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setCode("TEST-CODE");
        creation.setVocabularyId(new VocabularyPermId(vocabularyCode));
        creation.setOfficial(termOfficial);

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    List<VocabularyTermPermId> ids = v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation));
                    assertEquals(ids.size(), 1);
                }
            }, expectedError);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Vocabulary term vocabulary id cannot be null.*")
    public void testCreateWithVocabularyIdNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermCreation creation = termCreation();
        creation.setVocabularyId(null);

        v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Vocabulary IDONTEXIST does not exist.*")
    public void testCreateWithVocabularyIdNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermCreation creation = termCreation();
        creation.setVocabularyId(new VocabularyPermId("IDONTEXIST"));

        v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Vocabulary term code cannot be null or empty.*")
    public void testCreateWithCodeNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermCreation creation = termCreation();
        creation.setCode(null);

        v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation));
    }

    @DataProvider
    private Object[][] providerTestCreateWithCodeDuplicated()
    {
        return new Object[][] {
                { "ORGANISM", SYSTEM_USER, SYSTEM_USER, "Vocabulary term EXISTING-TERM-CODE (ORGANISM) already exists" },
                { "ORGANISM", SYSTEM_USER, TEST_USER, "Vocabulary term EXISTING-TERM-CODE (ORGANISM) already exists" },

                { "ORGANISM", TEST_USER, SYSTEM_USER, "Vocabulary term EXISTING-TERM-CODE (ORGANISM) already exists" },
                { "ORGANISM", TEST_USER, TEST_USER, "Vocabulary term EXISTING-TERM-CODE (ORGANISM) already exists" },

                { "$PLATE_GEOMETRY", SYSTEM_USER, SYSTEM_USER, "Vocabulary term EXISTING-TERM-CODE ($PLATE_GEOMETRY) already exists" },
                { "$PLATE_GEOMETRY", SYSTEM_USER, TEST_USER, "Vocabulary term EXISTING-TERM-CODE ($PLATE_GEOMETRY) already exists" },

                { "$PLATE_GEOMETRY", TEST_USER, SYSTEM_USER, "Vocabulary term EXISTING-TERM-CODE ($PLATE_GEOMETRY) already exists" },
                { "$PLATE_GEOMETRY", TEST_USER, TEST_USER, "Vocabulary term EXISTING-TERM-CODE ($PLATE_GEOMETRY) already exists" },
        };
    }

    @Test(dataProvider = "providerTestCreateWithCodeDuplicated")
    public void testCreateWithCodeDuplicated(String vocabularyCode, String existingTermRegistrator, String newTermRegistrator,
            String expectedError)
    {
        String existingTermRegistratorSessionToken =
                existingTermRegistrator.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(existingTermRegistrator, PASSWORD);
        String newTermRegistratorSessionToken =
                newTermRegistrator.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(newTermRegistrator, PASSWORD);

        VocabularyTermCreation existingTermCreation = new VocabularyTermCreation();
        existingTermCreation.setCode("EXISTING-TERM-CODE");
        existingTermCreation.setVocabularyId(new VocabularyPermId(vocabularyCode));

        v3api.createVocabularyTerms(existingTermRegistratorSessionToken, Arrays.asList(existingTermCreation));

        VocabularyTermCreation newTermCreation = new VocabularyTermCreation();
        newTermCreation.setCode("NEW-TERM-CODE");
        newTermCreation.setVocabularyId(new VocabularyPermId(vocabularyCode));

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createVocabularyTerms(newTermRegistratorSessionToken, Arrays.asList(existingTermCreation));
                }
            }, expectedError);
    }

    @Test
    public void testCreateWithOfficialTermAndPreviousTermNull()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setOfficial(true);
        createWithPreviousTermNull(creation);
    }

    @Test
    public void testCreateWithOfficialTermAndPreviousTermNotNull()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setOfficial(true);
        createWithPreviousTermNotNull(creation);
    }

    @Test
    public void testCreateWithUnofficialTermAndPreviousTermNull()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setOfficial(false);
        createWithPreviousTermNull(creation);
    }

    @Test
    public void testCreateWithUnofficialTermAndPreviousTermNotNull()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setOfficial(false);
        createWithPreviousTermNotNull(creation);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Position of term TIGER \\(ORGANISM\\) could not be found as the specified previous term IDONTEXIST \\(ORGANISM\\) does not exist.*")
    public void testCreateWithPreviousTermNonexistent()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setPreviousTermId(new VocabularyTermPermId("IDONTEXIST", "ORGANISM"));
        createTerms(TEST_USER, PASSWORD, creation);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Position of term TIGER \\(ORGANISM\\) could not be found as the specified previous term MALE \\(GENDER\\) is in a different vocabulary \\(GENDER\\).*")
    public void testCreateWithPreviousTermFromDifferentVocabulary()
    {
        VocabularyTermCreation creation = termCreation();
        creation.setPreviousTermId(new VocabularyTermPermId("MALE", "GENDER"));
        createTerms(TEST_USER, PASSWORD, creation);
    }

    @Test
    public void testCreateWithPreviousTermNewlyCreated()
    {
        VocabularyTermCreation creation1 = termCreation();
        creation1.setCode("NEW1");
        creation1.setPreviousTermId(new VocabularyTermPermId("HUMAN", "ORGANISM"));

        VocabularyTermCreation creation2 = termCreation();
        creation2.setCode("NEW2");
        creation2.setPreviousTermId(new VocabularyTermPermId("NEW1", "ORGANISM"));

        List<VocabularyTerm> termsBefore = listTerms(creation1.getVocabularyId());
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        createTerms(TEST_USER, PASSWORD, creation1, creation2);

        List<VocabularyTerm> termsAfter = listTerms(creation1.getVocabularyId());
        assertTerms(termsAfter, "RAT", "DOG", "HUMAN", "NEW1", "NEW2", "GORILLA", "FLY");
    }

    @Test
    public void testCreateMultipleTerms()
    {
        VocabularyTermCreation creation1 = termCreation();
        creation1.setCode("NEW1");
        creation1.setPreviousTermId(new VocabularyTermPermId("HUMAN", "ORGANISM"));

        VocabularyTermCreation creation2 = termCreation();
        creation2.setCode("NEW2");
        creation2.setPreviousTermId(new VocabularyTermPermId("GORILLA", "ORGANISM"));

        VocabularyTermCreation creation3 = termCreation();
        creation3.setCode("NEW3");
        creation3.setOfficial(false);
        creation3.setPreviousTermId(new VocabularyTermPermId("NEW2", "ORGANISM"));

        VocabularyTermCreation creation4 = termCreation();
        creation4.setCode("NEW4");
        creation4.setPreviousTermId(null);

        List<VocabularyTerm> termsBefore = listTerms(creation1.getVocabularyId());
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        createTerms(TEST_USER, PASSWORD, creation1, creation2, creation3, creation4);

        List<VocabularyTerm> termsAfter = listTerms(creation1.getVocabularyId());
        assertTerms(termsAfter, "RAT", "DOG", "HUMAN", "NEW1", "GORILLA", "NEW2", "NEW3", "FLY", "NEW4");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setVocabularyId(new VocabularyPermId("ORGANISM"));
        creation.setCode("LOG_TEST_1");

        VocabularyTermCreation creation2 = new VocabularyTermCreation();
        creation2.setVocabularyId(new VocabularyPermId("$PLATE_GEOMETRY"));
        creation2.setCode("LOG_TEST_2");

        v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-vocabulary-terms  NEW_VOCABULARY_TERMS('[VocabularyTermCreation[vocabularyId=ORGANISM,code=LOG_TEST_1], VocabularyTermCreation[vocabularyId=$PLATE_GEOMETRY,code=LOG_TEST_2]]')");
    }

    private VocabularyTermCreation termCreation()
    {
        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setVocabularyId(new VocabularyPermId("ORGANISM"));
        creation.setCode("TIGER");
        creation.setDescription("tiger's description");
        creation.setLabel("tiger's label");
        return creation;
    }

    private List<VocabularyTerm> createTerms(String user, String password, VocabularyTermCreation... creations)
    {
        String sessionToken = v3api.login(user, password);

        // build criteria
        VocabularyTermSearchCriteria criteria = new VocabularyTermSearchCriteria();
        criteria.withOrOperator();
        for (VocabularyTermCreation creation : creations)
        {
            String vocabularyCode = ((VocabularyPermId) creation.getVocabularyId()).getPermId();
            criteria.withId().thatEquals(new VocabularyTermPermId(creation.getCode(), vocabularyCode));
        }

        // build fetch options
        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.withRegistrator();
        fetchOptions.withVocabulary();

        // search before creation
        SearchResult<VocabularyTerm> resultsBefore = v3api.searchVocabularyTerms(sessionToken, criteria, fetchOptions);
        assertEquals(resultsBefore.getTotalCount(), 0);

        // create
        List<VocabularyTermPermId> permIds = v3api.createVocabularyTerms(sessionToken, Arrays.asList(creations));

        assertEquals(permIds.size(), creations.length);
        for (int i = 0; i < creations.length; i++)
        {
            VocabularyTermCreation creation = creations[i];
            VocabularyTermPermId permId = permIds.get(i);
            assertEquals(permId.getCode(), creation.getCode());
            assertEquals(permId.getVocabularyCode(), ((VocabularyPermId) creation.getVocabularyId()).getPermId());
        }

        // search after creation
        SearchResult<VocabularyTerm> resultsAfter = v3api.searchVocabularyTerms(sessionToken, criteria, fetchOptions);

        assertEquals(resultsAfter.getTotalCount(), creations.length);
        for (int i = 0; i < creations.length; i++)
        {
            VocabularyTermCreation creation = creations[i];
            VocabularyTerm term = resultsAfter.getObjects().get(i);
            assertEquals(term.getCode(), creation.getCode());
            assertEquals(term.getVocabulary().getCode(), ((VocabularyPermId) creation.getVocabularyId()).getPermId());
            assertEquals(term.getRegistrator().getUserId(), user);
            assertEquals(term.getLabel(), creation.getLabel());
            assertEquals(term.getDescription(), creation.getDescription());
            assertEquals(term.isOfficial(), creation.isOfficial());
        }

        v3api.logout(sessionToken);

        return resultsAfter.getObjects();
    }

    private void createWithPreviousTermNull(VocabularyTermCreation creation)
    {
        List<VocabularyTerm> termsBefore = listTerms(creation.getVocabularyId());
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        creation.setPreviousTermId(null);
        createTerms(TEST_USER, PASSWORD, creation);

        List<VocabularyTerm> termsAfter = listTerms(creation.getVocabularyId());
        assertTerms(termsAfter, "RAT", "DOG", "HUMAN", "GORILLA", "FLY", creation.getCode());
    }

    private void createWithPreviousTermNotNull(VocabularyTermCreation creation)
    {
        List<VocabularyTerm> termsBefore = listTerms(creation.getVocabularyId());
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        creation.setPreviousTermId(new VocabularyTermPermId("HUMAN", "ORGANISM"));
        createTerms(TEST_USER, PASSWORD, creation);

        List<VocabularyTerm> termsAfter = listTerms(creation.getVocabularyId());
        assertTerms(termsAfter, "RAT", "DOG", "HUMAN", creation.getCode(), "GORILLA", "FLY");
    }

}
