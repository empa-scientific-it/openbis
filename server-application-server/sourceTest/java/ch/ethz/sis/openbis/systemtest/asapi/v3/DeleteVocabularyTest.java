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
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class DeleteVocabularyTest extends AbstractVocabularyTest
{
    @Test
    public void testDeleteUnusedVocabulary()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularyDeletionOptions deletionOptions = new VocabularyDeletionOptions();
        deletionOptions.setReason("test");
        VocabularyPermId id = new VocabularyPermId("HUMAN");

        // When
        v3api.deleteVocabularies(sessionToken, Arrays.asList(id), deletionOptions);

        // Then
        Map<IVocabularyId, Vocabulary> result = v3api.getVocabularies(sessionToken, Arrays.asList(id), new VocabularyFetchOptions());
        assertEquals(result.size(), 0);

        v3api.logout(sessionToken);
    }

    @Test
    public void testDeleteInternalVocabulary()
    {
        // Given
        String sessionToken = v3api.loginAsSystem();

        VocabularyCreation creation = new VocabularyCreation();
        creation.setCode("I_AM_INTERNAL");
        creation.setManagedInternally(true);

        VocabularyPermId id = v3api.createVocabularies(sessionToken, Arrays.asList(creation)).get(0);

        VocabularyDeletionOptions deletionOptions = new VocabularyDeletionOptions();
        deletionOptions.setReason("test");

        // When
        v3api.deleteVocabularies(sessionToken, Arrays.asList(id), deletionOptions);

        // Then
        Map<IVocabularyId, Vocabulary> result = v3api.getVocabularies(sessionToken, Arrays.asList(id), new VocabularyFetchOptions());
        assertEquals(result.size(), 0);
        assertNotNull(id);

        v3api.logout(sessionToken);
    }

    @Test
    public void testDeleteUsedVocabulary()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularyDeletionOptions deletionOptions = new VocabularyDeletionOptions();
        deletionOptions.setReason("test");
        VocabularyPermId id = new VocabularyPermId("GENDER");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    v3api.deleteVocabularies(sessionToken, Arrays.asList(id), deletionOptions);
                }
            },
                // Then
                "Vocabulary 'GENDER' is being used. Delete all connected data  first.");
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Deletion reason cannot be null.*")
    public void testDeleteWithoutReason()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularyDeletionOptions deletionOptions = new VocabularyDeletionOptions();
        VocabularyPermId id = new VocabularyPermId("HUMAN");

        // When
        v3api.deleteVocabularies(sessionToken, Arrays.asList(id), deletionOptions);

        // Then, see method annotation
    }

    @Test(dataProvider = PROVIDE_USERS_NOT_ALLOWED_TO_MANAGE_VOCABULARIES)
    public void testDeleteWithUserCausingAuthorizationFailure(final String user)
    {
        VocabularyPermId vocabularyPermId = new VocabularyPermId("HUMAN");
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    VocabularyDeletionOptions deletionOptions = new VocabularyDeletionOptions();
                    deletionOptions.setReason("test");
                    v3api.deleteVocabularies(sessionToken, Arrays.asList(vocabularyPermId), deletionOptions);
                }
            }, vocabularyPermId);
    }

    @Test(dataProvider = PROVIDE_USERS_NOT_ALLOWED_TO_MANAGE_INTERNAL_VOCABULARIES)
    public void testDeleteInternalWithUserCausingAuthorizationFailure(final String user)
    {
        VocabularyPermId vocabularyPermId = new VocabularyPermId("$PLATE_GEOMETRY");
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    VocabularyDeletionOptions deletionOptions = new VocabularyDeletionOptions();
                    deletionOptions.setReason("test");
                    v3api.deleteVocabularies(sessionToken, Arrays.asList(vocabularyPermId), deletionOptions);
                }
            }, vocabularyPermId);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        VocabularyDeletionOptions o = new VocabularyDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteVocabularies(sessionToken, Arrays.asList(new VocabularyPermId("TEST-LOGGING-1"), new VocabularyPermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-vocabularies  VOCABULARY_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('VocabularyDeletionOptions[reason=test-reason]')");
    }

}
