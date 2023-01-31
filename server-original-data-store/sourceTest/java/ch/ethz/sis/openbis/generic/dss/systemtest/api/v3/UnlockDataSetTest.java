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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.lock.DataSetLockOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unlock.DataSetUnlockOptions;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author Franz-Josef Elmer
 *
 */
public class UnlockDataSetTest extends AbstractArchiveUnarchiveDataSetTest
{
    @Test
    public void testUnlockUnlockedDataSet() throws Exception
    {
        // Given
        String sessionToken = v3.login(TEST_USER, PASSWORD);
        registerDataSet();
        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        assertEquals(ArchivingStatus.AVAILABLE, getDataSet(sessionToken, dataSetId).getPhysicalData().getStatus());
        DataSetUnlockOptions options = new DataSetUnlockOptions();
        
        // When
        v3.unlockDataSets(sessionToken, Arrays.asList(dataSetId), options);
        
        // Then
        DataSet dataSet = getDataSet(sessionToken, dataSetId);
        assertEquals(ArchivingStatus.AVAILABLE, dataSet.getPhysicalData().getStatus());
        
        v3.logout(sessionToken);
    }
    
    @Test
    public void testUnlockLockedDataSet() throws Exception
    {
        // Given
        String sessionToken = v3.login(TEST_USER, PASSWORD);
        registerDataSet();
        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        v3.lockDataSets(sessionToken, Arrays.asList(dataSetId), new DataSetLockOptions());
        assertEquals(ArchivingStatus.LOCKED, getDataSet(sessionToken, dataSetId).getPhysicalData().getStatus());
        DataSetUnlockOptions options = new DataSetUnlockOptions();
        
        // When
        v3.unlockDataSets(sessionToken, Arrays.asList(dataSetId), options);
        
        // Then
        DataSet dataSet = getDataSet(sessionToken, dataSetId);
        assertEquals(ArchivingStatus.AVAILABLE, dataSet.getPhysicalData().getStatus());
        
        v3.logout(sessionToken);
    }

    @Test
    public void testUnlockArchivedDataSet() throws Exception
    {
        // Given
        String sessionToken = v3.login(TEST_USER, PASSWORD);
        registerDataSet();
        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        v3.archiveDataSets(sessionToken, Arrays.asList(dataSetId), new DataSetArchiveOptions());
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.ARCHIVED);
        
        // When
        v3.unlockDataSets(sessionToken, Arrays.asList(dataSetId), new DataSetUnlockOptions());
        
        // Then
        waitUntilDataSetStatus(dataSetCode, ArchivingStatus.ARCHIVED);
        
        v3.logout(sessionToken);
    }
    
    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Object with DataSetPermId = \\[IDONTEXIST\\] has not been found.*")
    public void testUnlockWithNonexistentDataSet() throws Exception
    {
        DataSetPermId dataSetId = new DataSetPermId("IDONTEXIST");
        DataSetUnlockOptions options = new DataSetUnlockOptions();
        String sessionToken = v3.login(TEST_USER, PASSWORD);

        v3.unlockDataSets(sessionToken, Arrays.asList(dataSetId), options);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*test_space does not have enough privileges.*")
    public void testUnlockWithUnauthorizedDataSet() throws Exception
    {
        // Given
        registerDataSet();
        DataSetPermId dataSetId = new DataSetPermId(dataSetCode);
        DataSetUnlockOptions options = new DataSetUnlockOptions();
        String sessionToken = v3.login(TEST_SPACE_USER, PASSWORD);

        // When
        v3.unlockDataSets(sessionToken, Arrays.asList(dataSetId), options);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testUnlockWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        // Given
        String sessionToken = v3.login(user.getUserId(), PASSWORD);
        IDataSetId dataSetId = new DataSetPermId("20120628092259000-41");
        DataSetUnlockOptions options = new DataSetUnlockOptions();

        // When & Then
        if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            v3.unlockDataSets(sessionToken, Arrays.asList(dataSetId), options);
        } else
        {
            try
            {
                v3.unlockDataSets(sessionToken, Arrays.asList(dataSetId), options);
                fail();
            } catch (Exception e)
            {
                assertEquals(AuthorizationFailureException.class, e.getCause().getClass());
            }
        }
    }

}
