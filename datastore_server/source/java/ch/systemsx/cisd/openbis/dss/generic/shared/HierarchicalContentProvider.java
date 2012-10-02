/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.PathInfoDBAwareHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;

/**
 * The default implementation of {@link IHierarchicalContentProvider}.
 * 
 * @author Piotr Buczek
 */
public class HierarchicalContentProvider implements IHierarchicalContentProvider
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            HierarchicalContentProvider.class);

    private final IEncapsulatedOpenBISService openbisService;

    private final IDataSetDirectoryProvider directoryProvider;

    private IHierarchicalContentFactory hierarchicalContentFactory;

    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IShareIdManager shareIdManager, IConfigProvider configProvider)
    {
        this(openbisService, new DataSetDirectoryProvider(configProvider.getStoreRoot(),
                shareIdManager), null);
    }

    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IShareIdManager shareIdManager, IConfigProvider configProvider,
            IHierarchicalContentFactory hierarchicalContentFactory)
    {
        this(openbisService, new DataSetDirectoryProvider(configProvider.getStoreRoot(),
                shareIdManager), hierarchicalContentFactory);
    }

    @Private
    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IDataSetDirectoryProvider directoryProvider,
            IHierarchicalContentFactory hierarchicalContentFactory)
    {
        this.openbisService = openbisService;
        this.directoryProvider = directoryProvider;
        this.hierarchicalContentFactory = hierarchicalContentFactory;
    }

    @Override
    public IHierarchicalContent asContent(String dataSetCode)
    {
        IDatasetLocationNode locationNode = openbisService.tryGetDataSetLocation(dataSetCode);
        if (locationNode == null)
        {
            operationLog.error(String.format("Data set '%s' not found in openBIS server.",
                    dataSetCode));
            throw new IllegalArgumentException("Unknown data set: " + dataSetCode);
        }
        return asContent(locationNode);
    }

    @Override
    public IHierarchicalContent asContent(ExternalData dataSet)
    {
        return asContent(new ExternalDataLocationNode(dataSet));
    }

    private IHierarchicalContent asContent(IDatasetLocationNode locationNode)
    {
        if (locationNode.isContainer())
        {
            List<IHierarchicalContent> componentContents = new ArrayList<IHierarchicalContent>();
            for (IDatasetLocationNode component : locationNode.getComponents())
            {
                IHierarchicalContent componentContent = tryCreateComponentContent(component);
                if (componentContent != null)
                {
                    componentContents.add(componentContent);
                }
            }
            return getHierarchicalContentFactory().asVirtualHierarchicalContent(componentContents);
        } else
        {
            return asContent(locationNode.getLocation());
        }
    }

    private IHierarchicalContent tryCreateComponentContent(
            IDatasetLocationNode componentLocationNode)
    {
        try
        {
            if (componentLocationNode.isContainer())
            {
                return asContent(componentLocationNode);
            } else
            {
                return asContent(componentLocationNode.getLocation());
            }
        } catch (IllegalArgumentException ex)
        {
            operationLog
                    .info("ignoring contained data set "
                            + componentLocationNode.getLocation().getDataSetCode() + ": "
                            + ex.getMessage());
            return null;
        }
    }

    @Override
    public IHierarchicalContent asContent(final IDatasetLocation datasetLocation)
    {
        // NOTE: remember to call IHierarchicalContent.close() to unlock the data set when finished
        // working with the IHierarchivalContent
        directoryProvider.getShareIdManager().lock(datasetLocation.getDataSetCode());
        File dataSetDirectory = directoryProvider.getDataSetDirectory(datasetLocation);
        IDelegatedAction onCloseAction = new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    directoryProvider.getShareIdManager().releaseLock(
                            datasetLocation.getDataSetCode());
                }
            };
        return asContent(dataSetDirectory, onCloseAction);
    }

    @Override
    public IHierarchicalContent asContent(File dataSetDirectory)
    {
        return getHierarchicalContentFactory().asHierarchicalContent(dataSetDirectory,
                IDelegatedAction.DO_NOTHING);
    }

    public IHierarchicalContent asContent(File dataSetDirectory, IDelegatedAction onCloseAction)
    {
        try
        {
            return getHierarchicalContentFactory().asHierarchicalContent(dataSetDirectory,
                    onCloseAction);
        } catch (RuntimeException ex)
        {
            onCloseAction.execute();
            throw ex;
        }
    }

    private IHierarchicalContentFactory getHierarchicalContentFactory()
    {
        if (hierarchicalContentFactory == null)
        {
            hierarchicalContentFactory = PathInfoDBAwareHierarchicalContentFactory.create();
        }
        return hierarchicalContentFactory;
    }

}
