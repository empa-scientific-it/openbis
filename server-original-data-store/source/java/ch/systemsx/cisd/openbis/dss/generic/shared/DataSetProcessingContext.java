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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;

/**
 * Context for processing data sets by a {@link IProcessingPluginTask}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetProcessingContext
{
    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetProcessingContext.class);

    private final Map<String, String> parameterBindings;

    private final IMailClient mailClient;

    private final String userEmailOrNull;

    private final IDataSetDirectoryProvider directoryProvider;

    private final IHierarchicalContentProvider hierarchicalContentProvider;

    /**
     * Session token of a user on behalf of who, the script is executed.
     */
    private final String sessionTokenOrNull;

    private final ISessionWorkspaceProvider sessionWorkspaceProviderOrNull;

    private final String userId;

    private final IEncapsulatedOpenBISService service;

    /**
     * Creates an instance for specified directory provider, parameter bindings, e-mail client, and optional user e-mail address and sessionToken.
     */
    // This method is only used in tests.
    public DataSetProcessingContext(IHierarchicalContentProvider contentProvider,
            IDataSetDirectoryProvider directoryProvider, Map<String, String> parameterBindings,
            IMailClient mailClient, String userId, String userEmailOrNull)
    {
        this(null, contentProvider, directoryProvider, null, parameterBindings, mailClient, userId,
                userEmailOrNull, null);
    }

    /**
     * Creates an instance for specified directory provider, parameter bindings, e-mail client, and optional user e-mail address and sessionToken.
     */
    public DataSetProcessingContext(IHierarchicalContentProvider contentProvider,
            IDataSetDirectoryProvider directoryProvider, Map<String, String> parameterBindings,
            IMailClient mailClient, String userId, String userEmailOrNull, String sessionTokenOrNull)
    {
        this(contentProvider, directoryProvider, null, parameterBindings, mailClient, userId,
                userEmailOrNull, sessionTokenOrNull);
    }

    /**
     * Creates an instance for specified directory provider, workspace provider, parameter bindings, e-mail client, and optional user e-mail address
     * and sessionToken.
     */
    public DataSetProcessingContext(IHierarchicalContentProvider contentProvider,
            IDataSetDirectoryProvider directoryProvider,
            ISessionWorkspaceProvider sessionWorkspaceProviderOrNull,
            Map<String, String> parameterBindings, IMailClient mailClient, String userId,
            String userEmailOrNull, String sessionTokenOrNull)
    {
        this(ServiceProvider.getOpenBISService(), contentProvider, directoryProvider,
                sessionWorkspaceProviderOrNull, parameterBindings, mailClient, userId,
                userEmailOrNull, sessionTokenOrNull);
    }

    /**
     * Creates an instance for specified service, directory provider, workspace provider, parameter bindings, e-mail client, and optional user e-mail
     * address and sessionToken.
     */
    private DataSetProcessingContext(IEncapsulatedOpenBISService service,
            IHierarchicalContentProvider contentProvider,
            IDataSetDirectoryProvider directoryProvider,
            ISessionWorkspaceProvider sessionWorkspaceProviderOrNull,
            Map<String, String> parameterBindings, IMailClient mailClient, String userId,
            String userEmailOrNull, String sessionTokenOrNull)
    {
        this.service = service;
        this.hierarchicalContentProvider = contentProvider;
        this.directoryProvider = directoryProvider;
        this.parameterBindings = parameterBindings;
        this.mailClient = mailClient;
        this.userId = userId;
        this.userEmailOrNull = userEmailOrNull;
        this.sessionTokenOrNull = sessionTokenOrNull;
        this.sessionWorkspaceProviderOrNull = sessionWorkspaceProviderOrNull;
    }

    public IDataSetDirectoryProvider getDirectoryProvider()
    {
        return directoryProvider;
    }

    /**
     * Returns the session workspace provider of this context, if available and <code>null</code> if this context has no session workspace provider.
     */
    public ISessionWorkspaceProvider tryGetSessionWorkspaceProvider()
    {
        return sessionWorkspaceProviderOrNull;
    }

    public final Map<String, String> getParameterBindings()
    {
        return parameterBindings;
    }

    public final IMailClient getMailClient()
    {
        return mailClient;
    }

    public final String getUserId()
    {
        return userId;
    }

    public final String getUserEmailOrNull()
    {
        return userEmailOrNull;
    }

    public String trySessionToken()
    {
        return sessionTokenOrNull;
    }

    public IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        final IHierarchicalContentProvider contentProvider = getContentProvider();
        return new IHierarchicalContentProvider()
            {

                @Override
                public IHierarchicalContent asContent(AbstractExternalData dataSet)
                {
                    assertAuthorization(dataSet.getCode());
                    return contentProvider.asContent(dataSet);
                }

                @Override
                public IHierarchicalContent asContent(String dataSetCode)
                        throws IllegalArgumentException
                {
                    assertAuthorization(dataSetCode);
                    return contentProvider.asContent(dataSetCode);
                }

                @Override
                public IHierarchicalContent asContentWithoutModifyingAccessTimestamp(String dataSetCode)
                {
                    assertAuthorization(dataSetCode);
                    return contentProvider.asContentWithoutModifyingAccessTimestamp(dataSetCode);
                }

                @Override
                public IHierarchicalContent asContentWithoutModifyingAccessTimestamp(AbstractExternalData dataSet)
                {
                    assertAuthorization(dataSet.getCode());
                    return asContentWithoutModifyingAccessTimestamp(dataSet);
                }

                @SuppressWarnings("deprecation")
                @Override
                public IHierarchicalContent asContent(File datasetDirectory)
                {
                    return contentProvider.asContent(datasetDirectory);
                }

                @SuppressWarnings("deprecation")
                @Override
                public IHierarchicalContent asContent(IDatasetLocation datasetLocation)
                {
                    return contentProvider.asContent(datasetLocation);
                }

                @Override
                public IHierarchicalContentProvider cloneFor(
                        ISessionTokenProvider sessionTokenProvider)
                {
                    return contentProvider.cloneFor(sessionTokenProvider);
                }

            };
    }

    private IHierarchicalContentProvider getContentProvider()
    {
        if (sessionTokenOrNull == null)
        {
            return hierarchicalContentProvider;
        }
        OpenBISSessionHolder sessionHolder = new OpenBISSessionHolder();
        sessionHolder.setSessionToken(sessionTokenOrNull);
        final IHierarchicalContentProvider contentProvider =
                hierarchicalContentProvider.cloneFor(sessionHolder);
        return contentProvider;
    }

    public IHierarchicalContentProvider getHierarchicalContentProviderUnfiltered()
    {
        return hierarchicalContentProvider;
    }

    private void assertAuthorization(String dataSetCode)
    {
        if (sessionTokenOrNull == null)
        {
            operationLog.warn("Undefined user session token. Skip authorization check for data set " + dataSetCode + ".");
            return;
        }
        if (service == null)
        {
            operationLog.warn("Undefined service. Skip authorization check for data set " + dataSetCode + ".");
            return;
        }
        service.checkDataSetAccess(sessionTokenOrNull, dataSetCode);
    }
}
