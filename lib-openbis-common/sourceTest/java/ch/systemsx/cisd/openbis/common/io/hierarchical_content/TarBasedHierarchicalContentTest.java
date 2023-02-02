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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.filesystem.tar.Tar;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;

/**
 * @author pkupczyk
 */
public class TarBasedHierarchicalContentTest extends AbstractPackageBasedHierarchicalContentTest
{

    @Override
    protected IHierarchicalContent createPackage(File packageFile, File dataDir) throws Exception
    {
        Tar tar = null;
        try
        {
            tar = new Tar(packageFile);
            tar.add(dataDir, dataDir.getPath().length());
            List<H5FolderFlags> h5FolderFlags = Arrays.asList(new H5FolderFlags("", true, true));
            return new TarBasedHierarchicalContent(packageFile, h5FolderFlags, null, 4096, null);
        } finally
        {
            if (tar != null)
            {
                tar.close();
            }
        }
    }
}
