/*
 * Copyright 2015 ETH Zuerich, CISD
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
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.delete.SemanticAnnotationDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class DeleteSemanticAnnotationTest extends AbstractDeletionTest
{

    @Test
    public void testDeleteWithInstanceAdmin()
    {
        testDeleteWithUser(TEST_USER);
    }

    @Test
    public void testDeleteWithSpaceAdmin()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testDeleteWithUser(TEST_SPACE_USER);
                }
            }, null);
    }

    @Test
    public void testDeleteWithSpaceObserver()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testDeleteWithUser(TEST_GROUP_OBSERVER);
                }
            }, null);
    }

    @Test
    public void testDeleteNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationDeletionOptions options = new SemanticAnnotationDeletionOptions();
        options.setReason("It is just a test");

        v3api.deleteSemanticAnnotations(sessionToken, Arrays.asList(new SemanticAnnotationPermId("I_DONT_EXIST")), options);
    }

    @Test
    public void testDeleteEmptyList()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationDeletionOptions options = new SemanticAnnotationDeletionOptions();
        options.setReason("It is just a test");

        v3api.deleteSemanticAnnotations(sessionToken, new ArrayList<SemanticAnnotationPermId>(), options);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Entity ids cannot be null.*")
    public void testDeleteWithNullIds()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationDeletionOptions options = new SemanticAnnotationDeletionOptions();
        options.setReason("It is just a test");

        v3api.deleteSemanticAnnotations(sessionToken, null, options);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Deletion options cannot be null.*")
    public void testDeleteWithNullOptions()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        v3api.deleteSemanticAnnotations(sessionToken, Arrays.asList(new SemanticAnnotationPermId("ST_MASTER_PLATE")), null);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Deletion reason cannot be null.*")
    public void testDeleteWithNullReason()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationDeletionOptions options = new SemanticAnnotationDeletionOptions();

        v3api.deleteSemanticAnnotations(sessionToken, Arrays.asList(new SemanticAnnotationPermId("ST_MASTER_PLATE")), options);
    }

    @Test
    public void testDelete()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationPermId id = new SemanticAnnotationPermId("ST_MASTER_PLATE");
        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();

        Map<ISemanticAnnotationId, SemanticAnnotation> beforeMap = v3api.getSemanticAnnotations(sessionToken, Arrays.asList(id), fo);
        assertEquals(beforeMap.size(), 1);

        SemanticAnnotationDeletionOptions options = new SemanticAnnotationDeletionOptions();
        options.setReason("It is just a test");

        v3api.deleteSemanticAnnotations(sessionToken, Arrays.asList(id), options);

        Map<ISemanticAnnotationId, SemanticAnnotation> afterMap = v3api.getSemanticAnnotations(sessionToken, Arrays.asList(id), fo);
        assertEquals(afterMap.size(), 0);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationDeletionOptions o = new SemanticAnnotationDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteSemanticAnnotations(sessionToken,
                Arrays.asList(new SemanticAnnotationPermId("TEST-LOGGING-1"), new SemanticAnnotationPermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-semantic-annotations  SEMANTIC_ANNOTATION_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('SemanticAnnotationDeletionOptions[reason=test-reason]')");
    }

    private void testDeleteWithUser(String user)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SemanticAnnotationDeletionOptions options = new SemanticAnnotationDeletionOptions();
        options.setReason("It is just a test");

        v3api.deleteSemanticAnnotations(sessionToken, Arrays.asList(new SemanticAnnotationPermId("ST_MASTER_PLATE")), options);
    }

}
