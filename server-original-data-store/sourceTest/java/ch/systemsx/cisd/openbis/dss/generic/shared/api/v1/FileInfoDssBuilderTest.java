/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class FileInfoDssBuilderTest extends AssertJUnit
{
    @Test
    public void testFileInfoBuilderOnDir() throws IOException
    {
        String root =
                "../datastore_server/sourceTest/java/ch/systemsx/cisd/openbis/dss/generic/shared/api/v1";
        FileInfoDssBuilder builder = new FileInfoDssBuilder(root, root);
        ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
        File requestedFile = new File(root);
        builder.appendFileInfosForFile(requestedFile, list, false);
        removeSvnFoldersFromList(list);
        // The 3 test files + 1 sub dir
        assertEquals(4, list.size());
    }

    private void removeSvnFoldersFromList(ArrayList<FileInfoDssDTO> list)
    {
        // Copy the list since we will modify it
        for (FileInfoDssDTO info : new ArrayList<FileInfoDssDTO>(list))
        {
            if (info.getPathInDataSet().endsWith(".svn"))
            {
                list.remove(info);
            }
        }
    }

    @Test
    public void testFileInfoBuilderOnDirWithTrailingSlash() throws IOException
    {
        String root =
                "../datastore_server/sourceTest/java/ch/systemsx/cisd/openbis/dss/generic/shared/api/v1/";
        FileInfoDssBuilder builder = new FileInfoDssBuilder(root, root);
        ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
        File requestedFile = new File(root);
        builder.appendFileInfosForFile(requestedFile, list, false);
        removeSvnFoldersFromList(list);
        // The 3 test files + 1 sub dir
        assertEquals(4, list.size());
    }

    @Test
    public void testFileInfoBuilderOnSingleFile() throws IOException
    {
        String root = "sourceTest/java/ch/systemsx/cisd/openbis/dss/generic/shared/api/v1/";
        FileInfoDssBuilder builder = new FileInfoDssBuilder(root, root);
        ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
        File requestedFile = new File(new File(root), "FileInfoDssBuilderTest.java");
        builder.appendFileInfosForFile(requestedFile, list, false);
        assertEquals(1, list.size());
    }
}
