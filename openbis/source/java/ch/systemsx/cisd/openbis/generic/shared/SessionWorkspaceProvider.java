/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.pat.IPersonalAccessTokenConverter;

/**
 * @author pkupczyk
 */
@Component(value = SessionWorkspaceProvider.INTERNAL_SERVICE_NAME)
public class SessionWorkspaceProvider implements ISessionWorkspaceProvider
{

    public static final String INTERNAL_SERVICE_NAME = "session-workspace-provider";

    public static final String SESSION_WORKSPACE_ROOT_DIR_KEY = "session-workspace-root-dir";

    public static final String SESSION_WORKSPACE_ROOT_DIR_DEFAULT = "sessionWorkspace";

    public static final String SESSION_WORKSPACE_SHREDDER_QUEUE_FILE = ".shredder";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, SessionWorkspaceProvider.class);

    private Properties serviceProperties;

    private IPersonalAccessTokenConverter personalAccessTokenConverter;

    private File sessionWorkspaceRootDir;

    public SessionWorkspaceProvider()
    {
    }

    SessionWorkspaceProvider(Properties serviceProperties, IPersonalAccessTokenConverter personalAccessTokenConverter)
    {
        this.serviceProperties = serviceProperties;
        this.personalAccessTokenConverter = personalAccessTokenConverter;
    }

    @PostConstruct
    void init() throws Exception
    {
        String sessionWorkspaceRootDirString =
                PropertyUtils.getProperty(serviceProperties, SESSION_WORKSPACE_ROOT_DIR_KEY, SESSION_WORKSPACE_ROOT_DIR_DEFAULT);
        sessionWorkspaceRootDir = new File(sessionWorkspaceRootDirString);

        operationLog.info("Session workspace root dir '" + sessionWorkspaceRootDir.getCanonicalPath() + "'");

        if (false == sessionWorkspaceRootDir.exists())
        {
            sessionWorkspaceRootDir.mkdirs();
        }

        QueueingPathRemoverService.start(sessionWorkspaceRootDir, new File(SESSION_WORKSPACE_SHREDDER_QUEUE_FILE));

        operationLog.info("Session workspace shredder service started");
    }

    @Override
    public Map<String, File> getSessionWorkspaces()
    {
        File[] sessionWorkspaces = sessionWorkspaceRootDir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return false == file.isHidden();
                }
            });

        Map<String, File> map = new TreeMap<String, File>();

        if (sessionWorkspaces != null)
        {
            for (File sessionWorkspace : sessionWorkspaces)
            {
                map.put(sessionWorkspace.getName(), sessionWorkspace);
            }
        }

        return map;
    }

    @Override
    public File getSessionWorkspace(String sessionTokenOrPAT)
    {
        String sessionToken = personalAccessTokenConverter.convert(sessionTokenOrPAT);
        File sessionWorkspace = new File(sessionWorkspaceRootDir, sessionToken);

        if (false == sessionWorkspace.exists())
        {
            sessionWorkspace.mkdirs();
            operationLog.info("Session workspace created");
        }

        return sessionWorkspace;
    }

    @Override
    public void deleteSessionWorkspace(String sessionTokenOrPAT)
    {
        try
        {
            String sessionToken = personalAccessTokenConverter.convert(sessionTokenOrPAT);
            File sessionWorkspace = new File(sessionWorkspaceRootDir, sessionToken);

            if (sessionWorkspace.exists())
            {
                QueueingPathRemoverService.removeRecursively(sessionWorkspace);
                operationLog.info("Session workspace added to shredder queue");
            }
        } catch (Exception e)
        {
            operationLog.warn("Session workspace could not be shredded", e);
        }
    }

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private void setServicePropertiesPlaceholder(ExposablePropertyPlaceholderConfigurer servicePropertiesPlaceholder)
    {
        serviceProperties = servicePropertiesPlaceholder.getResolvedProps();
    }

    @Autowired
    private void setPersonalAccessTokenConverter(final IPersonalAccessTokenConverter personalAccessTokenConverter)
    {
        this.personalAccessTokenConverter = personalAccessTokenConverter;
    }
}
