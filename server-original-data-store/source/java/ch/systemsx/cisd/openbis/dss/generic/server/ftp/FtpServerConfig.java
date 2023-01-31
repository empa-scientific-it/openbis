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

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.ConfigParameters;

/**
 * @author Kaloyan Enimanev
 */
public class FtpServerConfig
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FtpServerConfig.class);

    private final static String PREFIX = "ftp.server.";

    final static String ENABLE_KEY = PREFIX + "enable";

    final static String SFTP_PORT_KEY = PREFIX + "sftp-port";

    final static String LEGACY_FTP_PORT_KEY = PREFIX + "port";

    final static String FTP_PORT_KEY = PREFIX + "ftp-port";

    final static String USE_SSL_KEY = PREFIX + "use-ssl";

    final static String IMPLICIT_SSL_KEY = PREFIX + "implicit-ssl";

    final static String MAX_THREADS_KEY = PREFIX + "maxThreads";

    final static String ACTIVE_MODE_ENABLE_KEY = PREFIX + "activemode.enable";

    final static String ACTIVE_PORT_KEY = PREFIX + "activemode.port";

    final static String PASSIVE_MODE_PORT_RANGE_KEY = PREFIX + "passivemode.port.range";

    private static final int DEFAULT_ACTIVE_PORT = 2122;

    private static final boolean DEFAULT_USE_SSL = true;

    private static final boolean DEFAULT_IMPLICIT_SSL = false;

    private static final int DEFAULT_MAX_THREADS = 25;

    private static final String DEFAULT_PASSIVE_PORTS = "2130-2140";

    private boolean startServer;

    private int ftpPort;

    private boolean ftpMode;

    private boolean activeModeEnabled;

    private int activePort;

    private String passivePortsRange;

    private boolean useSSL;

    private boolean implicitSSL;

    private File keyStore;

    private String keyPassword;

    private String keyStorePassword;

    private int maxThreads;

    private boolean sftpMode;

    private int sftpPort;

    public FtpServerConfig(Properties props)
    {
        this.startServer = PropertyUtils.getBoolean(props, ENABLE_KEY, false);
        if (startServer)
        {
            initializeProperties(props);
        }
    }

    private void initializeProperties(Properties props)
    {
        sftpPort = PropertyUtils.getInt(props, SFTP_PORT_KEY, 0);
        sftpMode = sftpPort > 0;
        ftpPort = PropertyUtils.getPosInt(props, FTP_PORT_KEY, 0);
        if (ftpPort == 0)
        {
            ftpPort = PropertyUtils.getPosInt(props, LEGACY_FTP_PORT_KEY, 0);
        }
        ftpMode = ftpPort > 0;
        useSSL = PropertyUtils.getBoolean(props, USE_SSL_KEY, DEFAULT_USE_SSL);
        if (sftpMode || useSSL)
        {
            initializeSSLProperties(props);
        }
        activeModeEnabled = PropertyUtils.getBoolean(props, ACTIVE_MODE_ENABLE_KEY, false);
        activePort = PropertyUtils.getPosInt(props, ACTIVE_PORT_KEY, DEFAULT_ACTIVE_PORT);
        passivePortsRange =
                PropertyUtils
                        .getProperty(props, PASSIVE_MODE_PORT_RANGE_KEY, DEFAULT_PASSIVE_PORTS);
        maxThreads = PropertyUtils.getPosInt(props, MAX_THREADS_KEY, DEFAULT_MAX_THREADS);
    }

    private void initializeSSLProperties(Properties props)
    {
        String keyStoreFileName =
                PropertyUtils.getMandatoryProperty(props, ConfigParameters.KEYSTORE_PATH_KEY);
        keyStore = new File(keyStoreFileName);
        keyStorePassword =
                PropertyUtils.getMandatoryProperty(props, ConfigParameters.KEYSTORE_PASSWORD_KEY);
        keyPassword =
                PropertyUtils.getMandatoryProperty(props,
                        ConfigParameters.KEYSTORE_KEY_PASSWORD_KEY);
        implicitSSL = PropertyUtils.getBoolean(props, IMPLICIT_SSL_KEY, DEFAULT_IMPLICIT_SSL);
    }

    public boolean isSftpMode()
    {
        return sftpMode;
    }

    public int getSftpPort()
    {
        return sftpPort;
    }

    public boolean isStartServer()
    {
        return startServer;
    }

    public boolean isFtpMode()
    {
        return ftpMode;
    }

    public int getFtpPort()
    {
        return ftpPort;
    }

    public boolean isUseSSL()
    {
        return useSSL;
    }

    public boolean isImplicitSSL()
    {
        return implicitSSL;
    }

    public File getKeyStore()
    {
        return keyStore;
    }

    public String getKeyPassword()
    {
        return keyPassword;
    }

    public String getKeyStorePassword()
    {
        return keyStorePassword;
    }

    public Integer getMaxThreads()
    {
        return maxThreads;
    }

    /**
     * information being logged on FTP server startup.
     */
    public void logStartupInfo()
    {
        if (ftpMode)
        {
            operationLog.info("FTP Server port: " + ftpPort);
            operationLog.info("FTP Server using SSL: " + useSSL);
            operationLog.info("FTP Server passive ports: " + passivePortsRange);
            operationLog.info("FTP Server enable active mode: " + activeModeEnabled);
            if (activeModeEnabled)
            {
                operationLog.info("FTP Server active mode port: " + activePort);
            }
        }
        if (sftpMode)
        {
            operationLog.info("SFTP Server port: " + sftpPort);
        }
    }

    public boolean isActiveModeEnabled()
    {
        return activeModeEnabled;
    }

    public int getActiveLocalPort()
    {
        return activePort;
    }

    public String getPassivePortsRange()
    {
        return passivePortsRange;
    }

}
