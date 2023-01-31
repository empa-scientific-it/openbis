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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;

/**
 * @author pkupczyk
 */
public class PathInfoDBOnlyHierarchicalContentFactory extends
        DefaultFileBasedHierarchicalContentFactory
{

    public static IHierarchicalContentFactory create()
    {
        if (PathInfoDataSourceProvider.isDataSourceDefined())
        {
            return new PathInfoDBOnlyHierarchicalContentFactory(
                    ServiceProvider.getDataSetPathInfoProvider());
        } else
        {
            return null;
        }
    }

    private final IDataSetPathInfoProvider pathInfoProvider;

    @Private
    PathInfoDBOnlyHierarchicalContentFactory(IDataSetPathInfoProvider pathInfoProvider)
    {
        this.pathInfoProvider = pathInfoProvider;
    }

    @Override
    public IHierarchicalContent asHierarchicalContent(File file, IDelegatedAction onCloseAction)
    {
        final String dataSetCode = file.getName();
        ISingleDataSetPathInfoProvider dataSetPathInfoProvider =
                pathInfoProvider.tryGetSingleDataSetPathInfoProvider(dataSetCode);
        if (dataSetPathInfoProvider != null) // data set exists in DB
        {
            return new PathInfoProviderBasedHierarchicalContent(dataSetPathInfoProvider, file,
                    onCloseAction);
        } else
        {
            return null;
        }
    }

}
