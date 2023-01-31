/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;

import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Simple content provider for unit tests.
 *
 * @author Franz-Josef Elmer
 */
public class SimpleFileContentProvider implements IHierarchicalContentProvider
{
    private final File root;

    public SimpleFileContentProvider(File root)
    {
        this.root = root;
    }

    @Override
    public IHierarchicalContent asContent(String dataSetCode)
    {
        File dataSetFolder = new File(root, dataSetCode);
        return new DefaultFileBasedHierarchicalContentFactory().asHierarchicalContent(
                dataSetFolder, IDelegatedAction.DO_NOTHING);
    }

    @Override
    public IHierarchicalContent asContent(File datasetDirectory)
    {
        return null;
    }

    @Override
    public IHierarchicalContent asContentWithoutModifyingAccessTimestamp(String dataSetCode)
    {
        return asContent(dataSetCode);
    }

    @Override
    public IHierarchicalContent asContent(IDatasetLocation datasetLocation)
    {
        return null;
    }

    @Override
    public IHierarchicalContent asContentWithoutModifyingAccessTimestamp(AbstractExternalData dataSet)
    {
        return null;
    }

    @Override
    public IHierarchicalContent asContent(AbstractExternalData dataSet)
    {
        return null;
    }

    @Override
    public IHierarchicalContentProvider cloneFor(ISessionTokenProvider sessionTokenProvider)
    {
        return null;
    }
}