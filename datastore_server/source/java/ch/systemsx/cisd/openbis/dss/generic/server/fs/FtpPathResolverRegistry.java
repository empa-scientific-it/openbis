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

package ch.systemsx.cisd.openbis.dss.generic.server.fs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.HierarchicalContentUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.DirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.DirectoryResponse.DirectoryNode;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.DirectoryResponse.FileNode;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.DirectoryResponse.Node;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.FileResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.file.NonExistingFileResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.plugins.FileSystemPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.resolver.RootLevelResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Cache;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverConfig;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolverRegistry;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.NonExistingFtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFileWithContent;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;

/**
 * A entry point for the file system resolvers. It delegates resolution to plugin or default resolver.
 * 
 * @author Jakub Straszewski
 */
public class FtpPathResolverRegistry implements IFtpPathResolverRegistry
{

    private static final class NodeBasedFtpFile extends AbstractFtpFileWithContent
    {
        private final FtpPathResolverRegistry ftpPathResolverRegistry;
        private final FtpPathResolverContext resolverContext;

        NodeBasedFtpFile(FileNode fileNode, FtpPathResolverRegistry ftpPathResolverRegistry, 
                FtpPathResolverContext resolverContext)
        {
            super(fileNode.getFullPath());
            setSize(fileNode.getSize());
            this.ftpPathResolverRegistry = ftpPathResolverRegistry;
            this.resolverContext = resolverContext.cloneForPath(getAbsolutePath());
        }

        @Override
        public boolean isFile()
        {
            return true;
        }

        @Override
        public boolean isDirectory()
        {
            return false;
        }

        @Override
        public InputStream createInputStream(long offset) throws IOException
        {
            FtpFile ftpFile = ftpPathResolverRegistry.resolveAndConvert(absolutePath, resolverContext);
            return ftpFile.createInputStream(offset);
        }

        @Override
        public List<FtpFile> unsafeListFiles() throws RuntimeException
        {
            throw new UnsupportedOperationException("Listing files not supported: " + absolutePath);
        }

        @Override
        public IRandomAccessFile getFileContent()
        {
            FtpFile ftpFile = ftpPathResolverRegistry.resolveAndConvert(absolutePath, resolverContext);
            if (ftpFile instanceof AbstractFtpFileWithContent)
            {
                AbstractFtpFileWithContent fileWithContent = (AbstractFtpFileWithContent) ftpFile;
                return fileWithContent.getFileContent();
            }
            throw new UnsupportedOperationException("Content not supported: " + absolutePath);
        }
    }

    private static final class NodeBasedFtpFolder extends AbstractFtpFolder
    {
        private FtpPathResolverRegistry ftpPathResolverRegistry;
        private FtpPathResolverContext resolverContext;

        NodeBasedFtpFolder(String parentAbsolutePath, Node node, 
                FtpPathResolverRegistry ftpPathResolverRegistry, FtpPathResolverContext resolverContext)
        {
            super(concatenatePathElements(parentAbsolutePath, node.getFullPath()));
            this.ftpPathResolverRegistry = ftpPathResolverRegistry;
            this.resolverContext = resolverContext.cloneForPath(getAbsolutePath());
        }

        @Override
        public List<FtpFile> unsafeListFiles() throws RuntimeException
        {
            FtpFile ftpFile = ftpPathResolverRegistry.resolveAndConvert(absolutePath, resolverContext);
            return ftpFile.listFiles();
        }
    }

    private static String concatenatePathElements(String parent, String child)
    {
        String normalizedParent = parent.endsWith("/") ? parent : parent + "/";
        String normalizedChild = child.startsWith("/") ? child.substring(1) : child;
        return normalizedParent + normalizedChild;
    }
    
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FtpPathResolverRegistry.class);

    private List<FileSystemPlugin> plugins = new LinkedList<>();

    @Override
    public void initialize(FtpPathResolverConfig config)
    {
        Properties props = config.getProperties();
        String listOfEnabledFileSystemPlugins = props.getProperty(Constants.DSS_FS_PLUGIN_NAMES);

        if (listOfEnabledFileSystemPlugins != null)
        {
            String[] split = listOfEnabledFileSystemPlugins.split(",");
            for (String pluginName : split)
            {
                pluginName = pluginName.trim();
                String className = props.getProperty(pluginName + ".resolver-class");
                String code = props.getProperty(pluginName + ".code");
                try
                {
                    Class<?> clazz = Class.forName(className);
                    plugins.add(new FileSystemPlugin(pluginName, code, clazz));
                } catch (ClassNotFoundException ex)
                {
                    throw new ConfigurationFailureException("Couldn't load class for file system plugin: " + className);
                } catch (SecurityException ex)
                {
                    throw new ConfigurationFailureException("Couldn't load class for file system plugin");
                } catch (Exception ex)
                {
                    throw new ConfigurationFailureException("Couldn't load class for file system plugin", ex);
                }

            }
        }
    }

    private FtpFile convert(IFileSystemViewResponse response, FtpPathResolverContext resolverContext)
    {
        if (response instanceof DirectoryResponse)
        {
            return convert((DirectoryResponse) response, resolverContext);
        }
        if (response instanceof FileResponse)
        {
            return convert((FileResponse) response);
        }
        if (response instanceof NonExistingFileResponse)
        {
            return convert((NonExistingFileResponse) response);
        }
        throw new IllegalArgumentException("Unknown object of type " + IFileSystemViewResponse.class.getName());
    }

    private FtpFile convert(final DirectoryResponse dir, final FtpPathResolverContext resolverContext)
    {
        AbstractFtpFolder abstractFtpFolder = new AbstractFtpFolder(dir.getFullPath())
            {
                @Override
                public List<FtpFile> unsafeListFiles() throws RuntimeException
                {
                    List<FtpFile> result = new LinkedList<FtpFile>();
                    for (Node entry : dir.getFiles())
                    {
                        FtpFile file = null;
                        if (entry instanceof DirectoryNode)
                        {
                            file = new NodeBasedFtpFolder(this.absolutePath, entry, 
                                    FtpPathResolverRegistry.this, resolverContext);
                        } else if (entry instanceof FileNode)
                        {
                            file = new NodeBasedFtpFile((FileNode) entry, 
                                    FtpPathResolverRegistry.this, resolverContext);
                        }
                        if (file != null)
                        {
                            file.setLastModified(entry.getLastModified());
                            String key = createKey(file.getAbsolutePath(), resolverContext);
                            resolverContext.getCache().putResponse(key, file);
                            result.add(file);
                        }
                    }
                    return result;
                }
            };
        String key = createKey(abstractFtpFolder.getAbsolutePath(), resolverContext);
        FtpFile response = resolverContext.getCache().getResponse(key);
        if (response != null)
        {
            abstractFtpFolder.setLastModified(response.getLastModified());
        }
        return abstractFtpFolder;
    }

    private FtpFile convert(final FileResponse file)
    {
        AbstractFtpFileWithContent fileWithContent = new AbstractFtpFileWithContent(file.getFullPath())
            {
                @Override
                public InputStream createInputStream(long offset) throws IOException
                {
                    IHierarchicalContent content = file.getContent();
                    try
                    {
                        IHierarchicalContentNode node = file.getNode();
                        InputStream result =
                                HierarchicalContentUtils.getInputStreamAutoClosingContent(node, content);
                        if (offset > 0)
                        {
                            result.skip(offset);
                        }
                        return result;
                    } catch (IOException ioex)
                    {
                        content.close();
                        operationLog.error("Error while reading content at " + offset + " from " 
                                + absolutePath, ioex);
                        throw ioex;
                    } catch (RuntimeException re)
                    {
                        content.close();
                        operationLog.error("Error while reading content at " + offset + " from " 
                                + absolutePath, re);
                        throw re;
                    }
                }

                @Override
                public boolean isFile()
                {
                    return true;
                }

                @Override
                public boolean isDirectory()
                {
                    return false;
                }

                @Override
                public List<FtpFile> unsafeListFiles() throws RuntimeException
                {
                    throw new IllegalStateException("Don't expect to ask for file listing of file");
                }

                @Override
                public IRandomAccessFile getFileContent()
                {
                    return file.getNode().getFileContent();
                }
            };
        fileWithContent.setLastModified(file.getNode().getLastModified());
        fileWithContent.setSize(file.getNode().getFileLength());
        return fileWithContent;
    }

    private FtpFile convert(NonExistingFileResponse nonExistingFile)
    {
        return new NonExistingFtpFile(nonExistingFile.getFullPath(), nonExistingFile.getErrorMsg());
    }

    @Override
    public FtpFile resolve(String path, FtpPathResolverContext resolverContext)
    {
        String responseCacheKey = createKey(path, resolverContext);
        Cache cache = resolverContext.getCache();
        FtpFile response = cache.getResponse(responseCacheKey);
        if (response != null)
        {
            operationLog.info("Path " + path + " requested (found in cache).");
            return response;
        }

        response = resolveAndConvert(path, resolverContext);
        cache.putResponse(responseCacheKey, response);
        operationLog.info("Path " + path + " requested.");
        return response;

    }

    private FtpFile resolveAndConvert(String path, FtpPathResolverContext resolverContext)
    {
        IFileSystemViewResponse response;
        String[] split = path.equals("/") ? new String[] {} : path.substring(1).split("/");
        try
        {
            if (plugins.size() > 0)
            {
                response = resolvePlugins(path, split, resolverContext);
            } else
            {
                response = resolveDefault(path, resolverContext, split);
            }
        } catch (Exception e)
        {
            operationLog.warn("Resolving " + path + " failed", e);
            response = resolverContext.getResolverContext().createNonExistingFileResponse("Error when retrieving path");
        }
        return convert(response, resolverContext);
    }

    private String createKey(String path, FtpPathResolverContext resolverContext)
    {
        return resolverContext.getSessionToken() + "$" + path;
    }

    private IFileSystemViewResponse resolveDefault(String path, FtpPathResolverContext resolverContext, String[] split)
    {
        RootLevelResolver resolver = new RootLevelResolver();
        return resolver.resolve(split, resolverContext.getResolverContext());
    }

    private IFileSystemViewResponse resolvePlugins(String path, String[] subPath, FtpPathResolverContext resolverContext)
    {
        if (subPath.length == 0)
        {
            DirectoryResponse response = resolverContext.getResolverContext().createDirectoryResponse();
            response.addDirectory("DEFAULT");
            for (FileSystemPlugin plugin : plugins)
            {
                response.addDirectory(plugin.getPluginCode());
            }
            return response;
        } else
        {
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            if (subPath[0].equals("DEFAULT"))
            {
                return resolveDefault(path, resolverContext, remaining);
            } else
            {
                for (FileSystemPlugin plugin : plugins)
                {
                    if (plugin.getPluginCode().equals(subPath[0]))
                    {
                        IResolver resolver = plugin.getPluginResolver();
                        return resolver.resolve(remaining, resolverContext.getResolverContext());
                    }
                }
                return resolverContext.getResolverContext().createNonExistingFileResponse(null);
            }
        }
    }

}
