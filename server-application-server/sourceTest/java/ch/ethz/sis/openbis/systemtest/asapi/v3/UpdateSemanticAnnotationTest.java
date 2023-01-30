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
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.update.SemanticAnnotationUpdate;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.SemanticAnnotationDAO;

/**
 * @author pkupczyk
 */
public class UpdateSemanticAnnotationTest extends AbstractTest
{

    private static final String PROVIDE_UPDATES_FOR_SAMPLE_TYPE = "provide_updates_for_sample_type";

    private static final String PROVIDE_UPDATES_FOR_NON_SAMPLE_TYPE = "provide_updates_for_non_sample_type";

    @Test
    public void testUpdateWithInstanceAdmin()
    {
        testUpdateWithUser(TEST_USER);
    }

    @Test
    public void testUpdateWithSpaceAdmin()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testUpdateWithUser(TEST_SPACE_USER);
                }
            }, null);
    }

    @Test
    public void testUpdateWithSpaceObserver()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testUpdateWithUser(TEST_GROUP_OBSERVER);
                }
            }, null);
    }

    @Test
    public void testUpdateWithNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISemanticAnnotationId annotationId = new SemanticAnnotationPermId("IDONTEXIST");
        final SemanticAnnotationUpdate update = new SemanticAnnotationUpdate();
        update.setSemanticAnnotationId(annotationId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSemanticAnnotations(sessionToken, Arrays.asList(update));
                }
            }, annotationId);
    }

    @Test
    public void testUpdateWithAllFields()
    {
        String hash = UUID.randomUUID().toString();

        SemanticAnnotationUpdate update = new SemanticAnnotationUpdate();
        update.setSemanticAnnotationId(new SemanticAnnotationPermId("ST_MASTER_PLATE"));
        update.setPredicateOntologyId("predicate_ontology_id_" + hash);
        update.setPredicateOntologyVersion("predicate_ontology_version_" + hash);
        update.setPredicateAccessionId("predicate_accession_id_" + hash);
        update.setDescriptorOntologyId("descriptor_ontology_id_" + hash);
        update.setDescriptorOntologyVersion("descriptor_ontology_version_" + hash);
        update.setDescriptorAccessionId("descriptor_accession_id_" + hash);

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        v3api.updateSemanticAnnotations(sessionToken, Arrays.asList(update));

        Map<ISemanticAnnotationId, SemanticAnnotation> annotations =
                v3api.getSemanticAnnotations(sessionToken, Arrays.asList(update.getSemanticAnnotationId()), new SemanticAnnotationFetchOptions());

        SemanticAnnotation annotation = annotations.get(update.getSemanticAnnotationId());

        assertEquals(annotation.getPredicateOntologyId(), update.getPredicateOntologyId().getValue());
        assertEquals(annotation.getPredicateOntologyVersion(), update.getPredicateOntologyVersion().getValue());
        assertEquals(annotation.getPredicateAccessionId(), update.getPredicateAccessionId().getValue());
        assertEquals(annotation.getDescriptorOntologyId(), update.getDescriptorOntologyId().getValue());
        assertEquals(annotation.getDescriptorOntologyVersion(), update.getDescriptorOntologyVersion().getValue());
        assertEquals(annotation.getDescriptorAccessionId(), update.getDescriptorAccessionId().getValue());
    }

    @Test(dataProvider = PROVIDE_UPDATES_FOR_SAMPLE_TYPE)
    public void testUpdateWithFieldsForSampleType(SemanticAnnotationUpdate update, String expectedError)
    {
        update.setSemanticAnnotationId(new SemanticAnnotationPermId("ST_MASTER_PLATE"));
        testUpdateWithFields(update, expectedError);
    }

    @Test(dataProvider = PROVIDE_UPDATES_FOR_NON_SAMPLE_TYPE)
    public void testUpdateWithFieldsForPropertyType(SemanticAnnotationUpdate update, String expectedError)
    {
        update.setSemanticAnnotationId(new SemanticAnnotationPermId("PT_DESCRIPTION"));
        testUpdateWithFields(update, expectedError);
    }

    @Test(dataProvider = PROVIDE_UPDATES_FOR_NON_SAMPLE_TYPE)
    public void testUpdateWithFieldsForSampleTypePropertyType(SemanticAnnotationUpdate update, String expectedError)
    {
        update.setSemanticAnnotationId(new SemanticAnnotationPermId("ST_CELL_PLATE_PT_ORGANISM"));
        testUpdateWithFields(update, expectedError);
    }

    @SuppressWarnings("null")
    private void testUpdateWithFields(SemanticAnnotationUpdate update, String expectedError)
    {
        Exception exception = null;

        try
        {
            final String sessionToken = v3api.login(TEST_USER, PASSWORD);
            v3api.updateSemanticAnnotations(sessionToken, Arrays.asList(update));
        } catch (Exception e)
        {
            exception = e;
        }

        if (expectedError == null)
        {
            assertNull(exception);
        } else
        {
            AssertionUtil.assertStarts(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testUpdate()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SemanticAnnotationPermId permId1 = new SemanticAnnotationPermId("ST_MASTER_PLATE");
        final SemanticAnnotationPermId permId2 = new SemanticAnnotationPermId("ST_DILUTION_PLATE");

        Map<ISemanticAnnotationId, SemanticAnnotation> map =
                v3api.getSemanticAnnotations(sessionToken, Arrays.asList(permId1, permId2), new SemanticAnnotationFetchOptions());

        SemanticAnnotation before1 = map.get(permId1);
        SemanticAnnotation before2 = map.get(permId2);

        assertEquals(before1.getPermId(), permId1);
        assertEquals(before1.getPredicateOntologyId(), "testPredicateOntologyId1");
        assertEquals(before1.getPredicateOntologyVersion(), "testPredicateOntologyVersion1");
        assertEquals(before1.getPredicateAccessionId(), "testPredicateAccessionId1");
        assertEquals(before1.getDescriptorOntologyId(), "testDescriptorOntologyId1");
        assertEquals(before1.getDescriptorOntologyVersion(), "testDescriptorOntologyVersion1");
        assertEquals(before1.getDescriptorAccessionId(), "testDescriptorAccessionId1");

        assertEquals(before2.getPermId(), permId2);
        assertEquals(before2.getPredicateOntologyId(), "testPredicateOntologyId2");
        assertEquals(before2.getPredicateOntologyVersion(), "testPredicateOntologyVersion2");
        assertEquals(before2.getPredicateAccessionId(), "testPredicateAccessionId2");
        assertEquals(before2.getDescriptorOntologyId(), "testDescriptorOntologyId2");
        assertEquals(before2.getDescriptorOntologyVersion(), "testDescriptorOntologyVersion2");
        assertEquals(before2.getDescriptorAccessionId(), "testDescriptorAccessionId2");

        final SemanticAnnotationUpdate update1 = new SemanticAnnotationUpdate();
        update1.setSemanticAnnotationId(permId1);
        update1.setPredicateOntologyId("updatedPredicateOntologyId1");
        update1.setPredicateOntologyVersion("updatedPredicateOntologyVersion1");
        update1.setPredicateAccessionId("updatedPredicateAccessionId1");
        update1.setDescriptorOntologyId("updatedDescriptorOntologyId1");
        update1.setDescriptorOntologyVersion("updatedDescriptorOntologyVersion1");
        update1.setDescriptorAccessionId("updatedDescriptorAccessionId1");

        final SemanticAnnotationUpdate update2 = new SemanticAnnotationUpdate();
        update2.setSemanticAnnotationId(permId2);
        update2.setPredicateOntologyId("updatedPredicateOntologyId2");
        update2.setDescriptorOntologyId("updatedDescriptorOntologyId2");

        v3api.updateSemanticAnnotations(sessionToken, Arrays.asList(update1, update2));
        map = v3api.getSemanticAnnotations(sessionToken, Arrays.asList(permId1, permId2), new SemanticAnnotationFetchOptions());

        SemanticAnnotation after1 = map.get(permId1);
        SemanticAnnotation after2 = map.get(permId2);

        assertEquals(after1.getPermId(), permId1);
        assertEquals(after1.getPredicateOntologyId(), update1.getPredicateOntologyId().getValue());
        assertEquals(after1.getPredicateOntologyVersion(), update1.getPredicateOntologyVersion().getValue());
        assertEquals(after1.getPredicateAccessionId(), update1.getPredicateAccessionId().getValue());
        assertEquals(after1.getDescriptorOntologyId(), update1.getDescriptorOntologyId().getValue());
        assertEquals(after1.getDescriptorOntologyVersion(), update1.getDescriptorOntologyVersion().getValue());
        assertEquals(after1.getDescriptorAccessionId(), update1.getDescriptorAccessionId().getValue());

        assertEquals(after2.getPermId(), permId2);
        assertEquals(after2.getPredicateOntologyId(), update2.getPredicateOntologyId().getValue());
        assertEquals(after2.getPredicateOntologyVersion(), before2.getPredicateOntologyVersion());
        assertEquals(after2.getPredicateAccessionId(), before2.getPredicateAccessionId());
        assertEquals(after2.getDescriptorOntologyId(), update2.getDescriptorOntologyId().getValue());
        assertEquals(after2.getDescriptorOntologyVersion(), before2.getDescriptorOntologyVersion());
        assertEquals(after2.getDescriptorAccessionId(), before2.getDescriptorAccessionId());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationUpdate update = new SemanticAnnotationUpdate();
        update.setSemanticAnnotationId(new SemanticAnnotationPermId("ST_MASTER_PLATE"));

        SemanticAnnotationUpdate update2 = new SemanticAnnotationUpdate();
        update2.setSemanticAnnotationId(new SemanticAnnotationPermId("ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY"));

        v3api.updateSemanticAnnotations(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-semantic-annotations  SEMANTIC_ANNOTATION_UPDATES('[SemanticAnnotationUpdate[annotationId=ST_MASTER_PLATE], SemanticAnnotationUpdate[annotationId=ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY]]')");
    }

    private void testUpdateWithUser(String userId)
    {
        String sessionToken = v3api.login(userId, PASSWORD);

        SemanticAnnotationUpdate update = new SemanticAnnotationUpdate();
        update.setSemanticAnnotationId(new SemanticAnnotationPermId("ST_MASTER_PLATE"));
        update.setPredicateOntologyId("testPredicateOntologyId1Updated");

        v3api.updateSemanticAnnotations(sessionToken, Arrays.asList(update));
    }

    private static SemanticAnnotationUpdate updateWithPredicateWithDescriptor()
    {
        String hash = UUID.randomUUID().toString();

        SemanticAnnotationUpdate update = new SemanticAnnotationUpdate();
        update.setPredicateOntologyId("predicate_ontology_id_" + hash);
        update.setPredicateOntologyVersion("");
        update.setPredicateAccessionId("predicate_accession_id_" + hash);
        update.setDescriptorOntologyId("descriptor_ontology_id_" + hash);
        update.setDescriptorOntologyVersion("");
        update.setDescriptorAccessionId("descriptor_accession_id_" + hash);

        return update;
    }

    private static SemanticAnnotationUpdate updateWithNullPredicate()
    {
        SemanticAnnotationUpdate update = new SemanticAnnotationUpdate();
        update.setPredicateOntologyId(null);
        update.setPredicateOntologyVersion(null);
        update.setPredicateAccessionId(null);
        return update;
    }

    private static SemanticAnnotationUpdate updateWithNullDescriptor()
    {
        SemanticAnnotationUpdate update = new SemanticAnnotationUpdate();
        update.setDescriptorOntologyId(null);
        update.setDescriptorOntologyVersion(null);
        update.setDescriptorAccessionId(null);
        return update;
    }

    private static SemanticAnnotationUpdate updateWithEmptyPredicate()
    {
        SemanticAnnotationUpdate update = new SemanticAnnotationUpdate();
        update.setPredicateOntologyId("");
        update.setPredicateOntologyVersion("");
        update.setPredicateAccessionId("");
        return update;
    }

    private static SemanticAnnotationUpdate updateWithEmptyDescriptor()
    {
        SemanticAnnotationUpdate update = new SemanticAnnotationUpdate();
        update.setDescriptorOntologyId("");
        update.setDescriptorOntologyVersion("");
        update.setDescriptorAccessionId("");
        return update;
    }

    @DataProvider(name = PROVIDE_UPDATES_FOR_SAMPLE_TYPE)
    public static Object[][] provideUpdatesForSampleType()
    {
        return new Object[][] {
                { updateWithPredicateWithDescriptor(), null },
                { updateWithNullPredicate(), SemanticAnnotationDAO.ERROR_PREDICATE_CANNOT_BE_NULL_OR_EMPTY },
                { updateWithNullDescriptor(), SemanticAnnotationDAO.ERROR_DESCRIPTOR_CANNOT_BE_NULL_OR_EMPTY },
                { updateWithEmptyPredicate(), SemanticAnnotationDAO.ERROR_PREDICATE_CANNOT_BE_NULL_OR_EMPTY },
                { updateWithEmptyDescriptor(), SemanticAnnotationDAO.ERROR_DESCRIPTOR_CANNOT_BE_NULL_OR_EMPTY },
        };
    }

    @DataProvider(name = PROVIDE_UPDATES_FOR_NON_SAMPLE_TYPE)
    public static Object[][] provideUpdatesForNonSampleType()
    {
        return new Object[][] {
                { updateWithPredicateWithDescriptor(), null },
                { updateWithNullPredicate(), SemanticAnnotationDAO.ERROR_PREDICATE_CANNOT_BE_NULL_OR_EMPTY },
                { updateWithNullDescriptor(), null },
                { updateWithEmptyPredicate(), SemanticAnnotationDAO.ERROR_PREDICATE_CANNOT_BE_NULL_OR_EMPTY },
                { updateWithEmptyDescriptor(), SemanticAnnotationDAO.ERROR_DESCRIPTOR_CAN_BE_NULL_OR_NON_EMPTY },
        };
    }

}
