/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.ADMIN_USER_TECH_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_CODE_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_ID_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_ID_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_REGISTRATION_DATE_2;
import static junit.framework.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ExperimentSearchManagerDBTest
{
    private static final AbstractApplicationContext context = new ClassPathXmlApplicationContext("testApplicationContext.xml");

    private DBTestHelper dbTestHelper = context.getBean(DBTestHelper.class);

    private ExperimentSearchManager searchManager;

    public ExperimentSearchManagerDBTest() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }

    @BeforeClass
    public void setUpClass() throws Exception
    {
        searchManager = context.getBean("experiment-search-manager", ExperimentSearchManager.class);
    }

    @AfterClass
    public void tearDownClass() throws Exception
    {
        context.registerShutdownHook();
    }

    @AfterMethod
    public void tearDown() throws Exception
    {
        dbTestHelper.resetConnection();
    }

    /**
     * Tests {@link SampleSearchManager} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCode()
    {
        final ExperimentSearchCriteria equalsCriterion = new ExperimentSearchCriteria();
        equalsCriterion.withCode().thatEquals(EXPERIMENT_CODE_1);
        checkCodeCriterion(equalsCriterion);

        final ExperimentSearchCriteria containsCriterion = new ExperimentSearchCriteria();
        containsCriterion.withCode().thatContains(EXPERIMENT_CODE_1.substring(1, EXPERIMENT_CODE_1.length() - 1));
        checkCodeCriterion(containsCriterion);

        final ExperimentSearchCriteria startsWithCriterion = new ExperimentSearchCriteria();
        startsWithCriterion.withCode().thatStartsWith(EXPERIMENT_CODE_1.substring(0, 4));
        checkCodeCriterion(startsWithCriterion);

        final ExperimentSearchCriteria endsWithCriterion = new ExperimentSearchCriteria();
        endsWithCriterion.withCode().thatEndsWith(EXPERIMENT_CODE_1.substring(4));
        checkCodeCriterion(endsWithCriterion);
    }

    /**
     * Checks if the criterion returns expected results.
     *
     * @param criterion criterion to be checked.
     */
    private void checkCodeCriterion(final ExperimentSearchCriteria criterion)
    {
        final Set<Long> sampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertEquals(sampleIds.size(), 1);
        assertEquals(sampleIds.iterator().next().longValue(), EXPERIMENT_ID_1);
    }

    /**
     * Tests {@link SampleSearchManager} with {@link RegistrationDateSearchCriteria} search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithRegistrationDateField()
    {
        final ExperimentSearchCriteria equalsCriterion = new ExperimentSearchCriteria();
        equalsCriterion.withRegistrationDate().thatEquals(EXPERIMENT_REGISTRATION_DATE_2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(EXPERIMENT_ID_2));

        final ExperimentSearchCriteria earlierThanCriterion = new ExperimentSearchCriteria();
        earlierThanCriterion.withRegistrationDate().thatIsEarlierThanOrEqualTo(EXPERIMENT_REGISTRATION_DATE_2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertFalse(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_3));

        final ExperimentSearchCriteria laterThanCriterion = new ExperimentSearchCriteria();
        laterThanCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(EXPERIMENT_REGISTRATION_DATE_2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertTrue(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_3));
    }

}
