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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.lock.DataSetLockOptions;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class ArchiveDataSetTest extends AbstractArchiveUnarchiveDataSetTest
{

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Object with DataSetPermId = \\[IDONTEXIST\\] has not been found.*")
    public void testArchiveWithNonexistentDataSet() throws Exception
    {
        DataSetPermId dataSetId = new DataSetPermId("IDONTEXIST");
        DataSetArchiveOptions options = new DataSetArchiveOptions();

        String sessionToken = v3.login(TEST_USER, PASSWORD);
        v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*test_space does not have enough privileges.*")
    public void testArchiveWithUnauthorizedDataSet() throws Exception
    {
        registerDataSet();

        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        DataSetArchiveOptions options = new DataSetArchiveOptions();

        String sessionToken = v3.login(TEST_SPACE_USER, PASSWORD);
        v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
    }

    @Test
    public void testArchiveLockedDataSet() throws Exception
    {
        // Given
        registerDataSet();
        String sessionToken = v3.login(TEST_USER, PASSWORD);
        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        v3.lockDataSets(sessionToken, Arrays.asList(dataSetId), new DataSetLockOptions());
        DataSetArchiveOptions options = new DataSetArchiveOptions();
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.LOCKED);

        // Then
        v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), options);

        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.LOCKED);
    }

    @Test
    public void testArchiveWithRemoveFromStoreTrue() throws Exception
    {
        String sessionToken = v3.login(TEST_USER, PASSWORD);
        
        registerDataSet();
        
        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        DataSetArchiveOptions options = new DataSetArchiveOptions();
        
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.AVAILABLE);
        v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.ARCHIVED);
    }
    
    @Test
    public void testArchiveWithRemoveFromStoreFalse() throws Exception
    {
        String sessionToken = v3.login(TEST_USER, PASSWORD);

        registerDataSet();

        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        DataSetArchiveOptions options = new DataSetArchiveOptions();
        options.setRemoveFromDataStore(false);

        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.AVAILABLE);
        v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.AVAILABLE);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testArchiveWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3.login(user.getUserId(), PASSWORD);

        IDataSetId dataSetId = new DataSetPermId("20120628092259000-41");
        DataSetArchiveOptions options = new DataSetArchiveOptions();

        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
        } else
        {
            try
            {
                v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), options);
                fail();
            } catch (Exception e)
            {
                assertEquals(e.getCause().getClass(), AuthorizationFailureException.class);
            }
        }
    }

}
