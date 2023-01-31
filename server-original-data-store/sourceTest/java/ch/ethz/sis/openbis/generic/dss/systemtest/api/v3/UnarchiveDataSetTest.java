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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class UnarchiveDataSetTest extends AbstractArchiveUnarchiveDataSetTest
{

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Object with DataSetPermId = \\[IDONTEXIST\\] has not been found.*")
    public void testUnarchiveWithNonexistentDataSet() throws Exception
    {
        DataSetPermId dataSetId = new DataSetPermId("IDONTEXIST");
        DataSetUnarchiveOptions options = new DataSetUnarchiveOptions();

        String sessionToken = v3.login(TEST_USER, PASSWORD);
        v3.unarchiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*test_space does not have enough privileges.*")
    public void testUnarchiveWithUnauthorizedDataSet() throws Exception
    {
        registerDataSet();

        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        DataSetUnarchiveOptions options = new DataSetUnarchiveOptions();

        String sessionToken = v3.login(TEST_SPACE_USER, PASSWORD);
        v3.unarchiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
    }

    @Test
    public void testUnarchive() throws Exception
    {
        registerDataSet();

        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        DataSetArchiveOptions archiveOptions = new DataSetArchiveOptions();
        DataSetUnarchiveOptions unarchiveOptions = new DataSetUnarchiveOptions();

        String sessionToken = v3.login(TEST_USER, PASSWORD);

        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.AVAILABLE);
        v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), archiveOptions);
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.ARCHIVED);
        v3.unarchiveDataSets(sessionToken, Arrays.asList(dataSetId), unarchiveOptions);
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.AVAILABLE);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testUnarchiveWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String adminSessionToken = v3.login(TEST_USER, PASSWORD);
        String sessionToken = v3.login(user.getUserId(), PASSWORD);

        IDataSetId dataSetId = new DataSetPermId("20120628092259000-41");
        DataSetArchiveOptions archiveOptions = new DataSetArchiveOptions();
        DataSetUnarchiveOptions unarchiveOptions = new DataSetUnarchiveOptions();

        v3.archiveDataSets(adminSessionToken, Arrays.asList(dataSetId), archiveOptions);

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            v3.unarchiveDataSets(sessionToken, Arrays.asList(dataSetId), unarchiveOptions);
        } else
        {
            try
            {
                v3.unarchiveDataSets(sessionToken, Arrays.asList(dataSetId), unarchiveOptions);
                fail();
            } catch (Exception e)
            {
                assertEquals(e.getCause().getClass(), AuthorizationFailureException.class);
            }
        }
    }

}
