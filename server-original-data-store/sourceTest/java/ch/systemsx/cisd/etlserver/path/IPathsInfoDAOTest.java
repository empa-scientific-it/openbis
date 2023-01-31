/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.path;

import java.util.Date;
import java.util.List;

import net.lemnik.eodsql.QueryTool;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.SystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;

/**
 * @author pkupczyk
 */
public class IPathsInfoDAOTest extends SystemTestCase
{

    @Test
    public void testListDataSetsSize()
    {
        IPathsInfoDAO dao = QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);

        long dataSetId = dao.createDataSet("DATA_SET_WITH_SIZE", "abc");
        long rootDirectoryId = dao.createDataSetFile(dataSetId, null, "", "root", 123L, true, null, null, new Date());
        dao.createDataSetFile(dataSetId, rootDirectoryId, "root", "file1.txt", 100L, false, null, null, new Date());
        dao.createDataSetFile(dataSetId, rootDirectoryId, "root", "file2.txt", 23L, false, null, null, new Date());

        List<PathEntryDTO> entries = dao.listDataSetsSize(new String[] { "DATA_SET_WITH_SIZE" });

        Assert.assertEquals(1, entries.size());
        Assert.assertEquals(Long.valueOf(123L), entries.get(0).getSizeInBytes());
    }

}
