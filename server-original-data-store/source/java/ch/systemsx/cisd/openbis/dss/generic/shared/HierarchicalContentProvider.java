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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.HierarchicalContentProxy;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentExecuteOnAccess;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.DssServiceRpcGenericFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.IContentCache;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.IDssServiceRpcGenericFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.PathInfoDBAwareHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.PathInfoDBOnlyHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.RemoteHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSetLocation;

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

    private IHierarchicalContentFactory hierarchicalContentFactoryForLinkedData;
    
    private final ISessionTokenProvider sessionTokenProvider;

    private final String dataStoreCode;

    private final boolean trustAllCertificates;

    private final IContentCache cache;

    private final IDssServiceRpcGenericFactory serviceFactory;

    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IShareIdManager shareIdManager, IConfigProvider configProvider,
            IContentCache contentCache,
            ISessionTokenProvider sessionTokenProvider,
            ExposablePropertyPlaceholderConfigurer infoProvider)
    {
        this(openbisService, new DataSetDirectoryProvider(configProvider.getStoreRoot(),
                shareIdManager), null, new DssServiceRpcGenericFactory(), contentCache,
                sessionTokenProvider, configProvider.getDataStoreCode(), infoProvider);
    }

    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IShareIdManager shareIdManager, IConfigProvider configProvider,
            IContentCache contentCache, IHierarchicalContentFactory hierarchicalContentFactory,
            IDssServiceRpcGenericFactory serviceFactory,
            ISessionTokenProvider sessionTokenProvider,
            ExposablePropertyPlaceholderConfigurer infoProvider)
    {
        this(openbisService, new DataSetDirectoryProvider(configProvider.getStoreRoot(),
                shareIdManager), hierarchicalContentFactory, serviceFactory, contentCache,
                sessionTokenProvider, configProvider.getDataStoreCode(), infoProvider);
    }

    @Private
    public HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IDataSetDirectoryProvider directoryProvider,
            IHierarchicalContentFactory hierarchicalContentFactory,
            IDssServiceRpcGenericFactory serviceFactory, IContentCache contentCache,
            ISessionTokenProvider sessionTokenProvider, String dataStoreCode,
            ExposablePropertyPlaceholderConfigurer infoProvider)
    {
        this(openbisService, directoryProvider, hierarchicalContentFactory, serviceFactory,
                contentCache, sessionTokenProvider, dataStoreCode, infoProvider != null
                        && "true".equalsIgnoreCase(infoProvider.getResolvedProps().getProperty(
                                "trust-all-certificates")));
    }

    private HierarchicalContentProvider(IEncapsulatedOpenBISService openbisService,
            IDataSetDirectoryProvider directoryProvider,
            IHierarchicalContentFactory hierarchicalContentFactory,
            IDssServiceRpcGenericFactory serviceFactory, IContentCache contentCache,
            ISessionTokenProvider sessionTokenProvider, String dataStoreCode, boolean trustAllCertificates)
    {
        this.openbisService = openbisService;
        this.directoryProvider = directoryProvider;
        this.hierarchicalContentFactory = hierarchicalContentFactory;
        this.serviceFactory = serviceFactory;
        this.cache = contentCache;
        this.sessionTokenProvider = sessionTokenProvider;
        this.dataStoreCode = dataStoreCode;
        this.trustAllCertificates = trustAllCertificates;

    }

    @Override
    public IHierarchicalContentProvider cloneFor(ISessionTokenProvider anotherSessionTokenProvider)
    {
        return new HierarchicalContentProvider(openbisService, directoryProvider,
                hierarchicalContentFactory, serviceFactory, cache, anotherSessionTokenProvider,
                dataStoreCode, trustAllCertificates);
    }

    @Override
    public IHierarchicalContent asContent(String dataSetCode)
    {
        return asContent(dataSetCode, true);
    }

    @Override
    public IHierarchicalContent asContentWithoutModifyingAccessTimestamp(String dataSetCode) throws IllegalArgumentException
    {
        return asContent(dataSetCode, false);
    }

    private IHierarchicalContent asContent(String dataSetCode, boolean shouldUPdateAccessTimestamp)
    {
        IDatasetLocationNode locationNode = openbisService.tryGetDataSetLocation(dataSetCode);
        if (locationNode == null)
        {
            operationLog.error(String.format("Data set '%s' not found in openBIS server.",
                    dataSetCode));
            throw new IllegalArgumentException("Unknown data set: " + dataSetCode);
        }

        return asContent(locationNode, shouldUPdateAccessTimestamp);
    }

    @Override
    public IHierarchicalContent asContent(AbstractExternalData dataSet)
    {
        return asContent(new ExternalDataLocationNode(dataSet), true);
    }

    @Override
    public IHierarchicalContent asContentWithoutModifyingAccessTimestamp(AbstractExternalData dataSet)
    {
        return asContent(new ExternalDataLocationNode(dataSet), false);
    }

    private IHierarchicalContent asContent(final IDatasetLocationNode locationNode, final boolean shouldUpdateAccessTimestamp)
    {
        if (isLocal(locationNode))
        {
            if (locationNode.isContainer())
            {
                List<IHierarchicalContent> componentContents = new ArrayList<IHierarchicalContent>();
                final String containerDataSetCode = locationNode.getLocation().getDataSetCode();
                List<IDatasetLocationNode> sortedNodes = new ArrayList<IDatasetLocationNode>(locationNode.getComponents());
                Collections.sort(sortedNodes, new Comparator<IDatasetLocationNode>()
                    {
                        @Override
                        public int compare(IDatasetLocationNode n1, IDatasetLocationNode n2)
                        {
                            return getOrderInContainer(n1) - getOrderInContainer(n2);
                        }

                        private int getOrderInContainer(IDatasetLocationNode node)
                        {
                            Integer orderInContainer = node.getLocation().getOrderInContainer(containerDataSetCode);
                            return orderInContainer == null ? 0 : orderInContainer;
                        }
                    });
                for (final IDatasetLocationNode component : sortedNodes)
                {
                    final IDatasetLocation location = component.getLocation();
                    IHierarchicalContentExecuteOnAccess onAccess = new IHierarchicalContentExecuteOnAccess()
                        {
                            @Override
                            public void execute()
                            {
                                if (shouldUpdateAccessTimestamp)
                                {
                                    openbisService.notifyDatasetAccess(location.getDataSetCode());
                                }
                            }
                        };
                    IHierarchicalContent componentContent =
                            HierarchicalContentProxy.getProxyFor(tryCreateComponentContent(component), Arrays.asList(onAccess));

                    if (componentContent != null)
                    {
                        componentContents.add(new DataSetAwareHierarchicalContent(location, componentContent));
                    }
                }
                return getHierarchicalContentFactory().asVirtualHierarchicalContent(componentContents);
            } else
            {
                IHierarchicalContentExecuteOnAccess onAccess = new IHierarchicalContentExecuteOnAccess()
                    {
                        @Override
                        public void execute()
                        {
                            if (shouldUpdateAccessTimestamp)
                            {
                                openbisService.notifyDatasetAccess(locationNode.getLocation().getDataSetCode());
                            }
                        }
                    };
                IHierarchicalContent asContent = HierarchicalContentProxy.getProxyFor(asContent(locationNode.getLocation()), Arrays.asList(onAccess));
                return asContent;
            }
        } else
        {

            ISingleDataSetPathInfoProvider provider = null;
            if (PathInfoDataSourceProvider.isDataSourceDefined())
            {
                IDataSetPathInfoProvider dataSetPathInfoProvider = ServiceProvider.getDataSetPathInfoProvider();

                provider =
                        dataSetPathInfoProvider.tryGetSingleDataSetPathInfoProvider(locationNode
                                .getLocation().getDataSetCode());
            }

            if (trustAllCertificates)
            {
                SslCertificateHelper.trustAnyCertificate(locationNode.getLocation()
                        .getDataStoreUrl());
            }

            IHierarchicalContentExecuteOnAccess onAccess = new IHierarchicalContentExecuteOnAccess()
                {
                    @Override
                    public void execute()
                    {
                        if (shouldUpdateAccessTimestamp)
                        {
                            openbisService.notifyDatasetAccess(locationNode.getLocation().getDataSetCode());
                        }
                    }
                };

            IHierarchicalContent hierarchicalContentProxy = HierarchicalContentProxy.getProxyFor(
                    new RemoteHierarchicalContent(locationNode, provider, serviceFactory, sessionTokenProvider, cache), Arrays.asList(onAccess));
            return hierarchicalContentProxy;
        }
    }

    private boolean isLocal(IDatasetLocationNode node)
    {
        return this.dataStoreCode.equals(node.getLocation().getDataStoreCode());
    }

    private IHierarchicalContent tryCreateComponentContent(IDatasetLocationNode componentLocationNode)
    {
        try
        {
            if (componentLocationNode.isContainer())
            {
                return asContent(componentLocationNode, true);
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
        IHierarchicalContentFactory contentFactory = datasetLocation instanceof LinkDataSetLocation ?
                getHierarchicalContentFactoryForLinkedData() :
                getHierarchicalContentFactory();
        try
        {
            return contentFactory.asHierarchicalContent(dataSetDirectory, onCloseAction);
        } catch (RuntimeException ex)
        {
            onCloseAction.execute();
            throw ex;
        }
    }

    @Override
    public IHierarchicalContent asContent(File dataSetDirectory)
    {
        return getHierarchicalContentFactory().asHierarchicalContent(dataSetDirectory,
                IDelegatedAction.DO_NOTHING);
    }

    private IHierarchicalContentFactory getHierarchicalContentFactory()
    {
        if (hierarchicalContentFactory == null)
        {
            hierarchicalContentFactory = PathInfoDBAwareHierarchicalContentFactory.create();
        }
        return hierarchicalContentFactory;
    }

    private IHierarchicalContentFactory getHierarchicalContentFactoryForLinkedData()
    {
        if (hierarchicalContentFactoryForLinkedData == null)
        {
            hierarchicalContentFactoryForLinkedData = PathInfoDBOnlyHierarchicalContentFactory.create();
        }
        return hierarchicalContentFactoryForLinkedData;
    }
    
}
