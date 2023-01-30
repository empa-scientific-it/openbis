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
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.SemanticAnnotationDAO;

/**
 * @author pkupczyk
 */
public class CreateSemanticAnnotationTest extends AbstractTest
{

    private static final String PROVIDE_CREATIONS_FOR_SAMPLE_TYPE = "provide_creations_for_sample_type";

    private static final String PROVIDE_CREATIONS_FOR_NON_SAMPLE_TYPE = "provide_creations_for_non_sample_type";

    @Test
    public void testCreateWithInstanceAdmin()
    {
        testCreateWithUser(TEST_USER);
    }

    @Test
    public void testCreateWithSpaceAdmin()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreateWithUser(TEST_SPACE_USER);
                }
            }, null);
    }

    @Test
    public void testCreateWithSpaceObserver()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreateWithUser(TEST_GROUP_OBSERVER);
                }
            }, null);
    }

    @Test
    public void testCreateWithAllFields()
    {
        String hash = UUID.randomUUID().toString();

        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setEntityTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));
        creation.setPredicateOntologyId("predicate_ontology_id_" + hash);
        creation.setPredicateOntologyVersion("predicate_ontology_version_" + hash);
        creation.setPredicateAccessionId("predicate_accession_id_" + hash);
        creation.setDescriptorOntologyId("descriptor_ontology_id_" + hash);
        creation.setDescriptorOntologyVersion("descriptor_ontology_version_" + hash);
        creation.setDescriptorAccessionId("descriptor_accession_id_" + hash);

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withEntityType();

        SemanticAnnotation annotation = testCreate(TEST_USER, creation, fo);

        assertEquals(annotation.getEntityType().getPermId(), creation.getEntityTypeId());
        assertEquals(annotation.getPredicateOntologyId(), creation.getPredicateOntologyId());
        assertEquals(annotation.getPredicateOntologyVersion(), creation.getPredicateOntologyVersion());
        assertEquals(annotation.getPredicateAccessionId(), creation.getPredicateAccessionId());
        assertEquals(annotation.getDescriptorOntologyId(), creation.getDescriptorOntologyId());
        assertEquals(annotation.getDescriptorOntologyVersion(), creation.getDescriptorOntologyVersion());
        assertEquals(annotation.getDescriptorAccessionId(), creation.getDescriptorAccessionId());
    }

    @Test(dataProvider = PROVIDE_CREATIONS_FOR_SAMPLE_TYPE)
    public void testCreateWithFieldsForSampleType(SemanticAnnotationCreation creation, String expectedError)
    {
        creation.setEntityTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));
        testCreateWithFields(creation, expectedError);
    }

    @Test(dataProvider = PROVIDE_CREATIONS_FOR_NON_SAMPLE_TYPE)
    public void testCreateWithFieldsForPropertyType(SemanticAnnotationCreation creation, String expectedError)
    {
        creation.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));
        testCreateWithFields(creation, expectedError);
    }

    @Test(dataProvider = PROVIDE_CREATIONS_FOR_NON_SAMPLE_TYPE)
    public void testCreateWithFieldsForSampleTypePropertyType(SemanticAnnotationCreation creation, String expectedError)
    {
        creation.setPropertyAssignmentId(
                new PropertyAssignmentPermId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE), new PropertyTypePermId("COMMENT")));
        testCreateWithFields(creation, expectedError);
    }

    @SuppressWarnings("null")
    private void testCreateWithFields(SemanticAnnotationCreation creation, String expectedError)
    {
        Exception exception = null;

        try
        {
            testCreate(TEST_USER, creation, new SemanticAnnotationFetchOptions());
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
    public void testCreateWithNoOwner()
    {
        SemanticAnnotationCreation creation = creationWithPredicateWithDescriptor();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, new SemanticAnnotationFetchOptions());
                }
            }, SemanticAnnotationDAO.ERROR_OWNER_CANNOT_BE_NULL_OR_MORE_THAN_ONE);
    }

    @Test
    public void testCreateWithMultipleOwners()
    {
        SemanticAnnotationCreation creation = creationWithPredicateWithDescriptor();
        creation.setEntityTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));
        creation.setPropertyTypeId(new PropertyTypePermId("COMMENT"));
        creation.setPropertyAssignmentId(
                new PropertyAssignmentPermId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE), new PropertyTypePermId("COMMENT")));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, new SemanticAnnotationFetchOptions());
                }
            }, SemanticAnnotationDAO.ERROR_OWNER_CANNOT_BE_NULL_OR_MORE_THAN_ONE);
    }

    @Test
    public void testCreateWithSampleType()
    {
        SemanticAnnotationCreation creation = creationWithPredicateWithDescriptor();
        creation.setEntityTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withEntityType();

        SemanticAnnotation annotation = testCreate(TEST_USER, creation, fo);
        assertEquals(annotation.getEntityType().getPermId(), creation.getEntityTypeId());
    }

    @Test
    public void testCreateWithSampleTypeNonexistent()
    {
        SemanticAnnotationCreation creation = creationWithPredicateWithDescriptor();
        creation.setEntityTypeId(new EntityTypePermId("I_DONT_EXIST", EntityKind.SAMPLE));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, new SemanticAnnotationFetchOptions());
                }
            }, creation.getEntityTypeId());
    }

    @Test
    public void testCreateWithNonSampleType()
    {
        SemanticAnnotationCreation creation = creationWithPredicateWithDescriptor();
        creation.setEntityTypeId(new EntityTypePermId("SIRNA_HCS", EntityKind.EXPERIMENT));

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withEntityType();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, fo);
                }
            }, "Semantic annotations can be defined for sample entity types only");
    }

    @Test
    public void testCreateWithPropertyType()
    {
        SemanticAnnotationCreation creation = creationWithPredicateWithDescriptor();
        creation.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withPropertyType();

        SemanticAnnotation annotation = testCreate(TEST_USER, creation, fo);
        assertEquals(annotation.getPropertyType().getPermId(), creation.getPropertyTypeId());
    }

    @Test
    public void testCreateWithPropertyTypeNonexistent()
    {
        SemanticAnnotationCreation creation = creationWithPredicateWithDescriptor();
        creation.setPropertyTypeId(new PropertyTypePermId("I_DONT_EXIST"));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, new SemanticAnnotationFetchOptions());
                }
            }, creation.getPropertyTypeId());
    }

    @Test
    public void testCreateWithSampleTypePropertyType()
    {
        SemanticAnnotationCreation creation = creationWithPredicateWithDescriptor();
        creation.setPropertyAssignmentId(
                new PropertyAssignmentPermId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE), new PropertyTypePermId("COMMENT")));

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withPropertyAssignment();

        SemanticAnnotation annotation = testCreate(TEST_USER, creation, fo);
        assertEquals(annotation.getPropertyAssignment().getPermId(), creation.getPropertyAssignmentId());
    }

    @Test
    public void testCreateWithSampleTypePropertyTypeNonexistent()
    {
        SemanticAnnotationCreation creation = creationWithPredicateWithDescriptor();
        creation.setPropertyAssignmentId(
                new PropertyAssignmentPermId(new EntityTypePermId("I_DONT_EXIST", EntityKind.SAMPLE), new PropertyTypePermId("I_DONT_EXIST")));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, new SemanticAnnotationFetchOptions());
                }
            }, creation.getPropertyAssignmentId());
    }

    @Test
    public void testCreateWithNonSampleTypePropertyType()
    {
        SemanticAnnotationCreation creation = creationWithPredicateWithDescriptor();
        creation.setPropertyAssignmentId(
                new PropertyAssignmentPermId(new EntityTypePermId("SIRNA_HCS", EntityKind.EXPERIMENT), new PropertyTypePermId("DESCRIPTION")));

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withPropertyAssignment();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    testCreate(TEST_USER, creation, fo);
                }
            }, "Semantic annotations can be defined for sample property assignments only");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setEntityTypeId(new EntityTypePermId("CONTROL_LAYOUT", EntityKind.SAMPLE));
        creation.setPredicateOntologyId("p_oid_1");
        creation.setPredicateOntologyVersion("p_ver_1");
        creation.setPredicateAccessionId("p_aid_1");
        creation.setDescriptorOntologyId("d_oid_1");
        creation.setDescriptorOntologyVersion("d_ver_1");
        creation.setDescriptorAccessionId("d_aid_1");

        SemanticAnnotationCreation creation2 = new SemanticAnnotationCreation();
        creation2.setPropertyTypeId(new PropertyTypePermId("DESCRIPTION"));
        creation2.setPredicateOntologyId("p_oid_2");
        creation2.setPredicateOntologyVersion("p_ver_2");
        creation2.setPredicateAccessionId("p_aid_2");
        creation2.setDescriptorOntologyId("d_oid_2");
        creation2.setDescriptorOntologyVersion("d_ver_2");
        creation2.setDescriptorAccessionId("d_aid_2");

        v3api.createSemanticAnnotations(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-semantic-annotations  NEW_SEMANTIC_ANNOTATIONS('["
                        + "SemanticAnnotationCreation[entityTypeId=CONTROL_LAYOUT (SAMPLE),propertyTypeId=<null>,propertyAssignmentId=<null>,predicateOntologyId=p_oid_1,predicateOntologyVersion=p_ver_1,predicateAccessionId=p_aid_1,descriptorOntologyId=d_oid_1,descriptorOntologyVersion=d_ver_1,descriptorAccessionId=d_aid_1], "
                        + "SemanticAnnotationCreation[entityTypeId=<null>,propertyTypeId=DESCRIPTION,propertyAssignmentId=<null>,predicateOntologyId=p_oid_2,predicateOntologyVersion=p_ver_2,predicateAccessionId=p_aid_2,descriptorOntologyId=d_oid_2,descriptorOntologyVersion=d_ver_2,descriptorAccessionId=d_aid_2]"
                        + "]')");
    }

    private void testCreateWithUser(String userId)
    {
        SemanticAnnotationCreation creation = creationWithPredicateWithDescriptor();
        creation.setEntityTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));

        SemanticAnnotation annotation = testCreate(userId, creation, new SemanticAnnotationFetchOptions());

        assertEquals(annotation.getDescriptorOntologyId(), creation.getDescriptorOntologyId());
    }

    private SemanticAnnotation testCreate(String userId, SemanticAnnotationCreation creation, SemanticAnnotationFetchOptions fo)
    {
        final String sessionToken = v3api.login(userId, PASSWORD);

        List<SemanticAnnotationPermId> permIds = v3api.createSemanticAnnotations(sessionToken, Arrays.asList(creation));

        Map<ISemanticAnnotationId, SemanticAnnotation> map =
                v3api.getSemanticAnnotations(sessionToken, permIds, fo);

        assertEquals(map.size(), 1);

        return map.values().iterator().next();
    }

    private static SemanticAnnotationCreation creationWithPredicateWithDescriptor()
    {
        String hash = UUID.randomUUID().toString();

        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setPredicateOntologyId("predicate_ontology_id_" + hash);
        creation.setPredicateOntologyVersion("");
        creation.setPredicateAccessionId("predicate_accession_id_" + hash);
        creation.setDescriptorOntologyId("descriptor_ontology_id_" + hash);
        creation.setDescriptorOntologyVersion("");
        creation.setDescriptorAccessionId("descriptor_accession_id_" + hash);

        return creation;
    }

    private static SemanticAnnotationCreation creationWithNullPredicate()
    {
        String hash = UUID.randomUUID().toString();

        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setDescriptorOntologyId("descriptor_ontology_id_" + hash);
        creation.setDescriptorOntologyVersion("");
        creation.setDescriptorAccessionId("descriptor_accession_id_" + hash);

        return creation;
    }

    private static SemanticAnnotationCreation creationWithNullDescriptor()
    {
        String hash = UUID.randomUUID().toString();

        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setPredicateOntologyId("predicate_ontology_id_" + hash);
        creation.setPredicateOntologyVersion("");
        creation.setPredicateAccessionId("predicate_accession_id_" + hash);

        return creation;
    }

    private static SemanticAnnotationCreation creationWithEmptyPredicate()
    {
        String hash = UUID.randomUUID().toString();

        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setPredicateOntologyId("");
        creation.setPredicateOntologyVersion("");
        creation.setPredicateAccessionId("");
        creation.setDescriptorOntologyId("descriptor_ontology_id_" + hash);
        creation.setDescriptorOntologyVersion("");
        creation.setDescriptorAccessionId("descriptor_accession_id_" + hash);

        return creation;
    }

    private static SemanticAnnotationCreation creationWithEmptyDescriptor()
    {
        String hash = UUID.randomUUID().toString();

        SemanticAnnotationCreation creation = new SemanticAnnotationCreation();
        creation.setPredicateOntologyId("predicate_ontology_id_" + hash);
        creation.setPredicateOntologyVersion("");
        creation.setPredicateAccessionId("predicate_accession_id_" + hash);
        creation.setDescriptorOntologyId("");
        creation.setDescriptorOntologyVersion("");
        creation.setDescriptorAccessionId("");

        return creation;
    }

    @DataProvider(name = PROVIDE_CREATIONS_FOR_SAMPLE_TYPE)
    public static Object[][] provideCreationsForSampleType()
    {
        return new Object[][] {
                { creationWithPredicateWithDescriptor(), null },
                { creationWithNullPredicate(), SemanticAnnotationDAO.ERROR_PREDICATE_CANNOT_BE_NULL_OR_EMPTY },
                { creationWithNullDescriptor(), SemanticAnnotationDAO.ERROR_DESCRIPTOR_CANNOT_BE_NULL_OR_EMPTY },
                { creationWithEmptyPredicate(), SemanticAnnotationDAO.ERROR_PREDICATE_CANNOT_BE_NULL_OR_EMPTY },
                { creationWithEmptyDescriptor(), SemanticAnnotationDAO.ERROR_DESCRIPTOR_CANNOT_BE_NULL_OR_EMPTY },
        };
    }

    @DataProvider(name = PROVIDE_CREATIONS_FOR_NON_SAMPLE_TYPE)
    public static Object[][] provideCreationsForNonSampleType()
    {
        return new Object[][] {
                { creationWithPredicateWithDescriptor(), null },
                { creationWithNullPredicate(), SemanticAnnotationDAO.ERROR_PREDICATE_CANNOT_BE_NULL_OR_EMPTY },
                { creationWithNullDescriptor(), null },
                { creationWithEmptyPredicate(), SemanticAnnotationDAO.ERROR_PREDICATE_CANNOT_BE_NULL_OR_EMPTY },
                { creationWithEmptyDescriptor(), SemanticAnnotationDAO.ERROR_DESCRIPTOR_CAN_BE_NULL_OR_NON_EMPTY },
        };
    }

}
