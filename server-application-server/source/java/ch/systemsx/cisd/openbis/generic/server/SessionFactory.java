/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.ISessionFactory;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session.ISessionCleaner;

/**
 * Factory of {@link Session} objects.
 * 
 * @author Franz-Josef Elmer
 */
public final class SessionFactory implements ISessionFactory<Session>
{
    private final static BlockingQueue<CleanUpTask> sessionsToBeClosedQueue = new LinkedBlockingQueue<>();

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SessionFactory.class);

    private final IDataStoreDAO datastoreDAO;

    private final IDataStoreServiceFactory dssFactory;

    private final ISessionWorkspaceProvider sessionWorkspaceProvider;

    static
    {
        Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (true)
                    {
                        try
                        {
                            CleanUpTask task = sessionsToBeClosedQueue.take();
                            task.execute();
                        } catch (InterruptedException ex)
                        {
                            // Exit thread.
                        } catch (InterruptedExceptionUnchecked ex)
                        {
                            // Exit thread.
                        }
                    }
                }
            }, "Session clean up queue");
        thread.setDaemon(true);
        thread.start();
    }

    public SessionFactory()
    {
        this(null, null, null);
    }

    public SessionFactory(IDAOFactory daoFactory, IDataStoreServiceFactory dssFactory, ISessionWorkspaceProvider sessionWorkspaceProvider)
    {
        this.datastoreDAO = (daoFactory != null) ? daoFactory.getDataStoreDAO() : null;
        this.dssFactory = dssFactory;
        this.sessionWorkspaceProvider = sessionWorkspaceProvider;
    }

    //
    // ISessionFactory
    //

    @Override
    public final Session create(final String sessionToken, final String userName,
            final Principal principal, final String remoteHost, final long sessionStart,
            final long expirationTime)
    {
        return create(sessionToken, userName, principal, remoteHost, sessionStart, expirationTime, false, null);
    }

    @Override public Session create(final String sessionToken, final String userName, final Principal principal, final String remoteHost,
            final long sessionStart, final long sessionExpirationTime, final boolean isPersonalAccessTokenSession,
            final String personalAccessTokenSessionName)
    {
        final Session session =
                new Session(userName, sessionToken, principal, remoteHost, sessionStart, sessionExpirationTime, isPersonalAccessTokenSession,
                        personalAccessTokenSessionName);
        if (datastoreDAO != null && dssFactory != null)
        {
            session.addCleanupListener(new ISessionCleaner()
            {
                @Override
                public void cleanup()
                {
                    sessionWorkspaceProvider.deleteSessionWorkspace(sessionToken);
                    cleanUpSessionOnDataStoreServers(sessionToken, datastoreDAO, dssFactory);
                }
            });
        }
        return session;
    }

    public static void cleanUpSessionOnDataStoreServers(String sessionToken,
            IDataStoreDAO datastoreDAO, IDataStoreServiceFactory dssFactory)
    {
        for (DataStorePE datastore : datastoreDAO.listDataStores())
        {
            final String remoteUrl = datastore.getRemoteUrl();
            if (StringUtils.isBlank(remoteUrl) == false)
            {
                sessionsToBeClosedQueue.add(new CleanUpTask(dssFactory, remoteUrl, sessionToken));
            } else
            {
                operationLog.warn("datastore remoteUrl of datastore " + datastore.getCode()
                        + " is empty - skipping DSS session cleanup.");
            }
        }
    }

    private static final class CleanUpTask
    {
        private IDataStoreServiceFactory dssFactory;

        private String remoteUrl;

        private String sessionToken;

        CleanUpTask(IDataStoreServiceFactory dssFactory, String remoteUrl, String sessionToken)
        {
            this.dssFactory = dssFactory;
            this.remoteUrl = remoteUrl;
            this.sessionToken = sessionToken;
        }

        void execute()
        {
            try
            {
                operationLog.info("Execute clean-up task for session " + sessionToken + " on DSS " + remoteUrl);
                dssFactory.createMonitored(remoteUrl, LogLevel.WARN).cleanupSession(sessionToken);
                operationLog.info("Clean-up task for session " + sessionToken + " finished on DSS " + remoteUrl);
            } catch (Exception e)
            {
                operationLog.error("Clean-up task for session " + sessionToken + " failed on DSS " + remoteUrl
                        + ". Reason: " + e, e);
            }
        }
    }
}
