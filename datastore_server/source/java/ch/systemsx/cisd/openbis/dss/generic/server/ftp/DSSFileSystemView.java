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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.ResolverContext;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.util.Key;

/**
 * A central class that manages the movement of a user up and down the exposed hierarchical structure.
 * 
 * @author Kaloyan Enimanev
 */
public class DSSFileSystemView implements FileSystemView
{
    private static final Set<String> METHOD_NAMES = new HashSet<String>(Arrays.asList(
            "tryToGetExperiment", "listDataSetsByExperimentID"));

    private final class ServiceInvocationHandler implements InvocationHandler
    {
        private final Map<Key, Object> objectCache = new HashMap<Key, Object>();

        private final IServiceForDataStoreServer openbisService;

        private ServiceInvocationHandler(IServiceForDataStoreServer service)
        {
            this.openbisService = service;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable
        {
            if (METHOD_NAMES.contains(method.getName()))
            {
                Key key = new Key(args);
                Object result = objectCache.get(key);
                if (result == null)
                {
                    result = invoke(method, args);
                    objectCache.put(key, result);
                }
                return result;
            } else
            {
                return invoke(method, args);
            }
        }

        private Object invoke(Method method, Object[] args) throws IllegalAccessException,
                InvocationTargetException
        {
            return method.invoke(openbisService, args);
        }
    }

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DSSFileSystemView.class);

    private final String sessionToken;

    private final IServiceForDataStoreServer service;

    private final IGeneralInformationService generalInfoService;

    private final IApplicationServerApi v3api;

    private FtpFile workingDirectory;

    private final IFtpPathResolverRegistry pathResolverRegistry;

    private Cache cache;

    public DSSFileSystemView(String sessionToken, final IServiceForDataStoreServer service,
            IGeneralInformationService generalInfoService, IApplicationServerApi v3api,
            IFtpPathResolverRegistry pathResolverRegistry) throws FtpException
    {
        this(sessionToken, service, generalInfoService, v3api, pathResolverRegistry,
                new Cache(SystemTimeProvider.SYSTEM_TIME_PROVIDER));
    }

    public DSSFileSystemView(String sessionToken, final IServiceForDataStoreServer service,
            IGeneralInformationService generalInfoService, IApplicationServerApi v3api,
            IFtpPathResolverRegistry pathResolverRegistry, Cache cache) throws FtpException
    {
        this.sessionToken = sessionToken;
        this.cache = cache;
        this.service =
                (IServiceForDataStoreServer) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { IServiceForDataStoreServer.class },
                        new ServiceInvocationHandler(service));
        this.generalInfoService = generalInfoService;
        this.v3api = v3api;
        this.pathResolverRegistry = pathResolverRegistry;
        this.workingDirectory = getFile(FtpConstants.ROOT_DIRECTORY);
    }

    @Override
    public boolean changeWorkingDirectory(String path) throws FtpException
    {
        FtpFile ftpFile = getFile(path);
        if (ftpFile != null && ftpFile.isDirectory())
        {
            workingDirectory = ftpFile;
            return true;
        }
        return false;
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public FtpFile getFile(String path) throws FtpException
    {
        String normalizedPath = normalizePath(path);
        operationLog.info("path:>" + path + "<, normalized path:>"+normalizedPath+"<");

        // this check speeds directory listings in the LFTP console client
        if (workingDirectory != null && workingDirectory.getAbsolutePath().equals(normalizedPath))
        {
            return workingDirectory;
        }

        try
        {
            FtpPathResolverContext context =
                    new FtpPathResolverContext(sessionToken, service, generalInfoService, v3api,
                            pathResolverRegistry, cache, path);
            return pathResolverRegistry.resolve(normalizedPath, context);
        } catch (RuntimeException rex)
        {
            Throwable realThrowable = CheckedExceptionTunnel.unwrapIfNecessary(rex);
            String message =
                    String.format("Error while resolving FTP path '%s' : %s", path,
                            realThrowable.getMessage());
            if (realThrowable instanceof UserFailureException == false)
            {
                operationLog.error(message, realThrowable);
            }
            throw new FtpException(message);
        }
    }

    private String normalizePath(String path) throws FtpException
    {
        String fullPath = path.trim();
        if (fullPath.startsWith("./"))
        {
            fullPath = fullPath.substring(2);
        }
        if (fullPath.startsWith(FtpConstants.FILE_SEPARATOR) == false)
        {
            String absolutePath = workingDirectory.getAbsolutePath();
            if (absolutePath.endsWith(FtpConstants.FILE_SEPARATOR) == false)
            {
                absolutePath += FtpConstants.FILE_SEPARATOR;
            }
            fullPath = absolutePath + fullPath;
        }

        try
        {
            String fullPathURLEncoded = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.toString());
            // Slashes need to be not encoded. Otherwise normalization would work.
            fullPathURLEncoded = fullPathURLEncoded.replace("%2F", "/");
            URI uri = new URI(fullPathURLEncoded);
            String normalizedPathWithURI = uri.normalize().toString();
            String normalizedPath = URLDecoder.decode(normalizedPathWithURI, StandardCharsets.UTF_8.toString());
            // remove trailing slashes
            normalizedPath = normalizedPath.replaceAll("/*$", "");
            // replace multiple adjacent slashes with a single slash
            normalizedPath = normalizedPath.replaceAll("/+", "/");
            return StringUtils.isBlank(normalizedPath) ? FtpConstants.ROOT_DIRECTORY : normalizedPath;
        } catch (Exception ex)
        {
            operationLog.error(ex);
            throw new FtpException("Cannot parse path " + fullPath, ex);
        }
    }

    @Override
    public FtpFile getHomeDirectory() throws FtpException
    {
        return getFile(FtpConstants.ROOT_DIRECTORY);
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException
    {
        return workingDirectory;
    }

    @Override
    public boolean isRandomAccessible() throws FtpException
    {
        return true;
    }

}
