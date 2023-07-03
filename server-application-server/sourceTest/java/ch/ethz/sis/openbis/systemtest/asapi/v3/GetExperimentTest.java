/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.history.ExperimentRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class GetExperimentTest extends AbstractExperimentTest
{

    @Test
    public void testGetByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId permId1 = new ExperimentPermId("200811050951882-1028");
        ExperimentPermId permId2 = new ExperimentPermId("200811050952663-1029");

        Map<IExperimentId, Experiment> map =
                v3api.getExperiments(sessionToken, Arrays.asList(permId1, permId2),
                        new ExperimentFetchOptions());

        assertEquals(2, map.size());

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NOE/EXP-TEST-2");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/TEST-SPACE/NOE/EXP-TEST-2");
        ExperimentIdentifier identifier3 = new ExperimentIdentifier("/CISD/NEMO/EXP10");

        Map<IExperimentId, Experiment> map =
                v3api.getExperiments(sessionToken,
                        Arrays.asList(identifier1, identifier2, identifier3),
                        new ExperimentFetchOptions());

        assertEquals(3, map.size());

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getIdentifier(), identifier3);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);
        assertEquals(map.get(identifier3).getIdentifier(), identifier3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdentifierCaseInsensitive()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/cisD/Noe/EXP-test-2");

        Map<IExperimentId, Experiment> map =
                v3api.getExperiments(sessionToken,
                        Arrays.asList(identifier1),
                        new ExperimentFetchOptions());

        assertEquals(1, map.size());

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);

        assertEquals(map.get(identifier1).getIdentifier().getIdentifier(), "/CISD/NOE/EXP-TEST-2");
        assertEquals(map.get(new ExperimentIdentifier("/CISD/NOE/EXP-TEST-2")).getIdentifier().getIdentifier(), "/CISD/NOE/EXP-TEST-2");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NOE/EXP-TEST-2");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/TEST-SPACE/NOE/EXP-TEST-2");
        ExperimentIdentifier identifier3 = new ExperimentIdentifier("/CISD/NEMO/EXP10");
        ExperimentIdentifier identifier4 = new ExperimentIdentifier("/NONEXISTENT_SPACE/NEMO/EXP1");
        ExperimentIdentifier identifier5 = new ExperimentIdentifier("/CISD/NONEXISTENT_PROJECT/EXP1");
        ExperimentIdentifier identifier6 = new ExperimentIdentifier("/CISD/NEMO/EXP11");
        ExperimentPermId permId1 = new ExperimentPermId("200811050951882-1028");
        ExperimentPermId permId2 = new ExperimentPermId("200811050952663-1029");
        ExperimentIdentifier identifier7 = new ExperimentIdentifier("/CISD/NONEXISTENT_PROJECT/EXP1");
        ExperimentPermId permId3 = new ExperimentPermId("NONEXISTENT_EXPERIMENT");
        ExperimentIdentifier identifier8 = new ExperimentIdentifier("/CISD/NEMO/NONEXISTENT_EXPERIMENT");

        Map<IExperimentId, Experiment> map =
                v3api.getExperiments(sessionToken,
                        Arrays.asList(identifier1, identifier2, identifier3, identifier4, identifier5, identifier6, permId1, permId2, identifier7,
                                permId3, identifier8),
                        new ExperimentFetchOptions());

        assertEquals(6, map.size());

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getIdentifier(), identifier3);
        assertEquals(iter.next().getIdentifier(), identifier6);
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);
        assertEquals(map.get(identifier3).getIdentifier(), identifier3);
        assertEquals(map.get(identifier6).getIdentifier(), identifier6);
        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDifferent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentPermId permId = new ExperimentPermId("200811050952663-1029");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");

        Map<IExperimentId, Experiment> map =
                v3api.getExperiments(sessionToken, Arrays.asList(identifier1, permId, identifier2), new ExperimentFetchOptions());

        assertEquals(3, map.size());

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getPermId(), permId);
        assertEquals(iter.next().getIdentifier(), identifier2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(permId).getPermId(), permId);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // "/CISD/NEMO/EXP1" and "200811050951882-1028" is the same experiment
        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentPermId permId1 = new ExperimentPermId("200811050951882-1028");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");
        ExperimentPermId permId2 = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map =
                v3api.getExperiments(sessionToken, Arrays.asList(identifier1, permId1, identifier2, permId2), new ExperimentFetchOptions());

        assertEquals(3, map.size());

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getIdentifier(), identifier2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        assertTrue(map.get(identifier1) == map.get(permId1));

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsUnauthorized()
    {
        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        ExperimentIdentifier identifier3 = new ExperimentIdentifier("/CISD/NEMO/EXP11");
        ExperimentIdentifier identifier4 = new ExperimentIdentifier("/TEST-SPACE/NOE/EXP-TEST-2");

        List<? extends IExperimentId> ids = Arrays.asList(identifier1, identifier2, identifier3, identifier4);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, ids, new ExperimentFetchOptions());

        assertEquals(map.size(), 4);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.getExperiments(sessionToken, ids, new ExperimentFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getIdentifier(), identifier4);

        assertEquals(map.get(identifier2).getIdentifier(), identifier2);
        assertEquals(map.get(identifier4).getIdentifier(), identifier4);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithFetchOptionsEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Collections.singletonList(permId), new ExperimentFetchOptions());

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId(), permId);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(experiment.getRegistrationDate()), "2008-11-05 09:21:51");
        assertNotNull(experiment.getModificationDate());

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertAttachmentsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withType();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId().toString(), "200811050951882-1028");
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        ExperimentType type = experiment.getType();
        assertEquals(type.getPermId().getPermId(), "SIRNA_HCS");
        assertEquals(type.getCode(), "SIRNA_HCS");
        assertEquals(type.getDescription(), "Small Interfering RNA High Content Screening");
        assertNotNull(type.getModificationDate());
        assertPropertyAssignmentsNotFetched(type);
        assertEquals(type.getFetchOptions().hasPropertyAssignments(), false);

        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);
        assertAttachmentsNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTypeWithPropertyAssignments()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withType().withPropertyAssignments();
        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        assertEquals(1, map.size());
        Experiment experiment = map.get(permId);
        ExperimentType type = experiment.getType();
        assertEquals(type.getCode(), "SIRNA_HCS");

        assertEquals(type.getFetchOptions().hasPropertyAssignments(), true);
        List<PropertyAssignment> propertyAssignments = type.getPropertyAssignments();
        assertEquals(propertyAssignments.size(), 3);

        // DESCRIPTION
        PropertyAssignment propertyAssignment0 = propertyAssignments.get(0);
        assertEquals(propertyAssignment0.getOrdinal(), Integer.valueOf(1));
        assertEquals(propertyAssignment0.isMandatory(), Boolean.TRUE);
        assertEquals(propertyAssignment0.isShowInEditView(), Boolean.TRUE);
        assertEquals(propertyAssignment0.isShowRawValueInForms(), Boolean.FALSE);
        assertEqualsDate(propertyAssignment0.getRegistrationDate(), "2008-11-05 09:18:00");

        // PURCHASE_DATE
        PropertyAssignment propertyAssignment1 = propertyAssignments.get(1);
        assertEquals(propertyAssignment1.getOrdinal(), Integer.valueOf(2));
        assertEquals(propertyAssignment1.isMandatory(), Boolean.FALSE);
        assertEquals(propertyAssignment1.isShowInEditView(), Boolean.TRUE);
        assertEquals(propertyAssignment1.isShowRawValueInForms(), Boolean.FALSE);
        assertEqualsDate(propertyAssignment1.getRegistrationDate(), "2008-11-05 09:18:24");

        // GENDER
        PropertyAssignment propertyAssignment2 = propertyAssignments.get(2);
        assertEquals(propertyAssignment2.getOrdinal(), Integer.valueOf(3));
        assertEquals(propertyAssignment2.isMandatory(), Boolean.FALSE);
        assertEquals(propertyAssignment2.isShowInEditView(), Boolean.TRUE);
        assertEquals(propertyAssignment2.isShowRawValueInForms(), Boolean.FALSE);
        assertEqualsDate(propertyAssignment2.getRegistrationDate(), "2008-11-05 09:21:53");

        for (PropertyAssignment propertyAssignment : propertyAssignments)
        {
            assertPropertyTypeNotFetched(propertyAssignment);
            assertRegistratorNotFetched(propertyAssignment);
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTypeWithPropertyAssignmentsWithImplicitPropertyTypeFromSorting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withType().withPropertyAssignments().sortBy().code().desc();
        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        assertEquals(1, map.size());
        Experiment experiment = map.get(permId);
        ExperimentType type = experiment.getType();
        assertEquals(type.getCode(), "SIRNA_HCS");

        assertEquals(type.getFetchOptions().hasPropertyAssignments(), true);
        List<PropertyAssignment> propertyAssignments = type.getPropertyAssignments();
        assertEquals(propertyAssignments.size(), 3);

        assertEquals(propertyAssignments.get(0).getPropertyType().getCode(), "PURCHASE_DATE");
        assertEquals(propertyAssignments.get(1).getPropertyType().getCode(), "GENDER");
        assertEquals(propertyAssignments.get(2).getPropertyType().getCode(), "DESCRIPTION");

        for (PropertyAssignment propertyAssignment : propertyAssignments)
        {
            assertRegistratorNotFetched(propertyAssignment);
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTypeWithPropertyAssignmentsWithExplicitPropertyType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withType().withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType();
        propertyAssignmentFetchOptions.withRegistrator();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");
        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        assertEquals(1, map.size());
        Experiment experiment = map.get(permId);
        ExperimentType type = experiment.getType();
        assertEquals(type.getCode(), "SIRNA_HCS");

        assertEquals(type.getFetchOptions().hasPropertyAssignments(), true);
        List<PropertyAssignment> propertyAssignments = type.getPropertyAssignments();
        assertEquals(propertyAssignments.size(), 3);

        PropertyType propertyType0 = propertyAssignments.get(0).getPropertyType();
        assertEquals(propertyType0.getCode(), "DESCRIPTION");
        assertEquals(propertyType0.getLabel(), "Description");
        assertEquals(propertyType0.getDescription(), "A Description");
        assertEquals(propertyType0.isManagedInternally(), Boolean.FALSE);
        assertEquals(propertyType0.getDataType(), DataType.VARCHAR);
        assertEqualsDate(propertyType0.getRegistrationDate(), "2008-11-05 09:18:00");

        PropertyType propertyType1 = propertyAssignments.get(1).getPropertyType();
        assertEquals(propertyType1.getCode(), "PURCHASE_DATE");
        assertEquals(propertyType1.getLabel(), "Purchased");
        assertEquals(propertyType1.getDescription(), "When material has been bought");
        assertEquals(propertyType1.isManagedInternally(), Boolean.FALSE);
        assertEquals(propertyType1.getDataType(), DataType.TIMESTAMP);
        assertEqualsDate(propertyType1.getRegistrationDate(), "2008-11-05 09:18:16");

        PropertyType propertyType2 = propertyAssignments.get(2).getPropertyType();
        assertEquals(propertyType2.getCode(), "GENDER");
        assertEquals(propertyType2.getLabel(), "Gender");
        assertEquals(propertyType2.getDescription(), "The gender of the living organism");
        assertEquals(propertyType2.isManagedInternally(), Boolean.FALSE);
        assertEquals(propertyType2.getDataType(), DataType.CONTROLLEDVOCABULARY);
        assertEqualsDate(propertyType2.getRegistrationDate(), "2008-11-05 09:18:31");

        assertEquals(propertyAssignments.get(0).getRegistrator().getUserId(), "system");
        assertEquals(propertyAssignments.get(1).getRegistrator().getUserId(), "test");
        assertEquals(propertyAssignments.get(2).getRegistrator().getUserId(), "test");

        for (PropertyAssignment propertyAssignment : propertyAssignments)
        {
            PropertyType propertyType = propertyAssignment.getPropertyType();
            assertVocabularyNotFetched(propertyType);
            assertMaterialTypeNotFetched(propertyType);
            assertRegistratorNotFetched(propertyType);
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTypeWithPropertyAssignmentsWithExplicitPropertyTypeWithAllOptions()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        PropertyTypeFetchOptions propertyTypeFetchOptions = fetchOptions.withType().withPropertyAssignments().withPropertyType();
        propertyTypeFetchOptions.withVocabulary();
        propertyTypeFetchOptions.withMaterialType();
        propertyTypeFetchOptions.withRegistrator();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");
        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        assertEquals(1, map.size());
        Experiment experiment = map.get(permId);
        ExperimentType type = experiment.getType();
        assertEquals(type.getCode(), "SIRNA_HCS");

        assertEquals(type.getFetchOptions().hasPropertyAssignments(), true);
        List<PropertyAssignment> propertyAssignments = type.getPropertyAssignments();
        assertEquals(propertyAssignments.size(), 3);

        assertEquals(propertyAssignments.get(0).getPropertyType().getCode(), "DESCRIPTION");
        assertEquals(propertyAssignments.get(1).getPropertyType().getCode(), "PURCHASE_DATE");
        assertEquals(propertyAssignments.get(2).getPropertyType().getCode(), "GENDER");

        PropertyType propertyType = propertyAssignments.get(2).getPropertyType();
        assertEquals(propertyType.getVocabulary().getCode(), "GENDER");
        assertNull(propertyType.getMaterialType());
        assertEquals(propertyType.getRegistrator().getUserId(), "test");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTypeReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withType();

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(identifier1, identifier2), fetchOptions);

        assertEquals(2, map.size());
        Experiment experiment1 = map.get(identifier1);
        Experiment experiment2 = map.get(identifier2);

        assertFalse(experiment1 == experiment2);
        assertEquals(experiment1.getType().getCode(), "SIRNA_HCS");
        assertEquals(experiment2.getType().getCode(), "SIRNA_HCS");
        assertTrue(experiment1.getType() == experiment2.getType());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithAttachment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withAttachments().withPreviousVersion().withPreviousVersion().withContent();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId(), permId);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        List<Attachment> attachments = experiment.getAttachments(); // 1 of them

        Attachment attachment4 = attachments.get(0);
        assertAttachmentContentNotFetched(attachment4);

        Attachment attachment3 = attachment4.getPreviousVersion();
        assertAttachmentContentNotFetched(attachment3);

        Attachment attachment2 = attachment3.getPreviousVersion();
        assertPreviousAttachmentNotFetched(attachment2);
        assertEquals(attachment2.getContent().length, 228);

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test()
    public void testGetWithProject()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId(), permId);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        Project project = experiment.getProject();
        assertEquals(project.getPermId().toString(), "20120814110011738-103");
        assertEquals(project.getCode(), "NEMO");
        assertEquals(project.getIdentifier().toString(), "/CISD/NEMO");
        assertEquals(project.getDescription(), "nemo description");

        assertTypeNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test()
    public void testGetWithProjectReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject();

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(identifier1, identifier2), fetchOptions);

        assertEquals(2, map.size());
        Experiment experiment1 = map.get(identifier1);
        Experiment experiment2 = map.get(identifier2);

        assertFalse(experiment1 == experiment2);
        assertEquals(experiment1.getProject().getCode(), "NEMO");
        assertEquals(experiment2.getProject().getCode(), "NEMO");
        assertTrue(experiment1.getProject() == experiment2.getProject());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId(), permId);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        Map<String, Serializable> properties = experiment.getProperties();
        assertEquals(properties.size(), 2);
        assertEquals(properties.get("DESCRIPTION"), "A simple experiment");
        assertEquals(properties.get("GENDER"), "MALE");

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithRegistrator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withRegistrator();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId(), permId);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        assertEquals(experiment.getRegistrator().getUserId(), "test");
        assertRegistratorNotFetched(experiment.getRegistrator());

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertModifierNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test()
    public void testGetWithRegistratorReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withRegistrator();

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(identifier1, identifier2), fetchOptions);

        assertEquals(2, map.size());
        Experiment experiment1 = map.get(identifier1);
        Experiment experiment2 = map.get(identifier2);

        assertFalse(experiment1 == experiment2);
        assertEquals(experiment1.getRegistrator().getUserId(), "test");
        assertEquals(experiment2.getRegistrator().getUserId(), "test");
        assertTrue(experiment1.getRegistrator() == experiment2.getRegistrator());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithModifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withModifier();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId().toString(), "200811050951882-1028");
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        assertEquals(experiment.getModifier().getUserId(), "test_role");
        assertRegistratorNotFetched(experiment.getModifier());

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test()
    public void testGetWithModifierReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withModifier();

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP10");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(identifier1, identifier2), fetchOptions);

        assertEquals(2, map.size());
        Experiment experiment1 = map.get(identifier1);
        Experiment experiment2 = map.get(identifier2);

        assertFalse(experiment1 == experiment2);
        assertEquals(experiment1.getModifier().getUserId(), "test_role");
        assertEquals(experiment2.getModifier().getUserId(), "test_role");
        assertTrue(experiment1.getModifier() == experiment2.getModifier());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTags()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withTags();

        ExperimentPermId permId = new ExperimentPermId("200811050952663-1030");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId().toString(), "200811050952663-1030");
        assertEquals(experiment.getCode(), "EXP11");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP11");
        assertEquals(experiment.getTags().size(), 2);

        Set<String> actualTags = new HashSet<String>();
        for (Tag tag : experiment.getTags())
        {
            actualTags.add(tag.getPermId().getPermId());
        }
        assertEquals(actualTags, new HashSet<String>(Arrays.asList("/test/TEST_METAPROJECTS", "/test/ANOTHER_TEST_METAPROJECTS")));

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);
        assertAttachmentsNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTagsReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withTags();

        ExperimentPermId permId1 = new ExperimentPermId("200811050952663-1030");
        ExperimentPermId permId2 = new ExperimentPermId("201206190940555-1032");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Experiment experiment1 = map.get(permId1);
        Experiment experiment2 = map.get(permId2);

        assertEquals(experiment1.getTags().size(), 2);
        assertEquals(experiment2.getTags().size(), 1);
        assertContainSameObjects(experiment1.getTags(), experiment2.getTags(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithDataSets()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withDataSets();

        ExperimentPermId permId = new ExperimentPermId("200902091239077-1033");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        Experiment experiment = map.get(permId);

        List<DataSet> dataSets = experiment.getDataSets();
        assertEquals(dataSets.size(), 1);
        DataSet dataSet = dataSets.get(0);
        assertEquals(dataSet.getPermId().getPermId(), "20081105092159111-1");

        assertTypeNotFetched(dataSet);
        assertTagsNotFetched(dataSet);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithSamples()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withSamples();

        ExperimentPermId permId = new ExperimentPermId("200902091239077-1033");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        Experiment experiment = map.get(permId);
        List<Sample> samples = experiment.getSamples();
        Collection<String> codes = CollectionUtils.collect(samples, new Transformer<Sample, String>()
        {
            @Override
            public String transform(Sample input)
            {
                return input.getCode();
            }
        });
        AssertionUtil.assertCollectionContainsOnly(codes, "CP-TEST-1", "DYNA-TEST-1");
        v3api.logout(sessionToken);
    }

    public void testGetWithDataSetsAndDataSetFetchOptionsViaSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withDataSets().withTags();
        fetchOptions.withSamples().withDataSets().withType();

        ExperimentPermId permId = new ExperimentPermId("200902091239077-1033");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        Experiment experiment = map.get(permId);

        List<DataSet> dataSets = experiment.getDataSets();
        AssertionUtil.assertCollectionSize(dataSets, 1);
        DataSet dataSet = dataSets.get(0);
        assertEquals(dataSet.getPermId().getPermId(), "20081105092159111-1");

        assertEquals(dataSet.getType().getCode(), "HCS_IMAGE");
        dataSet.getTags();

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithProjectWithSpaceWithSamples()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject().withSpace().withSamples();

        ExperimentPermId permId = new ExperimentPermId("200902091255058-1037");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        assertEquals(1, map.size());
        Experiment experiment = map.get(permId);

        List<Sample> totalSamples = experiment.getProject().getSpace().getSamples();

        Collection<String> sampleCodes = CollectionUtils.collect(totalSamples, new Transformer<Sample, String>()
        {
            @Override
            public String transform(Sample input)
            {
                return input.getCode();
            }
        });

        AssertionUtil.assertCollectionContainsOnly(sampleCodes, "FV-TEST", "EV-TEST", "EV-INVALID", "EV-NOT_INVALID", "EV-PARENT",
                "EV-PARENT-NORMAL", "CP-TEST-4", "SAMPLE-TO-DELETE");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithMaterialProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withMaterialProperties().withRegistrator();
        fetchOptions.withProperties();

        ExperimentPermId permId = new ExperimentPermId("201108050937246-1031");

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        Experiment experiment = map.get(permId);

        assertEquals(experiment.getProperties().get("ANY_MATERIAL"), "BACTERIUM-Y (BACTERIUM)");

        Map<String, Material> materialProperties = experiment.getMaterialProperties();

        Material bacterium = materialProperties.get("ANY_MATERIAL");
        assertEquals(bacterium.getPermId(), new MaterialPermId("BACTERIUM-Y", "BACTERIUM"));
        assertEquals(bacterium.getRegistrator().getUserId(), "test");
        assertTagsNotFetched(bacterium);
    }

    @Test
    public void testGetWithHistoryNoChanges()
    {
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_EMPTY_HISTORY");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        creation.setProperty("DESCRIPTION", "a description");

        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withHistory();

        Experiment experiment = testGetWithHistory(creation, fo, null);
        List<HistoryEntry> history = experiment.getHistory();

        assertEquals(history.size(), 2);

        assertPropertyHistory(history.get(0), "DESCRIPTION", "a description", experiment.getRegistrationDate(), null);
        assertRelationshipHistory(history.get(1), new ProjectPermId("20120814110011738-101"), ExperimentRelationType.PROJECT,
                experiment.getRegistrationDate(), null);

        assertNotFetched(experiment::getPropertiesHistory);
        assertNotFetched(experiment::getProjectHistory);
        assertNotFetched(experiment::getSamplesHistory);
        assertNotFetched(experiment::getDataSetsHistory);
        assertNotFetched(experiment::getUnknownHistory);
    }

    @Test
    public void testGetWithHistoryProperty()
    {
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_EMPTY_HISTORY");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        creation.setProperty("DESCRIPTION", "a description");

        ExperimentUpdate update = new ExperimentUpdate();
        update.setProperty("DESCRIPTION", "a description 2");

        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withHistory();
        fo.withPropertiesHistory();

        Experiment experiment = testGetWithHistory(creation, fo, update);

        assertEquals(experiment.getHistory().size(), 3);
        assertPropertyHistory(experiment.getHistory().get(0), "DESCRIPTION", "a description", experiment.getRegistrationDate(),
                experiment.getModificationDate());
        assertPropertyHistory(experiment.getHistory().get(1), "DESCRIPTION", "a description 2", experiment.getModificationDate(), null);
        assertRelationshipHistory(experiment.getHistory().get(2), new ProjectPermId("20120814110011738-101"), ExperimentRelationType.PROJECT,
                experiment.getRegistrationDate(), null);

        assertEquals(experiment.getPropertiesHistory().size(), 2);
        assertPropertyHistory(experiment.getHistory().get(0), "DESCRIPTION", "a description", experiment.getRegistrationDate(),
                experiment.getModificationDate());
        assertPropertyHistory(experiment.getHistory().get(1), "DESCRIPTION", "a description 2", experiment.getModificationDate(), null);

        assertNotFetched(experiment::getProjectHistory);
    }

    @Test
    public void testGetWithHistoryProject()
    {
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_PROJECT_HISTORY");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        creation.setProperty("DESCRIPTION", "a description");

        ExperimentUpdate update = new ExperimentUpdate();
        update.setProjectId(new ProjectIdentifier("/CISD/NEMO"));

        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withHistory();
        fo.withProjectHistory();

        Experiment experiment = testGetWithHistory(creation, fo, update);

        assertEquals(experiment.getHistory().size(), 3);
        assertPropertyHistory(experiment.getHistory().get(0), "DESCRIPTION", "a description", experiment.getRegistrationDate(), null);
        assertRelationshipHistory(experiment.getHistory().get(1), new ProjectPermId("20120814110011738-101"), ExperimentRelationType.PROJECT,
                experiment.getRegistrationDate(), experiment.getModificationDate());
        assertRelationshipHistory(experiment.getHistory().get(2), new ProjectPermId("20120814110011738-103"), ExperimentRelationType.PROJECT,
                experiment.getRegistrationDate(), null);

        assertEquals(experiment.getProjectHistory().size(), 2);
        assertRelationshipHistory(experiment.getProjectHistory().get(0), new ProjectPermId("20120814110011738-101"), ExperimentRelationType.PROJECT,
                experiment.getRegistrationDate(), experiment.getModificationDate());
        assertRelationshipHistory(experiment.getProjectHistory().get(1), new ProjectPermId("20120814110011738-103"), ExperimentRelationType.PROJECT,
                experiment.getRegistrationDate(), null);

        assertNotFetched(experiment::getPropertiesHistory);
    }

    @Test
    public void testGetWithHistorySample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("EXPERIMENT_WITH_SAMPLE_HISTORY");
        experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> experimentPermIds = v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation));

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleCreation.setCode("SAMPLE_WITH_EXPERIMENT_HISTORY");
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setExperimentId(experimentPermIds.get(0));

        List<SamplePermId> samplePermIds = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation));

        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(samplePermIds.get(0));
        sampleUpdate.setExperimentId(null);

        v3api.updateSamples(sessionToken, Arrays.asList(sampleUpdate));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withHistory();
        fetchOptions.withSamplesHistory();

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, experimentPermIds, fetchOptions);
        assertEquals(map.size(), 1);

        Experiment experiment = map.get(experimentPermIds.get(0));

        assertEquals(experiment.getHistory().size(), 3);
        assertPropertyHistory(experiment.getHistory().get(0), "DESCRIPTION", "a description", experiment.getRegistrationDate(), null);
        assertRelationshipHistory(experiment.getHistory().get(1), new ProjectPermId("20120814110011738-101"), ExperimentRelationType.PROJECT,
                experiment.getRegistrationDate(), null);
        assertRelationshipHistory(experiment.getHistory().get(2), samplePermIds.get(0), ExperimentRelationType.SAMPLE);

        assertEquals(experiment.getSamplesHistory().size(), 1);
        assertRelationshipHistory(experiment.getSamplesHistory().get(0), samplePermIds.get(0), ExperimentRelationType.SAMPLE);

        assertNotFetched(experiment::getPropertiesHistory);
        assertNotFetched(experiment::getProjectHistory);
    }

    @Test
    public void testGetWithHistoryDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_DATA_SET_HISTORY");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId("COMPONENT_1A"));
        update.setExperimentId(permIds.get(0));

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId("COMPONENT_1A"));
        update.setExperimentId(new ExperimentPermId("200811050940555-1032"));

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withHistory();
        fetchOptions.withDataSetsHistory();

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, permIds, fetchOptions);
        assertEquals(map.size(), 1);

        Experiment experiment = map.get(permIds.get(0));

        assertEquals(experiment.getHistory().size(), 3);
        assertPropertyHistory(experiment.getHistory().get(0), "DESCRIPTION", "a description", experiment.getRegistrationDate(), null);
        assertRelationshipHistory(experiment.getHistory().get(1), new ProjectPermId("20120814110011738-101"), ExperimentRelationType.PROJECT,
                experiment.getRegistrationDate(), null);
        assertRelationshipHistory(experiment.getHistory().get(2), new DataSetPermId("COMPONENT_1A"), ExperimentRelationType.DATA_SET);

        assertEquals(experiment.getDataSetsHistory().size(), 1);
        assertRelationshipHistory(experiment.getDataSetsHistory().get(0), new DataSetPermId("COMPONENT_1A"), ExperimentRelationType.DATA_SET);

        assertNotFetched(experiment::getPropertiesHistory);
        assertNotFetched(experiment::getProjectHistory);
    }

    private Experiment testGetWithHistory(ExperimentCreation creation, ExperimentFetchOptions fo, ExperimentUpdate update)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        if (update != null)
        {
            update.setExperimentId(permIds.get(0));
            v3api.updateExperiments(sessionToken, Arrays.asList(update));
        }

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, permIds, fo);

        assertEquals(map.size(), 1);
        Experiment experiment = map.get(permIds.get(0));

        v3api.logout(sessionToken);

        return experiment;
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testGetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        List<IExperimentId> ids = Arrays.asList(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"),
                new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.getExperiments(sessionToken, ids, experimentFetchOptionsFull());
                }
            });
        } else
        {
            Map<IExperimentId, Experiment> result = v3api.getExperiments(sessionToken, ids, experimentFetchOptionsFull());

            if (user.isInstanceUser())
            {
                assertEquals(result.size(), 2);
            } else if (user.isTestSpaceUser() || user.isTestProjectUser())
            {
                assertEquals(result.size(), 1);
                assertEquals(result.values().iterator().next().getIdentifier(), new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
            } else
            {
                assertEquals(result.size(), 0);
            }
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withProject();
        fo.withProperties();

        v3api.getExperiments(sessionToken,
                Arrays.asList(new ExperimentPermId("200811050951882-1028"), new ExperimentIdentifier("/CISD/NOE/EXP-TEST-2")), fo);

        assertAccessLog(
                "get-experiments  EXPERIMENT_IDS('[200811050951882-1028, /CISD/NOE/EXP-TEST-2]') FETCH_OPTIONS('Experiment\n    with Project\n    with Properties\n')");
    }

}
