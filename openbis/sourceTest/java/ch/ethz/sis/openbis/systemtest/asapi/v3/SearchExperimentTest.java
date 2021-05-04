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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.fetchoptions.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DatePropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.fail;
import static org.testng.Assert.assertEquals;

/**
 * @author pkupczyk
 */
public class SearchExperimentTest extends AbstractExperimentTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        List<Experiment> experiments = searchExperiments(sessionToken, new ExperimentSearchCriteria(), new ExperimentFetchOptions());

        v3api.logout(sessionToken);

        assertExperimentIdentifiers(experiments, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithAttachments()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withPermId().thatEquals("200811050951882-1028");
        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        AttachmentFetchOptions previousVersionAttachmentOptions = fo.withAttachments().withPreviousVersion();
        previousVersionAttachmentOptions.withContent();
        previousVersionAttachmentOptions.withRegistrator();
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<Experiment> experiments = searchExperiments(sessionToken, criteria, fo);
        v3api.logout(sessionToken);

        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getFetchOptions().hasAttachments(), true);
        List<Attachment> attachments = experiment.getAttachments();
        Attachment attachment = attachments.get(0);
        assertEquals(attachment.getVersion(), new Integer(4));
        assertEquals(attachment.getFileName(), "exampleExperiments.txt");
        assertEquals(attachment.getTitle(), "Latest version");
        assertEquals(attachment.getDescription(), null);
        assertEquals(attachment.getFetchOptions().hasContent(), false);
        assertEquals(attachment.getFetchOptions().hasRegistrator(), false);
        assertEquals(attachment.getFetchOptions().hasPreviousVersion(), true);
        Attachment previousVersion = attachment.getPreviousVersion();
        assertEquals(previousVersion.getVersion(), new Integer(3));
        assertEquals(previousVersion.getFileName(), "exampleExperiments.txt");
        assertEquals(previousVersion.getTitle(), null);
        assertEquals(previousVersion.getDescription(), "Second latest version");
        assertEquals(previousVersion.getFetchOptions().hasRegistrator(), true);
        assertEquals(previousVersion.getRegistrator().toString(), "Person test");
        assertEquals(previousVersion.getRegistrationDate().toString(), "2008-12-10 13:49:20.23603");
        assertEquals(previousVersion.getFetchOptions().hasContent(), true);
        assertEquals(previousVersion.getFetchOptions().hasPreviousVersion(), false);
        assertEquals(previousVersion.getContent().length, 227);
        assertEquals(attachments.size(), 1);
        assertEquals(experiments.size(), 1);
    }

    @Test
    public void testSearchWithIdSetToIdentifier()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withId().thatEquals(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP1");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withId().thatEquals(new ExperimentPermId("200811050951882-1028"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP1");
    }

    @Test
    public void testSearchWithPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withPermId().thatEquals("200811050951882-1028");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP1");
    }

    @Test
    public void testSearchWithIdentifierThatEquals()
    {
        final ExperimentSearchCriteria criteria1 = new ExperimentSearchCriteria();
        criteria1.withIdentifier().thatEquals("/CISD/NEMO/EXP1");
        testSearch(TEST_USER, criteria1, "/CISD/NEMO/EXP1");

        final ExperimentSearchCriteria criteria2 = new ExperimentSearchCriteria();
        criteria2.withIdentifier().withWildcards().thatEquals("/cisd/Nemo/Exp1*");
        testSearch(TEST_USER, criteria2, "/CISD/NEMO/EXP1", "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP11");

        final ExperimentSearchCriteria criteria3 = new ExperimentSearchCriteria();
        criteria3.withIdentifier().withoutWildcards().thatEquals("/cisd/Nemo/Exp1*");
        testSearch(TEST_USER, criteria3);

        final ExperimentSearchCriteria criteria4 = new ExperimentSearchCriteria();
        criteria4.withIdentifier().withWildcards().thatEquals("/CISD/*/EXP-TEST-2");
        testSearch(TEST_USER, criteria4, "/CISD/NEMO/EXP-TEST-2", "/CISD/NOE/EXP-TEST-2");

        final ExperimentSearchCriteria criteria5 = new ExperimentSearchCriteria();
        criteria5.withIdentifier().withoutWildcards().thatEquals("/CISD/*/EXP-TEST-2");
        testSearch(TEST_USER, criteria5);
    }

    @Test
    public void testSearchWithIdentifierThatStartsWith()
    {
        final ExperimentSearchCriteria criteria1 = new ExperimentSearchCriteria();
        criteria1.withIdentifier().thatStartsWith("/TEST-SPACE/TEST-PROJ");
        testSearch(TEST_USER, criteria1, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        final ExperimentSearchCriteria criteria2 = new ExperimentSearchCriteria();
        criteria2.withIdentifier().withWildcards().thatStartsWith("/CISD/DEFAULT/*S*");
        testSearch(TEST_USER, criteria2, "/CISD/DEFAULT/EXP-WELLS", "/CISD/DEFAULT/EXP-REUSE");

        final ExperimentSearchCriteria criteria3 = new ExperimentSearchCriteria();
        criteria3.withIdentifier().withoutWildcards().thatStartsWith("/CISD/DEFAULT/*S*");
        testSearch(TEST_USER, criteria3);
    }

    @Test
    public void testSearchWithIdentifierThatEndsWith()
    {
        final ExperimentSearchCriteria criteria1 = new ExperimentSearchCriteria();
        criteria1.withIdentifier().thatEndsWith("-TEST-2");
        testSearch(TEST_USER, criteria1, "/CISD/NEMO/EXP-TEST-2", "/CISD/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");

        final ExperimentSearchCriteria criteria2 = new ExperimentSearchCriteria();
        criteria2.withIdentifier().withoutWildcards().thatEndsWith("-TEST-*");
        testSearch(TEST_USER, criteria2);

        final ExperimentSearchCriteria criteria3 = new ExperimentSearchCriteria();
        criteria3.withIdentifier().withWildcards().thatEndsWith("-TEST-*");
        testSearch(TEST_USER, criteria3, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP-TEST-2", "/CISD/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithIdentifierThatContains()
    {
        final ExperimentSearchCriteria criteria1 = new ExperimentSearchCriteria();
        criteria1.withIdentifier().thatContains("TEST-PROJECT");
        testSearch(TEST_USER, criteria1, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        final ExperimentSearchCriteria criteria2 = new ExperimentSearchCriteria();
        criteria2.withIdentifier().withWildcards().thatContains("TE*JECT");
        testSearch(TEST_USER, criteria2, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        final ExperimentSearchCriteria criteria3 = new ExperimentSearchCriteria();
        criteria3.withIdentifier().withoutWildcards().thatContains("TE*JECT");
        testSearch(TEST_USER, criteria3);
    }

    @Test
    public void testSearchWithCode()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withCode().thatStartsWith("EXP1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP1", "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithCodes()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("EXP10", "EXP11"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithTypeWithIdSetToPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withType().withId().thatEquals(new EntityTypePermId("COMPOUND_HCS"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withType().withCode().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withType().withPermId().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithProjectWithIdSetToIdentifier()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/NOE"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithIdSetToPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withPermId().thatEquals("20120814110011738-106");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithCode()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withCode().thatEquals("NOE");
        testSearch(TEST_USER, criteria, "/CISD/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithSpaceWithIdSetToPermId()
    {
        String[] expected = new String[] { "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE" };

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withSpace().withId().thatEquals(new SpacePermId("TEST-SPACE"));
        testSearch(TEST_USER, criteria, expected);

        criteria = new ExperimentSearchCriteria();
        criteria.withProject().withSpace().withId().thatEquals(new SpacePermId("/TEST-SPACE"));
        testSearch(TEST_USER, criteria, expected);
    }

    @Test
    public void testSearchWithProjectWithSpaceWithCode()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withSpace().withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithSpaceWithPermId()
    {
        String[] expected = new String[] { "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE" };

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProject().withSpace().withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, expected);

        criteria = new ExperimentSearchCriteria();
        criteria.withProject().withSpace().withPermId().thatEquals("/TEST-SPACE");
        testSearch(TEST_USER, criteria, expected);
    }

    @Test
    public void testSearchWithPropertyThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("desc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("desc");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("esc");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEquals("esc1");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithPropertyThatStartsWith()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("desc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("desc");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("esc");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatStartsWith("esc1");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithPropertyThatEndsWith()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("desc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("desc");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("esc");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatEndsWith("esc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");
    }

    @Test
    public void testSearchWithProperty()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProperty("COMMENT");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");
    }

    @Test
    public void testSearchWithPropertyThatContains()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("desc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("desc");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("esc");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withProperty("DESCRIPTION").thatContains("esc1");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1");
    }

    @Test
    public void testSearchWithPropertyMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        final EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);
        final ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("SAMPLE_PROPERTY_TEST");
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createExperiments(sessionToken, Collections.singletonList(experimentCreation));

        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withOrOperator();
        criteria.withProperty(propertyType.getPermId()).thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithDatePropertyThatEqualsWithString()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatEquals("2009-02-08");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatEquals("2009-02-09");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(2).thatEquals("2009-02-10");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithDatePropertyThatEqualsWithDate() throws Exception
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatEquals(format.parse("2009-02-08 23:59"));
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatEquals(format.parse("2009-02-09 23:00"));
        criteria.withDateProperty("PURCHASE_DATE").thatEquals(format.parse("2009-02-09 09:00"));
        criteria.withOrOperator();
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatEquals(format.parse("2009-02-10 00:00"));
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithDatePropertyThatIsEarlierThanOrEqualToWithString()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsEarlierThanOrEqualTo("2009-02-08");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(1).thatIsEarlierThanOrEqualTo("2009-02-09 09:00");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsEarlierThanOrEqualTo("2009-02-09 09:00");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsEarlierThanOrEqualTo("2009-02-09");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(2).thatIsEarlierThanOrEqualTo("2009-02-09");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithDatePropertyThatEarlierThanOrEqualToWithDate() throws Exception
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsEarlierThanOrEqualTo(format.parse("2009-02-09 08:59"));
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsEarlierThanOrEqualTo(format.parse("2009-02-09 09:00"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsEarlierThanOrEqualTo(format.parse("2009-02-09 23:00"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithDatePropertyThatIsLaterThanOrEqualToWithString()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsLaterThanOrEqualTo("2009-02-10");
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsLaterThanOrEqualTo("2009-02-09");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").withTimeZone(0).thatIsLaterThanOrEqualTo("2009-02-08");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithDatePropertyThatIsLaterThanOrEqualToWithDate() throws Exception
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsLaterThanOrEqualTo(format.parse("2009-02-10 00:01"));
        testSearch(TEST_USER, criteria, 0);

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsLaterThanOrEqualTo(format.parse("2009-02-10 00:00"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/NOE/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty("PURCHASE_DATE").thatIsLaterThanOrEqualTo(format.parse("2009-02-09 10:00"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithDateDatePropertyThatEquals()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_DATE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(propertyType.getPermId(), "2/17/20");
        v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty(propertyType.getPermId()).thatEquals("20-2-17");

        // When
        List<Experiment> experiments = v3api.searchExperiments(sessionToken, criteria, new ExperimentFetchOptions()).getObjects();

        // Then
        assertExperimentIdentifiers(experiments, "/CISD/NEMO/EXPERIMENT_WITH_DATE_PROPERTY");
    }

    @Test
    public void testSearchWithDateDatePropertyWithInvalidCriteria()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_DATE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(propertyType.getPermId(), "2/17/20");
        v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        try
        {
            // When
            criteria.withDateProperty(propertyType.getPermId()).thatIsLaterThanOrEqualTo("20-2-37");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e)
        {
            // Then
            assertEquals(e.getMessage(), "Date value: later than or equal to '20-2-37' "
                    + "does not match any of the supported formats: [y-M-d HH:mm:ss, y-M-d HH:mm, y-M-d]");
        }
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsLater()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_DATE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(propertyType.getPermId(), "2/17/20");
        v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty(propertyType.getPermId()).thatIsLaterThanOrEqualTo("2020-02-16");

        // When
        List<Experiment> experiments = v3api.searchExperiments(sessionToken, criteria, new ExperimentFetchOptions()).getObjects();

        // Then
        assertEquals(experiments.get(0).getIdentifier().getIdentifier(), "/CISD/NEMO/EXPERIMENT_WITH_DATE_PROPERTY");
        assertEquals(experiments.size(), 1);
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsLaterOrEqual()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);
        ExperimentCreation creation1 = new ExperimentCreation();
        creation1.setCode("EXPERIMENT_WITH_DATE_PROPERTY1");
        creation1.setTypeId(experimentType);
        creation1.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation1.setProperty(propertyType.getPermId(), "2/17/20");
        ExperimentCreation creation2 = new ExperimentCreation();
        creation2.setCode("EXPERIMENT_WITH_DATE_PROPERTY2");
        creation2.setTypeId(experimentType);
        creation2.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation2.setProperty(propertyType.getPermId(), "2020-02-16");
        v3api.createExperiments(sessionToken, Arrays.asList(creation1, creation2));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty(propertyType.getPermId()).thatIsLaterThanOrEqualTo("2020-02-16");

        // When
        List<Experiment> experiments = v3api.searchExperiments(sessionToken, criteria, new ExperimentFetchOptions()).getObjects();

        // Then
        assertExperimentIdentifiers(experiments, "/CISD/NEMO/EXPERIMENT_WITH_DATE_PROPERTY1",
                "/CISD/NEMO/EXPERIMENT_WITH_DATE_PROPERTY2");
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsEarlier()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_DATE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(propertyType.getPermId(), "1990-11-09");
        v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withDateProperty(propertyType.getPermId()).thatIsEarlierThanOrEqualTo("1990-11-10");

        // When
        List<Experiment> experiments = v3api.searchExperiments(sessionToken, criteria, new ExperimentFetchOptions()).getObjects();

        // Then
        assertEquals(experiments.get(0).getIdentifier().getIdentifier(), "/CISD/NEMO/EXPERIMENT_WITH_DATE_PROPERTY");
        assertEquals(experiments.size(), 1);
    }

    @Test
    public void testSearchWithDateDatePropertyThatIsEarlierWithTimezone()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_DATE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(propertyType.getPermId(), "1990-11-09");
        v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        DatePropertySearchCriteria datePropertySearchCriteria = criteria.withDateProperty(propertyType.getPermId());
        datePropertySearchCriteria.withTimeZone(6);
        datePropertySearchCriteria.thatIsEarlierThanOrEqualTo("1990-11-09");

        // When
        assertUserFailureException(Void -> v3api.searchExperiments(sessionToken, criteria, new ExperimentFetchOptions()),
                // Then
                "Search criteria with time zone doesn't make sense for property " + propertyType.getPermId()
                        + " of data type " + DataType.DATE);
    }

    @Test
    public void testSearchWithAnyPropertyThatIsEarlier()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_DATE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(propertyType.getPermId(), "1990-11-09");
        v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatIsLessThanOrEqualTo("2009-02-10");

        // When
        List<Experiment> experiments = v3api.searchExperiments(sessionToken, criteria, new ExperimentFetchOptions()).getObjects();

        // Then
        assertExperimentIdentifiers(experiments, "/CISD/NEMO/EXPERIMENT_WITH_DATE_PROPERTY", "/CISD/NEMO/EXP-TEST-2");
        assertEquals(experiments.size(), 2);
    }

    @Test
    public void testSearchWithDateDatePropertyWithTimezone()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_DATE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(propertyType.getPermId(), "2/17/20");
        v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        DatePropertySearchCriteria datePropertySearchCriteria = criteria.withDateProperty(propertyType.getPermId());
        datePropertySearchCriteria.withTimeZone(-4);
        datePropertySearchCriteria.thatEquals("2020-02-17");

        // When
        assertUserFailureException(Void -> v3api.searchExperiments(sessionToken, criteria, new ExperimentFetchOptions()),
                // Then
                "Search criteria with time zone doesn't make sense for property " + propertyType.getPermId()
                        + " of data type " + DataType.DATE);
    }

    @Test
    public void testSearchWithAnyPropertyThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatEquals("FEMALE");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatEquals("FEMAL");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithAnyPropertyThatEqualsWithWildcards()
    {
        final ExperimentSearchCriteria criteria1 = new ExperimentSearchCriteria();
        criteria1.withAnyProperty().withoutWildcards().thatEquals("*EMAL*");
        testSearch(TEST_USER, criteria1);

        final ExperimentSearchCriteria criteria2 = new ExperimentSearchCriteria();
        criteria2.withAnyProperty().withWildcards().thatEquals("*EMAL*");
        testSearch(TEST_USER, criteria2, "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithAnyPropertyThatStartsWith()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatStartsWith("FEMAL");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatStartsWith("EMAL");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithAnyPropertyThatEndsWith()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatEndsWith("EMALE");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatEndsWith("EMAL");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithAnyPropertyThatContains()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatContains("EMAL");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");

        criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatContains("FMAL");
        testSearch(TEST_USER, criteria, 0);
    }

    @Test
    public void testSearchWithAnyFieldMatchingProperty()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyField().thatEquals("FEMALE");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithAnyFieldMatchingAttribute()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyField().thatEquals("EXP-TEST-2");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/CISD/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithAnyFieldMatchingRegistratorOrModifier()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyField().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11", "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithAnyFieldMatchingExperimentType()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyField().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithAnyFieldMatchingIdentifier()
    {
        final ExperimentSearchCriteria criteria1 = new ExperimentSearchCriteria();
        criteria1.withAnyField().withWildcards().thatEquals("/CISD/NEMO/EXP-TEST-*");
        testSearch(TEST_USER, criteria1, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP-TEST-2");

        final ExperimentSearchCriteria criteria2 = new ExperimentSearchCriteria();
        criteria2.withAnyField().withoutWildcards().thatEquals("/CISD/NEMO/EXP-TEST-*");
        testSearch(TEST_USER, criteria2);

        final ExperimentSearchCriteria criteria3 = new ExperimentSearchCriteria();
        criteria3.withAnyField().thatStartsWith("/CISD/NEMO/EXP1");
        testSearch(TEST_USER, criteria3, "/CISD/NEMO/EXP1", "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP11");

        final ExperimentSearchCriteria criteria4 = new ExperimentSearchCriteria();
        criteria4.withAnyField().withWildcards().thatEndsWith("TEST-?");
        testSearch(TEST_USER, criteria4, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP-TEST-2", "/CISD/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXP-TEST-2");

        final ExperimentSearchCriteria criteria5 = new ExperimentSearchCriteria();
        criteria5.withAnyField().withoutWildcards().thatEndsWith("TEST-?");
        testSearch(TEST_USER, criteria5);
    }

    @Test
    public void testSearchWithAnyFieldMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        final EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);
        final ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("SAMPLE_PROPERTY_TEST");
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation));

        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withOrOperator();
        criteria.withAnyField().thatEquals("/CISD/CL1");

        testSearch(TEST_USER, criteria, 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithAnyPropertyMatchingSampleProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        final EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);
        final ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("SAMPLE_PROPERTY_TEST");
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation));

        final ExperimentSearchCriteria withAnyPropertySearchCriteria = new ExperimentSearchCriteria();
        withAnyPropertySearchCriteria.withOrOperator();
        withAnyPropertySearchCriteria.withAnyProperty().thatStartsWith("/CISD/CL");
        testSearch(TEST_USER, withAnyPropertySearchCriteria, "/CISD/DEFAULT/SAMPLE_PROPERTY_TEST");

        final ExperimentSearchCriteria withPropertySearchCriteria = new ExperimentSearchCriteria();
        withPropertySearchCriteria.withOrOperator();
        withPropertySearchCriteria.withProperty(propertyType.getPermId()).thatStartsWith("/CISD/CL");
        testSearch(TEST_USER, withPropertySearchCriteria, "/CISD/DEFAULT/SAMPLE_PROPERTY_TEST");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithTagWithIdSetToPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithIdSetToCodeId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagCode("TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithCode()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withTag().withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithPermId()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withTag().withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithPermIdUnauthorized()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withTag().withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    @Test
    public void testSearchWithRegistratorWithUserIdThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withRegistrator().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithRegistratorWithFirstNameThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withRegistrator().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithRegistratorWithLastNameThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withRegistrator().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithRegistratorWithEmailThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withRegistrator().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithModifierWithUserIdThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withModifier().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithModifierWithFirstNameThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withModifier().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithModifierWithLastNameThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withModifier().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithModifierWithEmailThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withModifier().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithRegistrationDateThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withRegistrationDate().thatEquals("2009-02-09");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP-TEST-2", "/CISD/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithRegistrationDateThatIsLaterThan()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withRegistrationDate().thatIsLaterThanOrEqualTo("2009-02-09");
        testSearch(TEST_USER, criteria, 5);
    }

    @Test
    public void testSearchWithRegistrationDateThatIsEarlierThan()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withRegistrationDate().thatIsEarlierThanOrEqualTo("2008-11-05");
        testSearch(TEST_USER, criteria, 7);
    }

    @Test
    public void testSearchWithModificationDateThatEquals()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withModificationDate().thatEquals("2009-03-18");
        testSearch(TEST_USER, criteria, 12);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("TEST");
        criteria.withCode().thatContains("SPACE");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050952663-1029");
        criteria.withPermId().thatEquals("200811050952663-1030");
        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withPermId().thatEquals("200811050951882-1028");
        testSearch(TEST_USER, criteria, 1);

        criteria = new ExperimentSearchCriteria();
        criteria.withPermId().thatEquals("200811050951882-1028");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    @Test
    public void testSearchWithSortingByCode()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new ExperimentIdentifier("/CISD/DEFAULT/EXP-WELLS"));
        criteria.withId().thatEquals(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        criteria.withId().thatEquals(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fo = new ExperimentFetchOptions();

        fo.sortBy().code().asc();
        List<Experiment> experiments1 = v3api.searchExperiments(sessionToken, criteria, fo).getObjects();
        assertExperimentIdentifiersInOrder(experiments1, "/CISD/DEFAULT/EXP-REUSE", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST",
                "/CISD/DEFAULT/EXP-WELLS");

        fo.sortBy().code().desc();
        List<Experiment> experiments2 = v3api.searchExperiments(sessionToken, criteria, fo).getObjects();
        assertExperimentIdentifiersInOrder(experiments2, "/CISD/DEFAULT/EXP-WELLS", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST",
                "/CISD/DEFAULT/EXP-REUSE");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByIdentifier()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new ExperimentIdentifier("/CISD/DEFAULT/EXP-WELLS"));
        criteria.withId().thatEquals(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        criteria.withId().thatEquals(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fo = new ExperimentFetchOptions();

        fo.sortBy().identifier().asc();
        List<Experiment> experiments1 = v3api.searchExperiments(sessionToken, criteria, fo).getObjects();
        assertExperimentIdentifiersInOrder(experiments1, "/CISD/DEFAULT/EXP-REUSE", "/CISD/DEFAULT/EXP-WELLS",
                "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        fo.sortBy().identifier().desc();
        List<Experiment> experiments2 = v3api.searchExperiments(sessionToken, criteria, fo).getObjects();
        assertExperimentIdentifiersInOrder(experiments2, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/CISD/DEFAULT/EXP-WELLS",
                "/CISD/DEFAULT/EXP-REUSE");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByType()
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        criteria.withId().thatEquals(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        criteria.withId().thatEquals(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withType();

        fo.sortBy().type().asc();
        fo.sortBy().code().asc();
        List<Experiment> experiments1 = v3api.searchExperiments(sessionToken, criteria, fo).getObjects();
        assertExperimentIdentifiersInOrder(experiments1, "/CISD/NEMO/EXP-TEST-1", "/CISD/DEFAULT/EXP-REUSE",
                "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        fo.sortBy().type().desc();
        fo.sortBy().code().desc();
        List<Experiment> experiments2 = v3api.searchExperiments(sessionToken, criteria, fo).getObjects();
        assertExperimentIdentifiersInOrder(experiments2, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/CISD/DEFAULT/EXP-REUSE",
                "/CISD/NEMO/EXP-TEST-1");

        v3api.logout(sessionToken);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        criteria.withId().thatEquals(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        SearchResult<Experiment> result = v3api.searchExperiments(sessionToken, criteria, experimentFetchOptionsFull());

        if (user.isInstanceUser())
        {
            assertEquals(result.getObjects().size(), 2);
        } else if ((user.isTestSpaceUser() || user.isTestProjectUser()) && !user.isDisabledProjectUser())
        {
            assertEquals(result.getObjects().size(), 1);
            assertEquals(result.getObjects().get(0).getIdentifier(), new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        } else
        {
            assertEquals(result.getObjects().size(), 0);
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentSearchCriteria c = new ExperimentSearchCriteria();
        c.withCode().thatStartsWith("EXP1");
        c.withProperty("DESCRIPTION").thatEquals("abc");

        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withHistory();
        fo.withModifier();

        v3api.searchExperiments(sessionToken, c, fo);

        assertAccessLog(
                "search-experiments  SEARCH_CRITERIA:\n'EXPERIMENT\n    with operator 'AND'\n    with attribute 'code' starts with 'EXP1'\n    with property 'DESCRIPTION' equal to 'abc'\n'\nFETCH_OPTIONS:\n'Experiment\n    with History\n    with Modifier\n'");
    }

    @Test
    public void testSearchForExperimentWithDatePropertyUsingWithProperty()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withProperty("PURCHASE_DATE").thatEquals("2009-02-09 10:00:00 +0100");
        testSearch(TEST_USER, criteria, 1);
    }

    @Test
    public void testSearchForExperimentWithBooleanPropertyUsingWithProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyType1 = createABooleanPropertyType(sessionToken, "IS_TRUE");
        final PropertyTypePermId propertyType2 = createAnIntegerPropertyType(sessionToken, "NUMBER");
        final EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType1,
                propertyType2);

        final ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("BOOLEAN_PROPERTY_TEST");
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty("NUMBER", "123");
        experimentCreation.setProperty("IS_TRUE", "true");
        v3api.createExperiments(sessionToken, Collections.singletonList(experimentCreation));

        final ExperimentSearchCriteria booleanCriteria = new ExperimentSearchCriteria();
        booleanCriteria.withProperty("IS_TRUE").thatEquals("true");
        testSearch(TEST_USER, booleanCriteria, "/CISD/DEFAULT/BOOLEAN_PROPERTY_TEST");

        final ExperimentSearchCriteria integerCriteria = new ExperimentSearchCriteria();
        integerCriteria.withProperty("NUMBER").thatEquals("123");
        testSearch(TEST_USER, integerCriteria, "/CISD/DEFAULT/BOOLEAN_PROPERTY_TEST");
    }

    @Test
    public void testSearchForExperimentWithIntegerPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createAnIntegerPropertyType(sessionToken, "INT_NUMBER");
        final EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);

        final ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("INTEGER_PROPERTY_TEST");
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty("INT_NUMBER", "123");

        v3api.createExperiments(sessionToken, Collections.singletonList(experimentCreation));

        final ExperimentSearchCriteria criteriaStartsWithMatch = new ExperimentSearchCriteria();
        criteriaStartsWithMatch.withProperty("INT_NUMBER").thatStartsWith("12");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaStartsWithMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "INTEGER"));

        final ExperimentSearchCriteria criteriaEndsWithMatch = new ExperimentSearchCriteria();
        criteriaEndsWithMatch.withProperty("INT_NUMBER").thatEndsWith("23");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaEndsWithMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "INTEGER"));

        final ExperimentSearchCriteria criteriaContainsMatch = new ExperimentSearchCriteria();
        criteriaContainsMatch.withProperty("INT_NUMBER").thatContains("23");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaContainsMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "INTEGER"));
    }

    @Test
    public void testSearchForExperimentWithRealPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createARealPropertyType(sessionToken, "REAL_NUMBER");
        final EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);

        final ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("REAL_PROPERTY_TEST");
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty("REAL_NUMBER", "1.23");

        v3api.createExperiments(sessionToken, Collections.singletonList(experimentCreation));

        final ExperimentSearchCriteria criteriaStartsWithMatch = new ExperimentSearchCriteria();
        criteriaStartsWithMatch.withProperty("REAL_NUMBER").thatStartsWith("1.2");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaStartsWithMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "REAL"));

        final ExperimentSearchCriteria criteriaEndsWithMatch = new ExperimentSearchCriteria();
        criteriaEndsWithMatch.withProperty("REAL_NUMBER").thatEndsWith("23");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaEndsWithMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "REAL"));

        final ExperimentSearchCriteria criteriaContainsMatch = new ExperimentSearchCriteria();
        criteriaContainsMatch.withProperty("REAL_NUMBER").thatContains(".2");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaContainsMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "REAL"));
    }

    @Test
    public void testSearchForExperimentWithBooleanPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createABooleanPropertyType(sessionToken, "BOOLEAN");
        final EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);

        final ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("BOOLEAN_PROPERTY_TEST");
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty("BOOLEAN", "false");

        v3api.createExperiments(sessionToken, Collections.singletonList(experimentCreation));

        final ExperimentSearchCriteria criteriaStartsWithMatch = new ExperimentSearchCriteria();
        criteriaStartsWithMatch.withProperty("BOOLEAN").thatStartsWith("fa");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaStartsWithMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "BOOLEAN"));

        final ExperimentSearchCriteria criteriaEndsWithMatch = new ExperimentSearchCriteria();
        criteriaEndsWithMatch.withProperty("BOOLEAN").thatEndsWith("lse");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaEndsWithMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "BOOLEAN"));

        final ExperimentSearchCriteria criteriaContainsMatch = new ExperimentSearchCriteria();
        criteriaContainsMatch.withProperty("BOOLEAN").thatContains("als");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaContainsMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "BOOLEAN"));

        final ExperimentSearchCriteria criteriaLTMatch = new ExperimentSearchCriteria();
        criteriaLTMatch.withProperty("BOOLEAN").thatIsLessThan("true");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaLTMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "LessThan", "BOOLEAN"));

        final ExperimentSearchCriteria criteriaLEMatch = new ExperimentSearchCriteria();
        criteriaLEMatch.withProperty("BOOLEAN").thatIsLessThanOrEqualTo("true");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaLEMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "LessThanOrEqualTo", "BOOLEAN"));

        final ExperimentSearchCriteria criteriaGTMatch = new ExperimentSearchCriteria();
        criteriaGTMatch.withProperty("BOOLEAN").thatIsGreaterThan("true");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaGTMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "GreaterThan", "BOOLEAN"));

        final ExperimentSearchCriteria criteriaGEMatch = new ExperimentSearchCriteria();
        criteriaGEMatch.withProperty("BOOLEAN").thatIsGreaterThanOrEqualTo("true");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaGEMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "GreaterThanOrEqualTo", "BOOLEAN"));
    }

    @Test
    public void testSearchNumeric()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId integerPropertyType = createAnIntegerPropertyType(sessionToken, "INT_NUMBER");
        final PropertyTypePermId realPropertyType = createARealPropertyType(sessionToken, "REAL_NUMBER");
        final EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, integerPropertyType,
                realPropertyType);

        final ExperimentCreation experimentCreation1 = getExperimentCreation(experimentType, 1, 0.01);
        final ExperimentCreation experimentCreation2 = getExperimentCreation(experimentType, 2, 0.02);
        final ExperimentCreation experimentCreation3 = getExperimentCreation(experimentType, 3, 0.03);

        v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation1, experimentCreation2,
                experimentCreation3));

        final ExperimentFetchOptions emptyFetchOptions = new ExperimentFetchOptions();

        // Greater or Equal - Integer
        final ExperimentSearchCriteria criteriaGE = new ExperimentSearchCriteria();
        criteriaGE.withNumberProperty("INT_NUMBER").thatIsGreaterThanOrEqualTo(2);
        final List<Experiment> experimentsGE = search(sessionToken, criteriaGE, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsGE, "/CISD/DEFAULT/TEST_2", "/CISD/DEFAULT/TEST_3");

        // Greater or Equal - Integer as Real
        final ExperimentSearchCriteria criteriaGEIR = new ExperimentSearchCriteria();
        criteriaGEIR.withNumberProperty("INT_NUMBER").thatIsGreaterThanOrEqualTo(2.0);
        final List<Experiment> experimentsGEIR = search(sessionToken, criteriaGEIR, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsGEIR, "/CISD/DEFAULT/TEST_2", "/CISD/DEFAULT/TEST_3");

        // Greater or Equal - Real
        final ExperimentSearchCriteria criteriaGER = new ExperimentSearchCriteria();
        criteriaGER.withNumberProperty("REAL_NUMBER").thatIsGreaterThanOrEqualTo(0.02);
        final List<Experiment> experimentsGER = search(sessionToken, criteriaGER, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsGER, "/CISD/DEFAULT/TEST_2", "/CISD/DEFAULT/TEST_3");

        // Greater - Integer
        final ExperimentSearchCriteria criteriaG = new ExperimentSearchCriteria();
        criteriaG.withNumberProperty("INT_NUMBER").thatIsGreaterThan(2);
        final List<Experiment> experimentsG = search(sessionToken, criteriaG, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsG, "/CISD/DEFAULT/TEST_3");

        // Greater - Integer as Real
        final ExperimentSearchCriteria criteriaGIR = new ExperimentSearchCriteria();
        criteriaGIR.withNumberProperty("INT_NUMBER").thatIsGreaterThan(2.0);
        final List<Experiment> experimentsGIR = search(sessionToken, criteriaGIR, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsGIR, "/CISD/DEFAULT/TEST_3");

        // Greater - Real
        final ExperimentSearchCriteria criteriaGR = new ExperimentSearchCriteria();
        criteriaGR.withNumberProperty("REAL_NUMBER").thatIsGreaterThan(0.02);
        final List<Experiment> experimentsGR = search(sessionToken, criteriaGR, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsGR, "/CISD/DEFAULT/TEST_3");

        // Equal - Integer
        final ExperimentSearchCriteria criteriaE = new ExperimentSearchCriteria();
        criteriaE.withNumberProperty("INT_NUMBER").thatEquals(2);
        final List<Experiment> experimentsE = search(sessionToken, criteriaE, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsE, "/CISD/DEFAULT/TEST_2");

        // Equal - Integer as String
        final ExperimentSearchCriteria criteriaES = new ExperimentSearchCriteria();
        criteriaES.withProperty("INT_NUMBER").thatEquals("2");
        final List<Experiment> experimentsES = search(sessionToken, criteriaES, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsES, "/CISD/DEFAULT/TEST_2");

        // Equal - Integer as Real as String
        final ExperimentSearchCriteria criteriaERS = new ExperimentSearchCriteria();
        criteriaERS.withProperty("INT_NUMBER").thatEquals("2.0");
        final List<Experiment> experimentsERS = search(sessionToken, criteriaERS, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsERS, "/CISD/DEFAULT/TEST_2");

        // Greater or Equal - Integer
        final ExperimentSearchCriteria criteriaLE = new ExperimentSearchCriteria();
        criteriaLE.withNumberProperty("INT_NUMBER").thatIsLessThanOrEqualTo(2);
        final List<Experiment> experimentsLE = search(sessionToken, criteriaLE, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsLE, "/CISD/DEFAULT/TEST_1", "/CISD/DEFAULT/TEST_2");

        // Less or Equal - Real
        final ExperimentSearchCriteria criteriaLER = new ExperimentSearchCriteria();
        criteriaLER.withNumberProperty("REAL_NUMBER").thatIsLessThanOrEqualTo(0.02);
        final List<Experiment> experimentsLER = search(sessionToken, criteriaLER, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsLER, "/CISD/DEFAULT/TEST_1", "/CISD/DEFAULT/TEST_2");

        // Less - Integer
        final ExperimentSearchCriteria criteriaL = new ExperimentSearchCriteria();
        criteriaL.withNumberProperty("INT_NUMBER").thatIsLessThan(2);
        final List<Experiment> experimentsL = search(sessionToken, criteriaL, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsL, "/CISD/DEFAULT/TEST_1");

        // Less - Integer as Real
        final ExperimentSearchCriteria criteriaLIR = new ExperimentSearchCriteria();
        criteriaLIR.withNumberProperty("INT_NUMBER").thatIsLessThan(2.0);
        final List<Experiment> experimentsLIR = search(sessionToken, criteriaLIR, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsLIR, "/CISD/DEFAULT/TEST_1");

        // Less - Real
        final ExperimentSearchCriteria criteriaLR = new ExperimentSearchCriteria();
        criteriaLR.withNumberProperty("REAL_NUMBER").thatIsLessThan(0.02);
        final List<Experiment> experimentsLR = search(sessionToken, criteriaLR, emptyFetchOptions);
        assertExperimentIdentifiersInOrder(experimentsLR, "/CISD/DEFAULT/TEST_1");
    }

    @Test
    public void testSearchForExperimentWithDatePropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createADatePropertyType(sessionToken, "DATE");
        final EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);

        final ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("DATE_PROPERTY_TEST");
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty("DATE", "2020-02-09");

        v3api.createExperiments(sessionToken, Collections.singletonList(experimentCreation));

        final ExperimentSearchCriteria criteriaContainsMatch = new ExperimentSearchCriteria();
        criteriaContainsMatch.withProperty("DATE").thatContains("02");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaContainsMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "DATE"));

        final ExperimentSearchCriteria criteriaStartsWithMatch = new ExperimentSearchCriteria();
        criteriaStartsWithMatch.withProperty("DATE").thatStartsWith("2020");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaStartsWithMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "DATE"));

        final ExperimentSearchCriteria criteriaEndsWithMatch = new ExperimentSearchCriteria();
        criteriaEndsWithMatch.withProperty("DATE").thatEndsWith("09");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaEndsWithMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "DATE"));
    }

    @Test
    public void testSearchForExperimentWithTimestampPropertyMatchingSubstring()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createATimestampPropertyType(sessionToken, "TIMESTAMP");
        final EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);

        final ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("TIMESTAMP_PROPERTY_TEST");
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty("TIMESTAMP", "2020-02-09 10:00:00 +0100");

        v3api.createExperiments(sessionToken, Collections.singletonList(experimentCreation));

        final ExperimentSearchCriteria criteriaContainsMatch = new ExperimentSearchCriteria();
        criteriaContainsMatch.withProperty("TIMESTAMP").thatContains("20");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaContainsMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "Contains", "TIMESTAMP"));

        final ExperimentSearchCriteria criteriaStartsWithMatch = new ExperimentSearchCriteria();
        criteriaStartsWithMatch.withProperty("TIMESTAMP").thatStartsWith("2020");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaStartsWithMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "StartsWith", "TIMESTAMP"));

        final ExperimentSearchCriteria criteriaEndsWithMatch = new ExperimentSearchCriteria();
        criteriaEndsWithMatch.withProperty("TIMESTAMP").thatEndsWith("0100");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaEndsWithMatch, new ExperimentFetchOptions()),
                String.format("Operator %s undefined for datatype %s.", "EndsWith", "TIMESTAMP"));
    }

    @Test
    public void testSearchForExperimentWithStringPropertyQueriedAsIntegerOrDate()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypePermId propertyType = createAVarcharPropertyType(sessionToken, "SHORT_TEXT");
        final EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);

        final ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("SHORT_TEXT_PROPERTY_TEST");
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty("SHORT_TEXT", "123");

        v3api.createExperiments(sessionToken, Collections.singletonList(experimentCreation));

        final ExperimentSearchCriteria criteriaWithNumberProperty = new ExperimentSearchCriteria();
        criteriaWithNumberProperty.withNumberProperty("SHORT_TEXT").thatEquals(123);
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaWithNumberProperty, new ExperimentFetchOptions()),
                String.format("Criterion of type %s cannot be applied to the data type %s.",
                        "NumberPropertySearchCriteria", "VARCHAR"));

        final ExperimentSearchCriteria criteriaWithDateProperty = new ExperimentSearchCriteria();
        criteriaWithDateProperty.withDateProperty("SHORT_TEXT").thatEquals("1990-11-09");
        assertUserFailureException(
                Void -> searchExperiments(sessionToken, criteriaWithDateProperty, new ExperimentFetchOptions()),
                String.format("Criterion of type %s cannot be applied to the data type %s.",
                        "DatePropertySearchCriteria", "VARCHAR"));
    }

    @Test
    public void testNestedLogicalOperators()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria().withAndOperator();

        final ExperimentSearchCriteria subCriteria1 = criteria.withSubcriteria().withOrOperator();
        subCriteria1.withCode().thatStartsWith("EXP-");
        subCriteria1.withCode().thatStartsWith("EXP1");

        final ExperimentSearchCriteria subCriteria2 = criteria.withSubcriteria().withOrOperator();
        subCriteria2.withCode().thatEndsWith("E");
        subCriteria2.withCode().thatEndsWith("1");

        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP1", "/CISD/DEFAULT/EXP-REUSE", "/CISD/NEMO/EXP-TEST-1",
                "/CISD/NEMO/EXP11");
    }

    @Test
    public void testNestedLogicalOperatorsMultipleNesting()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria().withAndOperator();

        final ExperimentSearchCriteria subCriteria1 = criteria.withSubcriteria().withOrOperator();
        subCriteria1.withSubcriteria().withSubcriteria().withCode().thatStartsWith("EXP-");
        subCriteria1.withSubcriteria().withSubcriteria().withSubcriteria().withSubcriteria().withCode().thatStartsWith("EXP1");

        final ExperimentSearchCriteria subCriteria2 = criteria.withSubcriteria().withOrOperator();
        subCriteria2.withSubcriteria().withCode().thatEndsWith("E");
        subCriteria2.withSubcriteria().withSubcriteria().withCode().thatEndsWith("1");

        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP1", "/CISD/DEFAULT/EXP-REUSE", "/CISD/NEMO/EXP-TEST-1",
                "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithPermIdWithAttributeFullTextSearch()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria().withAndOperator();

        criteria.withTextAttribute().thatMatches("exp-test-1 exp-test-2 experiment-to-delete");
        criteria.withPermId().thatContains("8-");

        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithPermIdWithPropertyFullTextSearch()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria().withOrOperator();
        criteria.withProperty("DESCRIPTION").thatMatches("desc1");
        criteria.withCode().thatEquals("EXP1");

        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP1");
    }

    @Test
    public void testSearchWithPermIdWithStringPropertyFullTextSearch()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria().withOrOperator();
        criteria.withStringProperty("DESCRIPTION").thatMatches("desc1");
        criteria.withCode().thatEquals("EXP1");

        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP1");
    }

    @Test
    public void testSearchWithPermIdWithAnyPropertyFullTextSearch()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyProperty().thatMatches("experiment");
        criteria.withPermId().thatStartsWith("2008");

        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP1", "/CISD/DEFAULT/EXP-REUSE",
                "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithPermIdWithAnyStringPropertyFullTextSearch()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyStringProperty().thatMatches("experiment");
        criteria.withPermId().thatStartsWith("2008");

        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP1", "/CISD/DEFAULT/EXP-REUSE",
                "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithPermIdWithAnyFieldFullTextSearch()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
        criteria.withAnyField().thatMatches("experiment exp-x exp-y exp-wells");
        criteria.withPermId().thatStartsWith("2008");

        testSearch(TEST_USER, criteria, "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP1", "/CISD/DEFAULT/EXP-REUSE",
                "/CISD/NEMO/EXP11", "/CISD/DEFAULT/EXP-WELLS");
    }

    @Test
    public void testSearchWithAttributeNegation()
    {
        final ExperimentSearchCriteria criteria = new ExperimentSearchCriteria().withAndOperator();
        criteria.withCode().thatContains("-TEST-");
        criteria.withSubcriteria().negate().withCode().thatEndsWith("1");

        testSearch(TEST_USER, criteria, "/CISD/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2", "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithStringPropertyNegation()
    {
        final ExperimentSearchCriteria criteria1 = new ExperimentSearchCriteria().withAndOperator();
        criteria1.withStringProperty("DESCRIPTION").thatContains("esc");
        criteria1.withSubcriteria().negate().withStringProperty("DESCRIPTION").thatContains("esc1");

        testSearch(TEST_USER, criteria1, "/CISD/NOE/EXP-TEST-2");

        final ExperimentSearchCriteria criteria2 = new ExperimentSearchCriteria().withAndOperator();
        criteria2.withStringProperty("DESCRIPTION").thatEndsWith("experiment");
        criteria2.withSubcriteria().negate().withStringProperty("DESCRIPTION").thatContains("test");

        testSearch(TEST_USER, criteria2, "/CISD/DEFAULT/EXP-Y", "/CISD/NEMO/EXP1", "/CISD/NEMO/EXP10",
                "/CISD/NEMO/EXP11");
    }

    public ExperimentCreation getExperimentCreation(final EntityTypePermId experimentType, final int intValue,
            final double realValue)
    {
        final ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("TEST_" + intValue);
        experimentCreation.setTypeId(experimentType);
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        experimentCreation.setProperty("INT_NUMBER", String.valueOf(intValue));
        experimentCreation.setProperty("REAL_NUMBER", String.valueOf(realValue));
        return experimentCreation;
    }

    private List<Experiment> search(final String sessionToken, final ExperimentSearchCriteria criteria,
            final ExperimentFetchOptions options)
    {
        return v3api.searchExperiments(sessionToken, criteria, options).getObjects();
    }

    private void testSearch(String user, ExperimentSearchCriteria criteria, String... expectedIdentifiers)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        List<Experiment> experiments = searchExperiments(sessionToken, criteria, new ExperimentFetchOptions());

        assertExperimentIdentifiers(experiments, expectedIdentifiers);
    }

    private void testSearch(String user, ExperimentSearchCriteria criteria, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        List<Experiment> experiments = searchExperiments(sessionToken, criteria, new ExperimentFetchOptions());

        assertEquals(experiments.size(), expectedCount);
    }

    private List<Experiment> searchExperiments(String sessionToken, ExperimentSearchCriteria criteria,
            ExperimentFetchOptions fetchOptions)
    {
        SearchResult<Experiment> searchResult = v3api.searchExperiments(sessionToken, criteria, fetchOptions);
        v3api.logout(sessionToken);
        return searchResult.getObjects();
    }

}
