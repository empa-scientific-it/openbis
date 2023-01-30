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

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyUpdate;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class UpdateVocabulariesTest extends AbstractVocabularyTest
{
    @Test
    public void testUpdateDescription()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularyUpdate update = new VocabularyUpdate();
        VocabularyPermId id = new VocabularyPermId("ORGANISM");
        update.setVocabularyId(id);
        update.setDescription("test description");

        // When
        v3api.updateVocabularies(sessionToken, Arrays.asList(update));

        // Then
        VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        Vocabulary vocabulary = v3api.getVocabularies(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(vocabulary.getDescription(), update.getDescription().getValue());
        assertEquals(vocabulary.getPermId(), id);
        assertEquals(vocabulary.getCode(), id.getPermId());
        assertEquals(vocabulary.isChosenFromList(), true);
        assertEquals(vocabulary.getUrlTemplate(), null);

        v3api.logout(sessionToken);
    }

    @Test
    public void testUpdateAll()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularyUpdate update = new VocabularyUpdate();
        VocabularyPermId id = new VocabularyPermId("organism");
        update.setVocabularyId(id);
        update.setDescription("test description");
        update.setChosenFromList(false);
        update.setUrlTemplate("https://www.ethz.ch");

        // When
        v3api.updateVocabularies(sessionToken, Arrays.asList(update));

        // Then
        VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        Vocabulary vocabulary = v3api.getVocabularies(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(vocabulary.getDescription(), update.getDescription().getValue());
        assertEquals(vocabulary.getPermId(), id);
        assertEquals(vocabulary.getCode(), id.getPermId());
        assertEquals(vocabulary.isChosenFromList(), false);
        assertEquals(vocabulary.getUrlTemplate(), update.getUrlTemplate().getValue());

        v3api.logout(sessionToken);
    }

    @Test
    public void testUpdateInternal()
    {
        // Given
        String sessionToken = v3api.loginAsSystem();
        VocabularyUpdate update = new VocabularyUpdate();
        VocabularyPermId id = new VocabularyPermId("$PLATE_GEOMETRY");
        update.setVocabularyId(id);
        update.setDescription("test internal");

        // When
        v3api.updateVocabularies(sessionToken, Arrays.asList(update));

        // Then
        Vocabulary vocabulary = v3api.getVocabularies(sessionToken, Arrays.asList(id), new VocabularyFetchOptions()).get(id);
        assertEquals(vocabulary.getDescription(), update.getDescription().getValue());
        assertEquals(vocabulary.isManagedInternally(), true);

        v3api.logout(sessionToken);
    }

    @Test
    public void testUpdateVocabularyWithMissingId()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularyUpdate update = new VocabularyUpdate();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    v3api.updateVocabularies(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Vocabulary id cannot be null.");

        v3api.logout(sessionToken);
    }

    @Test
    public void testUpdateVocabularyWithUnknownId()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        VocabularyUpdate update = new VocabularyUpdate();
        update.setVocabularyId(new VocabularyPermId("unknown"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    v3api.updateVocabularies(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "[UNKNOWN] has not been found.");

        v3api.logout(sessionToken);
    }

    @Test(dataProvider = PROVIDE_USERS_NOT_ALLOWED_TO_MANAGE_VOCABULARIES)
    public void testUpdateWithUserCausingAuthorizationFailure(final String user)
    {
        VocabularyPermId vocabularyId = new VocabularyPermId("ORGANISM");
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    VocabularyUpdate update = new VocabularyUpdate();
                    update.setVocabularyId(vocabularyId);
                    v3api.updateVocabularies(sessionToken, Arrays.asList(update));
                }
            }, vocabularyId);
    }

    @Test(dataProvider = PROVIDE_USERS_NOT_ALLOWED_TO_MANAGE_INTERNAL_VOCABULARIES)
    public void testUpdateInternalWithUserCausingAuthorizationFailure(final String user)
    {
        VocabularyPermId vocabularyId = new VocabularyPermId("$PLATE_GEOMETRY");
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    VocabularyUpdate update = new VocabularyUpdate();
                    update.setVocabularyId(vocabularyId);
                    v3api.updateVocabularies(sessionToken, Arrays.asList(update));
                }
            }, vocabularyId);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.loginAsSystem();

        VocabularyUpdate update = new VocabularyUpdate();
        update.setVocabularyId(new VocabularyPermId("$PLATE_GEOMETRY"));

        VocabularyUpdate update2 = new VocabularyUpdate();
        update2.setVocabularyId(new VocabularyPermId("ORGANISM"));

        v3api.updateVocabularies(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-vocabularies  VOCABULARY_UPDATES('[VocabularyUpdate[vocabularyId=$PLATE_GEOMETRY], VocabularyUpdate[vocabularyId=ORGANISM]]')");
    }

}
