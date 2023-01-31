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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.H5FolderFlags;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractPackageManager implements IPackageManager
{
    private transient IHierarchicalContentProvider contentProvider;

    private transient IDataSetDirectoryProvider directoryProvider;

    @Override
    public void create(File packageFile, AbstractExternalData dataSet)
    {
        create(packageFile, Collections.singletonList(dataSet), false);
    }

    @Override
    public void create(File packageFile, List<AbstractExternalData> dataSets)
    {
        create(packageFile, dataSets, true);
    }

    private void create(File packageFile, List<AbstractExternalData> dataSets, boolean withPathPrefix)
    {
        AbstractDataSetPackager packager = null;

        try
        {
            DataSetExistenceChecker existenceChecker =
                    new DataSetExistenceChecker(getDirectoryProvider(), TimingParameters.create(new Properties()));
            packager = createPackager(packageFile, existenceChecker);

            for (AbstractExternalData dataSet : dataSets)
            {
                packager.addDataSetTo(withPathPrefix ? dataSet.getCode() + "/" : "", dataSet);
            }
        } finally
        {
            if (packager != null)
            {
                packager.close();
            }
        }
    }

    protected abstract AbstractDataSetPackager createPackager(File packageFile, DataSetExistenceChecker existenceChecker);

    protected IHierarchicalContentProvider getContentProvider()
    {
        if (contentProvider == null)
        {
            contentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return contentProvider;
    }

    private IDataSetDirectoryProvider getDirectoryProvider()
    {
        if (directoryProvider == null)
        {
            directoryProvider = ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
        }
        return directoryProvider;
    }

    protected List<H5FolderFlags> extractH5FolderFlags(List<DatasetDescription> dataSets)
    {
        List<H5FolderFlags> result = new ArrayList<>();
        for (DatasetDescription dataSet : dataSets)
        {
            result.add(new H5FolderFlags(dataSet.getDataSetCode(), dataSet.isH5Folders(), dataSet.isH5arFolders()));
        }
        return result;
    }
}
