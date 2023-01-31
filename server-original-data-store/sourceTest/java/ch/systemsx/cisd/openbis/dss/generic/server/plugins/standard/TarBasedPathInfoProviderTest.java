/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.filesystem.tar.Tar;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.MockLogger;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.H5FolderFlags;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * @author Franz-Josef Elmer
 */
public class TarBasedPathInfoProviderTest extends AbstractFileSystemTestCase
{
    private static final File TEST_DATA_FOLDER = new File("../datastore_server/resource/test-data/"
            + TarBasedPathInfoProviderTest.class.getSimpleName());

    private static final SimpleComparator<DataSetPathInfo, String> COMPARATOR = new SimpleComparator<DataSetPathInfo, String>()
        {
            @Override
            public String evaluate(DataSetPathInfo item)
            {
                return item.getRelativePath();
            }
        };

    private ISimpleLogger logger;

    private TarBasedPathInfoProvider pathInfoProvider;

    private File tarFile;

    @BeforeMethod
    public void prepareExample() throws Exception
    {
        logger = new MockLogger();
        tarFile = new File(workingDirectory, "data.tar");
        Tar tar = null;
        try
        {
            tar = new Tar(tarFile);
            File file = new File(TEST_DATA_FOLDER, "ds1");
            tar.add(file, file.getPath().length());
        } finally
        {
            if (tar != null)
            {
                tar.close();
            }
        }
        pathInfoProvider = new TarBasedPathInfoProvider(tarFile, Arrays.asList(), 10, logger);
    }

    @Test
    public void testGetRootPathInfo()
    {
        DataSetPathInfo pathInfo = pathInfoProvider.getRootPathInfo();

        assertPathInfo("data[data, 0]", pathInfo);
    }

    @Test
    public void testTryGetPathInfoByRelativePath()
    {
        assertEquals(null, pathInfoProvider.tryGetPathInfoByRelativePath("blabla"));
        assertPathInfo("data[data, 0]", pathInfoProvider.tryGetPathInfoByRelativePath("data"));
        assertPathInfo("data/hello.txt[hello.txt, 11, d4a1185]",
                pathInfoProvider.tryGetPathInfoByRelativePath("data/hello.txt"));
    }

    @Test
    public void testListChildrenPathInfos()
    {
        DataSetPathInfo parent = pathInfoProvider.tryGetPathInfoByRelativePath("data/sub");
        List<DataSetPathInfo> children = pathInfoProvider.listChildrenPathInfos(parent);
        Collections.sort(children, COMPARATOR);

        assertPathInfo("data/sub/hello.txt[hello.txt, 10, 3d4448e7]", children.get(0));
        assertPathInfo("data/sub/some.txt[some.txt, 38, 2f7b8cfb]", children.get(1));
        AssertionUtil.assertContains("INFO: Reading statistics for input stream: 15.86 KB in 1626 chunks took < 1sec.",
                logger.toString());
        AssertionUtil.assertContains("INFO: Writing statistics for output stream: 15.86 KB in 1626 chunks took < 1sec.",
                logger.toString());
        assertEquals(2, children.size());
    }

    @Test
    public void testListMatchingPathInfos()
    {
        List<DataSetPathInfo> pathInfos = pathInfoProvider.listMatchingPathInfos(".*hello.*");
        Collections.sort(pathInfos, COMPARATOR);

        assertPathInfo("data/hello.txt[hello.txt, 11, d4a1185]", pathInfos.get(0));
        assertPathInfo("data/sub/hello.txt[hello.txt, 10, 3d4448e7]", pathInfos.get(1));
        assertEquals(2, pathInfos.size());
    }

    @Test
    public void testListMatchingPathInfos2()
    {
        List<DataSetPathInfo> pathInfos = pathInfoProvider.listMatchingPathInfos("data/sub/", ".*hello.*");
        Collections.sort(pathInfos, COMPARATOR);

        assertPathInfo("data/sub/hello.txt[hello.txt, 10, 3d4448e7]", pathInfos.get(0));
        assertEquals(1, pathInfos.size());
    }
    
    @Test
    public void testHdf5FileAsFile()
    {
        DataSetPathInfo pathInfo = pathInfoProvider.tryGetPathInfoByRelativePath("data.h5ar");
        
        assertEquals(false, pathInfo.isDirectory());
        assertPathInfo("data.h5ar[data.h5ar, 16184, 83bcffb0]", pathInfo);
    }
    
    @Test
    public void testHdf5FileAsFolder()
    {
        List<H5FolderFlags> flags = Arrays.asList(new H5FolderFlags("", false, true));
        TarBasedPathInfoProvider provider = new TarBasedPathInfoProvider(tarFile, flags, 10, logger);
        
        DataSetPathInfo parent = provider.tryGetPathInfoByRelativePath("data.h5ar/data");
        List<DataSetPathInfo> children = provider.listChildrenPathInfos(parent);
        
        Collections.sort(children, COMPARATOR);
        assertPathInfo("data.h5ar/data/hello.txt[hello.txt, 11, d4a1185]", children.get(0));
        assertPathInfo("data.h5ar/data/sub[sub, 0, 0]", children.get(1));
        assertEquals(2, children.size());
    }

    private void assertPathInfo(String expectedPathInfo, DataSetPathInfo pathInfo)
    {
        assertNotNull("Unspecified path info", pathInfo);
        StringBuilder builder = new StringBuilder();
        builder.append(pathInfo.getRelativePath()).append('[').append(pathInfo.getFileName()).append(", ");
        builder.append(pathInfo.getSizeInBytes());
        Integer checksum = pathInfo.getChecksumCRC32();
        if (checksum != null)
        {
            builder.append(", ").append(Integer.toHexString(checksum));
        }
        builder.append(']');
        assertEquals(expectedPathInfo, builder.toString());
    }

}
