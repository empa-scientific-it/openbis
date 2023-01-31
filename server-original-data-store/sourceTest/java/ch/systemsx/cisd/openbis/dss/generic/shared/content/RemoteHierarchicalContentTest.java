/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocationNode;

/**
 * @author Franz-Josef Elmer
 */
public class RemoteHierarchicalContentTest extends AbstractRemoteHierarchicalContentTestCase
{

    @Test
    public void testGetDefaultRootNode() throws Exception
    {
        ContentCache cache = createCache();
        RemoteHierarchicalContent content = createContent(cache);

        context.checking(new Expectations()
            {
                {
                    one(pathInfoProvider).getRootPathInfo();
                }
            });

        IHierarchicalContentNode rootNode = content.getRootNode();

        assertEquals(true, rootNode.isDirectory());
        assertEquals("", rootNode.getRelativePath());
        assertEquals(null, rootNode.getParentRelativePath());
        assertEquals("", rootNode.getName());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetRootNode() throws Exception
    {
        ContentCache cache = createCache();
        RemoteHierarchicalContent content = createContent(cache);

        context.checking(new Expectations()
            {
                {
                    one(pathInfoProvider).getRootPathInfo();
                    DataSetPathInfo pathInfo = new DataSetPathInfo();
                    pathInfo.setDirectory(true);
                    pathInfo.setRelativePath(remoteFile1.getName());
                    pathInfo.setFileName("root");
                    will(returnValue(pathInfo));

                    one(remoteDss).getDownloadUrlForFileForDataSet(SESSION_TOKEN, DATA_SET_CODE,
                            pathInfo.getRelativePath());
                    will(returnValue(remoteFile1.toURI().toURL().toString()));

                    one(persistenceManager).requestPersistence();
                }
            });

        IHierarchicalContentNode rootNode = content.getRootNode();

        File file = rootNode.getFile();
        assertEquals(new File(workSpace, ContentCache.CACHE_FOLDER + "/" + DATA_SET_CODE + "/"
                + remoteFile1.getName()).getAbsolutePath(), file.getAbsolutePath());
        assertEquals(FILE1_CONTENT, FileUtilities.loadToString(file).trim());
        context.assertIsSatisfied();
    }

    private RemoteHierarchicalContent createContent(ContentCache cache)
    {
        return new RemoteHierarchicalContent(new DatasetLocationNode(DATA_SET_LOCATION),
                pathInfoProvider, serviceFactory, sessionHolder, cache);
    }

}
