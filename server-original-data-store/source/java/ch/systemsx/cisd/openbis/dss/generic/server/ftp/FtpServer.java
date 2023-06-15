/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;

import org.apache.commons.io.IOUtils;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.log4j.Logger;
import org.apache.sshd.common.AttributeRepository.AttributeKey;
import org.apache.sshd.common.file.util.BaseFileSystem;
import org.apache.sshd.common.file.util.BasePath;
import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.SubsystemFactory;
import org.apache.sshd.sftp.common.SftpConstants;
import org.apache.sshd.sftp.server.SftpErrorStatusDataHandler;
import org.apache.sshd.sftp.server.SftpSubsystemEnvironment;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.AbstractFtpFileWithContent;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * Controls the lifecycle of an FTP server built into DSS.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpServer implements FileSystemFactory, org.apache.sshd.common.file.FileSystemFactory
{
    private static final AttributeKey<User> USER_KEY = new AttributeKey<User>();

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FtpServer.class);

    private final IServiceForDataStoreServer openBisService;

    private final FtpUserManager userManager;

    private final FtpServerConfig config;

    private final IFtpPathResolverRegistry pathResolverRegistry;

    private org.apache.ftpserver.FtpServer server;

    private final IGeneralInformationService generalInfoService;

    private final IApplicationServerApi v3api;

    private SshServer sshServer;

    private final Map<String, Set<DSSFileSystemView>> fileSystemViewsBySessionToken = new WeakHashMap<>();

    public FtpServer(IServiceForDataStoreServer openBisService, IGeneralInformationService generalInfoService, IApplicationServerApi v3api,
            FtpUserManager userManager) throws Exception
    {
        this.openBisService = openBisService;
        this.generalInfoService = generalInfoService;
        this.v3api = v3api;
        this.userManager = userManager;
        ExtendedProperties serviceProperties = DssPropertyParametersUtil.loadServiceProperties();
        Properties ftpProperties = PropertyParametersUtil.extractSingleSectionProperties(
                serviceProperties, "ftp.server", true).getProperties();
        this.config = new FtpServerConfig(serviceProperties);
        FtpPathResolverConfig resolverConfig = new FtpPathResolverConfig(ftpProperties);
        this.pathResolverRegistry = resolverConfig.getResolverRegistry();

        if (config.isStartServer())
        {
            config.logStartupInfo();
            resolverConfig.logStartupInfo("SFTP/FTP");
            start();
        }
    }

    private void start() throws Exception
    {
        if (config.isSftpMode())
        {
            sshServer = createSftpServer();
            operationLog.info(String.format("Starting SFTP server on port %d ...",
                    config.getSftpPort()));
            sshServer.start();
            operationLog.info("SFTP server started.");
        }
        if (config.isFtpMode())
        {
            server = createFtpServer();
            operationLog.info(String.format("Starting FTP server on port %d ...",
                    config.getFtpPort()));
            server.start();
            operationLog.info("FTP server started.");
        }
    }

    private org.apache.ftpserver.FtpServer createFtpServer()
    {
        FtpServerFactory serverFactory = new FtpServerFactory();

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(config.getFtpPort());
        if (config.isUseSSL())
        {
            SslConfigurationFactory sslConfigFactory = new SslConfigurationFactory();
            sslConfigFactory.setKeystoreFile(config.getKeyStore());
            sslConfigFactory.setKeystorePassword(config.getKeyStorePassword());
            sslConfigFactory.setKeyPassword(config.getKeyPassword());
            factory.setSslConfiguration(sslConfigFactory.createSslConfiguration());
            factory.setImplicitSsl(config.isImplicitSSL());
            serverFactory.setFtplets(Collections.<String, Ftplet> singletonMap("",
                    new DefaultFtplet()
                        {
                            @Override
                            public FtpletResult beforeCommand(FtpSession session, FtpRequest request)
                                    throws FtpException, IOException
                            {
                                String cmd = request.getCommand().toUpperCase();
                                if ("USER".equals(cmd))
                                {
                                    if (session.isSecure() == false)
                                    {
                                        session.write(new DefaultFtpReply(500,
                                                "Control channel is not secure. "
                                                        + "Please, issue AUTH command first."));
                                        return FtpletResult.SKIP;
                                    }
                                }
                                return super.beforeCommand(session, request);
                            }
                        }));
        }

        DataConnectionConfigurationFactory dccFactory = new DataConnectionConfigurationFactory();
        dccFactory.setPassivePorts(config.getPassivePortsRange());
        if (config.isActiveModeEnabled())
        {
            dccFactory.setActiveEnabled(true);
            dccFactory.setActiveLocalPort(config.getActiveLocalPort());
        }

        factory.setDataConnectionConfiguration(dccFactory.createDataConnectionConfiguration());
        serverFactory.addListener("default", factory.createListener());

        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setMaxThreads(config.getMaxThreads());
        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());

        serverFactory.setFileSystem(this);
        serverFactory.setUserManager(userManager);

        return serverFactory.createServer();
    }

    private SshServer createSftpServer()
    {
        SshServer s = SshServer.setUpDefaultServer();
        KeyPairProvider keyPairProvider = new KeystoreBasedKeyPairProvider(config, operationLog);
        s.setKeyPairProvider(keyPairProvider);
        s.setPort(config.getSftpPort());
        s.setSubsystemFactories(creatSubsystemFactories());
        s.setFileSystemFactory(this);
        s.setPasswordAuthenticator(new PasswordAuthenticator()
            {
                @Override
                public boolean authenticate(String username, String password, ServerSession session)
                        throws PasswordChangeRequiredException, AsyncAuthException
                {
                    try
                    {
                        UsernamePasswordAuthentication authentication =
                                new UsernamePasswordAuthentication(username, password);
                        User user = userManager.authenticate(authentication);
                        session.setAttribute(USER_KEY, user);
                        operationLog.info("User " + user + " authenticated. Session: " + session);
                        return true;
                    } catch (AuthenticationFailedException ex)
                    {
                        return false;
                    }
                }
            });
        s.addSessionListener(new SessionListener()
            {
                @Override
                public void sessionException(Session session, Throwable t)
                {
                    operationLog.error("Session exception", t);
                }
            });
        return s;
    }

    private List<? extends SubsystemFactory> creatSubsystemFactories()
    {
        SftpSubsystemFactory factory = new SftpSubsystemFactory.Builder().build();
        factory.setErrorStatusDataHandler(new SftpErrorStatusDataHandler()
            {
                private Set<Integer> subStatiForErrorLogging = new HashSet<>(Arrays.asList(
                        SftpConstants.SSH_FX_FAILURE, SftpConstants.SSH_FX_OP_UNSUPPORTED));
                private Set<Integer> subStatiForDebugLogging = new HashSet<>(Arrays.asList(
                        SftpConstants.SSH_FX_EOF));

                @Override
                public String resolveErrorMessage(SftpSubsystemEnvironment sftpSubsystem, int id,
                        Throwable e, int subStatus, int cmd, Object... args)
                {
                    String message = SftpErrorStatusDataHandler.super.resolveErrorMessage(sftpSubsystem, id, e, subStatus, cmd, args);
                    User user = sftpSubsystem.getSessionContext().getAttribute(USER_KEY);
                    String logMessage = "user: " + user + ", id=" + id + ", substatus=" + subStatus
                            + " (" + message + "), cmd=" + cmd + " (" + SftpConstants.getCommandMessageName(cmd)
                            + "), args=" + Arrays.asList(args);
                    if (subStatiForErrorLogging.contains(subStatus))
                    {
                        operationLog.error(logMessage, e);
                    } else if (subStatiForDebugLogging.contains(subStatus))
                    {
                        operationLog.debug(logMessage + ": " + e);
                    } else
                    {
                        operationLog.warn(logMessage + ": " + e);
                    }
                    return message;
                }
            });
        return Arrays.<SubsystemFactory> asList(factory);
    }

    /**
     * called by spring IoC container when the application shuts down.
     */
    public void stop()
    {
        if (server != null)
        {
            server.stop();
        }
        if (sshServer != null)
        {
            try
            {
                sshServer.stop();
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    @Override
    public DSSFileSystemView createFileSystemView(User user) throws FtpException
    {
        if (user instanceof FtpUser)
        {
            String sessionToken = ((FtpUser) user).getSessionToken();
            DSSFileSystemView fileSystemView = new DSSFileSystemView(sessionToken, openBisService, generalInfoService,
                    v3api, pathResolverRegistry);
            operationLog.info("Get file system views set for session " + sessionToken + " (" 
                    + fileSystemViewsBySessionToken.size() + " sessions are cached)");
            Set<DSSFileSystemView> views = fileSystemViewsBySessionToken.get(sessionToken);
            if (views == null)
            {
                operationLog.info("Create new file system views set for session " + sessionToken);
                views = new HashSet<>();
                fileSystemViewsBySessionToken.put(sessionToken, views);
            }
            views.add(fileSystemView);
            operationLog.info("There are " + views.size() + " file system views sets cached for session " + sessionToken);
            return fileSystemView;
        } else
        {
            throw new FtpException("Unsupported user type.");
        }
    }

    @Override
    public FileSystem createFileSystem(SessionContext session) throws IOException
    {
        User user = session.getAttribute(USER_KEY);
        try
        {
            DSSFileSystemView fileSystemView = createFileSystemView(user);
            OpenBisFileSystemProvider fileSystemProvider = new OpenBisFileSystemProvider(fileSystemView);
            return new OpenBisFileSystem(fileSystemProvider, fileSystemViewsBySessionToken, userManager, user);
        } catch (FtpException ex)
        {
            throw new IOException(ex.getMessage(), ex);
        }
    }

    private static class OpenBisFileSystem extends BaseFileSystem<OpenBisPath>
    {
        private final FtpUserManager userManager;

        private final User user;

        private final DSSFileSystemView fileSystemView;

        private final Map<String, Set<DSSFileSystemView>> fileSystemViewsBySessionToken;

        private boolean open = true;

        public OpenBisFileSystem(OpenBisFileSystemProvider fileSystemProvider,
                Map<String, Set<DSSFileSystemView>> fileSystemViewsBySessionToken,
                FtpUserManager userManager, User user)
        {
            super(fileSystemProvider);
            this.fileSystemViewsBySessionToken = fileSystemViewsBySessionToken;
            fileSystemView = fileSystemProvider.fileSystemView;
            this.userManager = userManager;
            this.user = user;
        }

        @Override
        public boolean isReadOnly()
        {
            return true;
        }

        @Override
        protected OpenBisPath create(String root, List<String> names)
        {
            return new OpenBisPath(this, root, names);
        }

        @Override
        public void close() throws IOException
        {
            Set<DSSFileSystemView> views = fileSystemViewsBySessionToken.get(fileSystemView.getSessionToken());
            boolean noViews = false;
            if (views != null)
            {
                views.remove(fileSystemView);
                if (views.isEmpty())
                {
                    fileSystemViewsBySessionToken.remove(fileSystemView.getSessionToken());
                    noViews = true;
                }
            }
            userManager.close(user, noViews);
            operationLog.info("File system closed for user " + user);
            open = false;
        }

        @Override
        public boolean isOpen()
        {
            return open;
        }

        @Override
        public Set<String> supportedFileAttributeViews()
        {
            return Collections.singleton("posix");
        }

        @Override
        public UserPrincipalLookupService getUserPrincipalLookupService()
        {
            throw new UnsupportedOperationException();
        }
    }

    private static class OpenBisPath extends BasePath<OpenBisPath, OpenBisFileSystem>
    {
        private OpenBisFileAttributes attributes;

        public OpenBisPath(OpenBisFileSystem fileSystem, String root, List<String> names)
        {
            super(fileSystem, root, names);
        }

        @Override
        public Path toRealPath(LinkOption... options) throws IOException
        {
            Path absolutePath = toAbsolutePath();
            FileSystemProvider provider = getFileSystem().provider();
            provider.checkAccess(absolutePath);
            return absolutePath;
        }

        public OpenBisFileAttributes getAttributes()
        {
            return attributes;
        }

        public void setAttributes(OpenBisFileAttributes attributes)
        {
            this.attributes = attributes;
        }
    }

    private static class OpenBisFileSystemProvider extends FileSystemProvider
    {
        private DSSFileSystemView fileSystemView;

        public OpenBisFileSystemProvider(DSSFileSystemView fileSystemView)
        {
            this.fileSystemView = fileSystemView;
        }

        @Override
        public Path readSymbolicLink(Path link) throws IOException
        {
            throw new NoSuchFileException("Symbolic links are not supported: " + link);
        }

        @Override
        public String getScheme()
        {
            return "openbis";
        }

        @Override
        public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException
        {
            return null;
        }

        @Override
        public FileSystem getFileSystem(URI uri)
        {
            return null;
        }

        @Override
        public Path getPath(URI uri)
        {
            return null;
        }

        @Override
        public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
        {
            return null;
        }

        @Override
        public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException
        {
            FtpFile folder = getFile(dir);
            List<Path> children = new ArrayList<>();
            for (FtpFile file : folder.listFiles())
            {
                children.add(dir.getFileSystem().getPath(file.getAbsolutePath()));
            }
            return new DirectoryStream<Path>()
                {
                    @Override
                    public void close() throws IOException
                    {
                    }

                    @Override
                    public Iterator<Path> iterator()
                    {
                        return children.iterator();
                    }
                };
        }

        @Override
        public InputStream newInputStream(Path path, OpenOption... options) throws IOException
        {
            throw new UnsupportedOperationException("Input streams not supported for " + path);
        }

        @Override
        public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
        {
            FtpFile file = getFile(path);
            NonExistingFtpFile.throwFileNotFoundExceptionIfNonExistingFtpFile(file);
            if (file instanceof AbstractFtpFileWithContent == false)
            {
                throw new UnsupportedOperationException("File channel not supported.");
            }
            return ((AbstractFtpFileWithContent) file).getFileChannel();
        }

        @Override
        public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException
        {
            throw new ReadOnlyFileSystemException();
        }

        @Override
        public void delete(Path path) throws IOException
        {
            throw new ReadOnlyFileSystemException();
        }

        @Override
        public void copy(Path source, Path target, CopyOption... options) throws IOException
        {
            throw new ReadOnlyFileSystemException();
        }

        @Override
        public void move(Path source, Path target, CopyOption... options) throws IOException
        {
            throw new ReadOnlyFileSystemException();
        }

        @Override
        public boolean isSameFile(Path path, Path path2) throws IOException
        {
            return path.toAbsolutePath().equals(path2.toAbsolutePath());
        }

        @Override
        public boolean isHidden(Path path) throws IOException
        {
            return false;
        }

        @Override
        public FileStore getFileStore(Path path) throws IOException
        {
            return null;
        }

        @Override
        public void checkAccess(Path path, AccessMode... modes) throws IOException
        {
        }

        @Override
        public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options)
        {
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException
        {
            if (path instanceof OpenBisPath == false)
            {
                throw new ProviderMismatchException();
            }
            OpenBisPath openBisPath = (OpenBisPath) path;
            OpenBisFileAttributes fileAttributes = openBisPath.getAttributes();
            if (fileAttributes == null)
            {
                fileAttributes = new OpenBisFileAttributes();
                FtpFile file = getFile(path);
                NonExistingFtpFile.throwFileNotFoundExceptionIfNonExistingFtpFile(file);
                FileTime lastModified = FileTime.fromMillis(file.getLastModified());
                fileAttributes.setModifiedTime(lastModified);
                fileAttributes.setCreationTime(lastModified);
                fileAttributes.setAccessTime(lastModified);
                fileAttributes.setSize(file.getSize());
                fileAttributes.setDirectory(file.isDirectory());
                fileAttributes.setRegularFile(file.isFile());
                openBisPath.setAttributes(fileAttributes);
            }
            return (A) fileAttributes;
        }

        private FtpFile getFile(Path path)
        {
            try
            {
                return fileSystemView.getFile(path.toString());
            } catch (FtpException e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            }
        }

        @Override
        public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException
        {
            return null;
        }

        @Override
        public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException
        {
            throw new ReadOnlyFileSystemException();
        }
    }

    private static class OpenBisFileAttributes implements PosixFileAttributes
    {
        private FileTime modifiedTime;

        private FileTime accessTime;

        private FileTime creationTime;

        private boolean regularFile;

        private boolean directory;

        private boolean symbolicLink;

        private long size;

        @Override
        public FileTime lastModifiedTime()
        {
            return modifiedTime;
        }

        public void setModifiedTime(FileTime modifiedTime)
        {
            this.modifiedTime = modifiedTime;
        }

        @Override
        public FileTime lastAccessTime()
        {
            return accessTime;
        }

        public void setAccessTime(FileTime accessTime)
        {
            this.accessTime = accessTime;
        }

        @Override
        public FileTime creationTime()
        {
            return creationTime;
        }

        public void setCreationTime(FileTime creationTime)
        {
            this.creationTime = creationTime;
        }

        @Override
        public boolean isRegularFile()
        {
            return regularFile;
        }

        public void setRegularFile(boolean regularFile)
        {
            this.regularFile = regularFile;
        }

        @Override
        public boolean isDirectory()
        {
            return directory;
        }

        public void setDirectory(boolean directory)
        {
            this.directory = directory;
        }

        @Override
        public boolean isSymbolicLink()
        {
            return symbolicLink;
        }

        @Override
        public boolean isOther()
        {
            return (regularFile || directory || symbolicLink) == false;
        }

        @Override
        public long size()
        {
            return size;
        }

        public void setSize(long size)
        {
            this.size = size;
        }

        @Override
        public Object fileKey()
        {
            return null;
        }

        @Override
        public UserPrincipal owner()
        {
            return null;
        }

        @Override
        public GroupPrincipal group()
        {
            return null;
        }

        @Override
        public Set<PosixFilePermission> permissions()
        {
            return EnumSet.of(PosixFilePermission.OWNER_READ);
        }

        @Override
        public String toString()
        {
            return modifiedTime + " " + (directory ? "DIR" : (regularFile ? "FILE" : "?")) + " " + size;
        }
    }

    private static final class KeystoreBasedKeyPairProvider extends AbstractKeyPairProvider
    {
        private final KeyPair[] keyPairs;

        private KeystoreBasedKeyPairProvider(FtpServerConfig config, Logger operationLog)
        {
            File keyStoreFile = config.getKeyStore();
            String keyStorePassword = config.getKeyStorePassword();
            String keyPassword = config.getKeyPassword();
            KeyStore keystore = loadKeystore(keyStoreFile, keyStorePassword);
            X509ExtendedKeyManager keyManager =
                    getKeyManager(keystore, keyStorePassword, keyPassword);
            List<KeyPair> list = new ArrayList<KeyPair>();
            try
            {
                Enumeration<String> aliases = keystore.aliases();
                while (aliases.hasMoreElements())
                {
                    String alias = aliases.nextElement();
                    if (keystore.isKeyEntry(alias))
                    {
                        Certificate certificate = keystore.getCertificate(alias);
                        PublicKey publicKey = certificate.getPublicKey();
                        PrivateKey privateKey = keyManager.getPrivateKey(alias);
                        list.add(new KeyPair(publicKey, privateKey));
                    }
                }
                keyPairs = list.toArray(new KeyPair[list.size()]);
                operationLog.info(keyPairs.length + " key pairs loaded from keystore "
                        + keyStoreFile);
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        @Override
        public Iterable<KeyPair> loadKeys(SessionContext session) throws IOException, GeneralSecurityException
        {
            return Arrays.asList(keyPairs);
        }

        private KeyStore loadKeystore(File keyStoreFile, String keyStorePassword)
        {
            InputStream stream = null;
            try
            {
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                stream = new FileInputStream(keyStoreFile);
                keystore.load(stream, keyStorePassword.toCharArray());
                return keystore;
            } catch (Exception e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            } finally
            {
                IOUtils.closeQuietly(stream);
            }
        }

        private X509ExtendedKeyManager getKeyManager(KeyStore keystore, String keyStorePassword,
                String keyPassword)
        {
            try
            {
                String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
                KeyManagerFactory factory = KeyManagerFactory.getInstance(defaultAlgorithm);
                char[] password =
                        (keyPassword == null ? keyStorePassword : keyPassword).toCharArray();
                factory.init(keystore, password);
                KeyManager[] keyManagers = factory.getKeyManagers();
                if (keyManagers.length != 1)
                {
                    throw new ConfigurationFailureException(
                            "Only one key manager expected instead of " + keyManagers.length + ".");
                }
                KeyManager keyManager = keyManagers[0];
                if (keyManager instanceof X509ExtendedKeyManager == false)
                {
                    throw new ConfigurationFailureException("Key manager is not of type "
                            + X509ExtendedKeyManager.class.getSimpleName() + ": "
                            + keyManager.getClass().getName());
                }
                return (X509ExtendedKeyManager) keyManager;
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    @Override
    public Path getUserHomeDir(SessionContext session) throws IOException
    {
        return null;
    }
}
