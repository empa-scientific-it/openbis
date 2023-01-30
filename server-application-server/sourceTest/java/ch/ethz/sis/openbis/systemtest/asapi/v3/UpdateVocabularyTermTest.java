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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
@Test(groups = { "before remote api" })
public class UpdateVocabularyTermTest extends AbstractVocabularyTest
{

    @DataProvider
    private Object[][] providerTestUpdateAuthorization()
    {
        return new Object[][] {
                { "ORGANISM", SYSTEM_USER, SYSTEM_USER, true, null },
                { "ORGANISM", SYSTEM_USER, SYSTEM_USER, false, null },
                { "ORGANISM", SYSTEM_USER, TEST_USER, true, null },
                { "ORGANISM", SYSTEM_USER, TEST_USER, false, null },

                { "ORGANISM", TEST_USER, TEST_USER, true, null },
                { "ORGANISM", TEST_USER, TEST_USER, false, null },

                { "ORGANISM", TEST_POWER_USER_CISD, SYSTEM_USER, false, null },
                { "ORGANISM", TEST_POWER_USER_CISD, TEST_USER, false, null },
                { "ORGANISM", TEST_POWER_USER_CISD, TEST_POWER_USER_CISD, false,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user" },

                { "$PLATE_GEOMETRY", SYSTEM_USER, SYSTEM_USER, true, null },
                { "$PLATE_GEOMETRY", SYSTEM_USER, SYSTEM_USER, false, null },
                { "$PLATE_GEOMETRY", SYSTEM_USER, TEST_USER, true,
                        "Terms created by the system user that belong to internal vocabularies can be managed only by the system user" },
                { "$PLATE_GEOMETRY", SYSTEM_USER, TEST_USER, false,
                        "Terms created by the system user that belong to internal vocabularies can be managed only by the system user" },

                { "$PLATE_GEOMETRY", TEST_USER, TEST_USER, true, null },
                { "$PLATE_GEOMETRY", TEST_USER, TEST_USER, false, null },

                { "$PLATE_GEOMETRY", TEST_POWER_USER_CISD, SYSTEM_USER, false, null },
                { "$PLATE_GEOMETRY", TEST_POWER_USER_CISD, TEST_USER, false, null },
                { "$PLATE_GEOMETRY", TEST_POWER_USER_CISD, TEST_POWER_USER_CISD, false,
                        "None of method roles '[INSTANCE_ADMIN, INSTANCE_ETL_SERVER]' could be found in roles of user" },
        };
    }

    @Test(dataProvider = "providerTestUpdateAuthorization")
    public void testUpdateAuthorization(String vocabularyCode, String termRegistrator, String termUpdater, boolean termOfficial,
            String expectedError)
    {
        String sessionTokenRegistrator = termRegistrator.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(termRegistrator, PASSWORD);

        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setCode("TEST-CODE");
        creation.setVocabularyId(new VocabularyPermId(vocabularyCode));
        creation.setOfficial(termOfficial);

        List<VocabularyTermPermId> permIds = v3api.createVocabularyTerms(sessionTokenRegistrator, Arrays.asList(creation));

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(permIds.get(0));
        update.setOfficial(true);

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionTokenUpdater = termUpdater.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(termUpdater, PASSWORD);
                    v3api.updateVocabularyTerms(sessionTokenUpdater, Arrays.asList(update));
                }
            }, expectedError);
    }

    @DataProvider
    private Object[][] providerTestUpdateAndTakeOverTerm()
    {
        return new Object[][] {
                { "ORGANISM", SYSTEM_USER, SYSTEM_USER, SYSTEM_USER, null },
                { "ORGANISM", SYSTEM_USER, TEST_USER, SYSTEM_USER, null },

                { "ORGANISM", TEST_USER, SYSTEM_USER, TEST_USER, null },
                { "ORGANISM", TEST_USER, TEST_USER, TEST_USER, null },

                { "$PLATE_GEOMETRY", SYSTEM_USER, SYSTEM_USER, SYSTEM_USER, null },
                { "$PLATE_GEOMETRY", SYSTEM_USER, TEST_USER, SYSTEM_USER,
                        "Terms created by the system user that belong to internal vocabularies can be managed only by the system user" },

                { "$PLATE_GEOMETRY", TEST_USER, SYSTEM_USER, SYSTEM_USER, null },
                { "$PLATE_GEOMETRY", TEST_USER, TEST_USER, TEST_USER, null },
        };
    }

    @Test(dataProvider = "providerTestUpdateAndTakeOverTerm")
    public void testUpdateAndTakeOverTerm(String vocabularyCode, String termRegistrator, String termUpdater,
            String expectedTermRegistratorAfterUpdate, String expectedError)
    {
        String termRegistratorSessionToken =
                termRegistrator.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(termRegistrator, PASSWORD);
        String termUpdaterSessionToken =
                termUpdater.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(termUpdater, PASSWORD);
        String termGetterSessionToken = v3api.loginAsSystem();

        VocabularyTermCreation termCreation = new VocabularyTermCreation();
        termCreation.setCode("TERM-TO-TAKE-OVER");
        termCreation.setVocabularyId(new VocabularyPermId(vocabularyCode));
        termCreation.setLabel("Original Label");
        termCreation.setDescription("Original Description");

        List<VocabularyTermPermId> termIds = v3api.createVocabularyTerms(termRegistratorSessionToken, Arrays.asList(termCreation));

        VocabularyTermUpdate termUpdate = new VocabularyTermUpdate();
        termUpdate.setVocabularyTermId(termIds.get(0));
        termUpdate.setLabel("Updated Label");
        termUpdate.setDescription("Updated Description");

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateVocabularyTerms(termUpdaterSessionToken, Arrays.asList(termUpdate));

                    VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
                    fetchOptions.withRegistrator();

                    Map<IVocabularyTermId, VocabularyTerm> terms = v3api.getVocabularyTerms(termGetterSessionToken, termIds, fetchOptions);
                    assertEquals(terms.size(), 1);

                    VocabularyTerm term = terms.get(termIds.get(0));
                    assertEquals(term.getCode(), "TERM-TO-TAKE-OVER");
                    assertEquals(term.getLabel(), "Updated Label");
                    assertEquals(term.getDescription(), "Updated Description");
                    assertEquals(term.getRegistrator().getUserId(), expectedTermRegistratorAfterUpdate);
                }
            }, expectedError);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Vocabulary term id cannot be null.*")
    public void testUpdateWithVocabularyIdNull()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        updateTerms(TEST_USER, PASSWORD, update);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Object with VocabularyTermPermId = \\[IDONTEXIST \\(MENEITHER\\)\\] has not been found.*")
    public void testUpdateWithVocabularyIdNonexistent()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("IDONTEXIST", "MENEITHER"));
        updateTerms(TEST_USER, PASSWORD, update);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Offical vocabulary term DOG \\(ORGANISM\\) cannot be updated to be unofficial.*")
    public void testUpdateWithOfficialTermMadeUnofficial()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getOfficialTermId());
        update.setOfficial(false);

        updateTerms(TEST_USER, PASSWORD, update);
    }

    @Test
    public void testUpdateWithUnofficialTermMadeOfficial()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getUnofficialTermId());
        update.setOfficial(true);

        List<VocabularyTerm> terms = updateTerms(TEST_USER, PASSWORD, update);

        assertEquals(terms.get(0).isOfficial(), Boolean.TRUE);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*None of method roles '\\[INSTANCE_ADMIN, INSTANCE_ETL_SERVER\\]' could be found in roles of user 'observer'.*")
    public void testUpdateWithUnofficialTermMadeOfficialUnauthorized()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(getUnofficialTermId());
        update.setOfficial(true);

        updateTerms(TEST_GROUP_OBSERVER, PASSWORD, update);
    }

    @Test
    public void testUpdateWithPreviousTermIdNull()
    {
        String vocabularyCode = "ORGANISM";

        List<VocabularyTerm> termsBefore = searchTerms(vocabularyCode);
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("DOG", vocabularyCode));
        update.setPreviousTermId(null);

        updateTerms(TEST_USER, PASSWORD, update);

        List<VocabularyTerm> termsAfter = searchTerms(vocabularyCode);
        assertTerms(termsAfter, "DOG", "RAT", "HUMAN", "GORILLA", "FLY");
    }

    @Test
    public void testUpdateWithPreviousTermIdNotNull()
    {
        String vocabularyCode = "ORGANISM";

        List<VocabularyTerm> termsBefore = searchTerms(vocabularyCode);
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("DOG", vocabularyCode));
        update.setPreviousTermId(new VocabularyTermPermId("GORILLA", vocabularyCode));

        updateTerms(TEST_USER, PASSWORD, update);

        List<VocabularyTerm> termsAfter = searchTerms(vocabularyCode);
        assertTerms(termsAfter, "RAT", "HUMAN", "GORILLA", "DOG", "FLY");
    }

    @Test
    public void testUpdateWithPreviousTermIdAndMultipleTerms()
    {
        String vocabularyCode = "ORGANISM";

        List<VocabularyTerm> termsBefore = searchTerms(vocabularyCode);
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        VocabularyTermUpdate updateDog = new VocabularyTermUpdate();
        updateDog.setVocabularyTermId(new VocabularyTermPermId("DOG", vocabularyCode));
        updateDog.setPreviousTermId(new VocabularyTermPermId("GORILLA", vocabularyCode));

        VocabularyTermUpdate updateGorilla = new VocabularyTermUpdate();
        updateGorilla.setVocabularyTermId(new VocabularyTermPermId("GORILLA", vocabularyCode));
        updateGorilla.setPreviousTermId(new VocabularyTermPermId("RAT", vocabularyCode));

        updateTerms(TEST_USER, PASSWORD, updateDog, updateGorilla);

        List<VocabularyTerm> termsAfter = searchTerms(vocabularyCode);
        assertTerms(termsAfter, "RAT", "GORILLA", "HUMAN", "DOG", "FLY");
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Object with VocabularyTermPermId = \\[IDONTEXIST \\(ORGANISM\\)\\] has not been found.*")
    public void testUpdateWithPreviousTermIdNonexistent()
    {
        String vocabularyCode = "ORGANISM";

        List<VocabularyTerm> termsBefore = searchTerms(vocabularyCode);
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("DOG", vocabularyCode));
        update.setPreviousTermId(new VocabularyTermPermId("IDONTEXIST", vocabularyCode));

        updateTerms(TEST_USER, PASSWORD, update);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Position of term DOG \\(ORGANISM\\) could not be found as the specified previous term MALE \\(GENDER\\) is in a different vocabulary \\(GENDER\\).*")
    public void testUpdateWithPreviousTermIdFromDifferentVocabulary()
    {
        String vocabularyCode = "ORGANISM";

        List<VocabularyTerm> termsBefore = searchTerms(vocabularyCode);
        assertTerms(termsBefore, "RAT", "DOG", "HUMAN", "GORILLA", "FLY");

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("DOG", vocabularyCode));
        update.setPreviousTermId(new VocabularyTermPermId("MALE", "GENDER"));

        updateTerms(TEST_USER, PASSWORD, update);
    }

    @Test
    public void testUpdateWithLabel()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("HUMAN", "ORGANISM"));
        update.setLabel("a brand new label");

        List<VocabularyTerm> terms = updateTerms(TEST_USER, PASSWORD, update);

        assertEquals(terms.get(0).getLabel(), update.getLabel().getValue());
    }

    @Test
    public void testUpdateWithDescription()
    {
        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("HUMAN", "ORGANISM"));
        update.setDescription("a brand new description");

        List<VocabularyTerm> terms = updateTerms(TEST_USER, PASSWORD, update);

        assertEquals(terms.get(0).getDescription(), update.getDescription().getValue());
    }

    @Test
    public void testUpdateWithMultipleTerms()
    {
        // TODO
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(new VocabularyTermPermId("DOG", "ORGANISM"));

        VocabularyTermUpdate update2 = new VocabularyTermUpdate();
        update2.setVocabularyTermId(new VocabularyTermPermId("PROPRIETARY", "$STORAGE_FORMAT"));

        v3api.updateVocabularyTerms(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-vocabulary-terms  VOCABULARY_TERM_UPDATES('[VocabularyTermUpdate[vocabularyTermId=DOG (ORGANISM)], VocabularyTermUpdate[vocabularyTermId=PROPRIETARY ($STORAGE_FORMAT)]]')");
    }

    private List<VocabularyTerm> updateTerms(String user, String password, VocabularyTermUpdate... updates)
    {
        String sessionToken = v3api.login(user, password);

        List<IVocabularyTermId> ids = new ArrayList<IVocabularyTermId>();
        for (VocabularyTermUpdate update : updates)
        {
            ids.add(update.getVocabularyTermId());
        }

        v3api.updateVocabularyTerms(sessionToken, Arrays.asList(updates));

        List<VocabularyTerm> terms = searchTerms(ids, new VocabularyTermFetchOptions());

        return terms;
    }

    private VocabularyTermPermId getUnofficialTermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setCode("IAMUNOFFICIAL");
        creation.setVocabularyId(new VocabularyPermId("ORGANISM"));
        creation.setOfficial(false);

        List<VocabularyTermPermId> permIds = v3api.createVocabularyTerms(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    private VocabularyTermPermId getOfficialTermId()
    {
        return new VocabularyTermPermId("DOG", "ORGANISM");
    }

}
