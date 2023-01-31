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

package ch.systemsx.cisd.etlserver.api.v1;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.etlserver.ETLDaemon;
import ch.systemsx.cisd.etlserver.IETLServerPlugin;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.Parameters;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * Utility class the maps between data set types (strings) and IETLServerPlugin instances. Made public to aid tests, but is really package internal.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class DataSetTypeToRegistratorMapper
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DataSetTypeToRegistratorMapper.class);

    // The default plugin is either the one explicitly specified in the properties file, or,
    // otherwise, the first-defined thread
    private final ITopLevelDataSetRegistrator defaultHandler;

    private final HashMap<String, ITopLevelDataSetRegistrator> handlerMap;

    private final HashMap<String, ITopLevelDataSetRegistrator> dropboxToHandlerMap;

    private static final String DSS_RPC_SECTION_KEY = "dss-rpc";

    private static final String DEFAULT_THREAD_KEY = "put-default";

    private static final String PUT_SECTION_KEY = "put";

    /**
     * Constructor for testing purposes. Should not be used otherwise.
     * 
     * @param plugin
     */
    protected DataSetTypeToRegistratorMapper(ITopLevelDataSetRegistrator plugin)
    {
        defaultHandler = plugin;
        handlerMap = new HashMap<String, ITopLevelDataSetRegistrator>();
        dropboxToHandlerMap = new HashMap<String, ITopLevelDataSetRegistrator>();
    }

    DataSetTypeToRegistratorMapper(Parameters params, IEncapsulatedOpenBISService openBISService,
            IMailClient mailClient, IDataSetValidator dataSetValidator)
    {
        DataSetTypeToTopLevelHandlerMapperInitializer initializer =
                new DataSetTypeToTopLevelHandlerMapperInitializer(params, openBISService,
                        mailClient, dataSetValidator);
        initializer.initialize();
        defaultHandler = initializer.getDefaultHandler();
        dropboxToHandlerMap = initializer.getDropboxToHandlerMap();
        handlerMap = initializer.getHandlerMap(dropboxToHandlerMap);
    }

    public ITopLevelDataSetRegistrator getRegistratorForDropbox(String dropboxName)
    {
        if (null == dropboxName)
        {
            return defaultHandler;
        }

        ITopLevelDataSetRegistrator plugin = dropboxToHandlerMap.get(dropboxName);

        if (plugin == null)
        {
            operationLog
                    .warn("Trying to drop dataset via rpc into dropbox "
                            + dropboxName
                            + ", but this dropbox was not configured for it so default dropbox will be used.\nIf that was not intended, then you need to add this line in your servers/datastore_server/etc/service.properties:\ndss-rpc.put."
                            + dropboxName
                            + " = " + dropboxName);
            return defaultHandler;
        } else
        {
            return plugin;
        }
    }

    public ITopLevelDataSetRegistrator getRegistratorForType(String dataSetTypeOrNull)
    {
        if (null == dataSetTypeOrNull)
        {
            return defaultHandler;
        }
        ITopLevelDataSetRegistrator plugin = handlerMap.get(dataSetTypeOrNull);
        return (null == plugin) ? defaultHandler : plugin;
    }

    public Collection<ITopLevelDataSetRegistrator> getRegistrators()
    {
        Collection<ITopLevelDataSetRegistrator> registrators = new ArrayList<ITopLevelDataSetRegistrator>();
        registrators.add(defaultHandler);
        registrators.addAll(handlerMap.values());
        return registrators;
    }

    public void initializeStoreRootDirectory(File storeDirectory)
    {
        initializeStoreRootDirectory(storeDirectory, defaultHandler);
        for (ITopLevelDataSetRegistrator handler : handlerMap.values())
        {
            initializeStoreRootDirectory(storeDirectory, handler);
        }
    }

    private void initializeStoreRootDirectory(File storeDirectory,
            ITopLevelDataSetRegistrator registrator)
    {
        if (registrator instanceof PutDataSetServerPluginHolder)
        {
            IETLServerPlugin plugin = ((PutDataSetServerPluginHolder) registrator).getPlugin();
            plugin.getStorageProcessor().setStoreRootDirectory(storeDirectory);
        }
    }

    private class DataSetTypeToTopLevelHandlerMapperInitializer
    {
        private final Parameters params;

        private final IEncapsulatedOpenBISService openBISService;

        private final IMailClient mailClient;

        private final IDataSetValidator dataSetValidator;

        private final HashMap<String, ThreadParameters> threadParamMap;

        private ExtendedProperties section;

        /**
         * @param params
         * @param openBISService
         * @param mailClient
         * @param dataSetValidator
         */
        public DataSetTypeToTopLevelHandlerMapperInitializer(Parameters params,
                IEncapsulatedOpenBISService openBISService, IMailClient mailClient,
                IDataSetValidator dataSetValidator)
        {
            super();
            this.params = params;
            this.openBISService = openBISService;
            this.mailClient = mailClient;
            this.dataSetValidator = dataSetValidator;
            threadParamMap = new HashMap<String, ThreadParameters>();
        }

        public void initialize()
        {
            initializeThreadMap();
            initializeSectionProperties();
        }

        public ITopLevelDataSetRegistrator getDefaultHandler()
        {
            ThreadParameters[] threadParams = params.getThreads();
            ThreadParameters firstThread = threadParams[0];

            String defaultThreadName = section.getProperty(DEFAULT_THREAD_KEY);
            ThreadParameters defaultThread =
                    (null != defaultThreadName) ? threadParamMap.get(defaultThreadName) : null;
            if (null == defaultThread)
            {
                return ETLDaemon.createTopLevelDataSetRegistrator(params.getProperties(),
                        firstThread, openBISService, mailClient, dataSetValidator, null, false,
                        false, false, firstThread.tryGetPreRegistrationScript(),
                        firstThread.tryGetPostRegistrationScript(),
                        firstThread.tryGetValidationScripts(), PutDataSetServerPluginHolder.class);
            }

            return ETLDaemon.createTopLevelDataSetRegistrator(params.getProperties(),
                    defaultThread, openBISService, mailClient, dataSetValidator, null, false,
                    false, false, defaultThread.tryGetPreRegistrationScript(),
                    defaultThread.tryGetPostRegistrationScript(),
                    defaultThread.tryGetValidationScripts(), PutDataSetServerPluginHolder.class);
        }

        public HashMap<String, ITopLevelDataSetRegistrator> getDropboxToHandlerMap()
        {
            HashMap<String, ITopLevelDataSetRegistrator> map =
                    new HashMap<String, ITopLevelDataSetRegistrator>();

            Properties putSection = section.getSubset(PUT_SECTION_KEY + ".", true);

            for (Object keyObject : putSection.keySet())
            {
                String key = (String) keyObject;
                String threadName = putSection.getProperty(key);
                ThreadParameters threadParams = threadParamMap.get(threadName);
                if (null != threadParams)
                {
                    ITopLevelDataSetRegistrator registrator =
                            ETLDaemon.createTopLevelDataSetRegistrator(params.getProperties(),
                                    threadParams, openBISService, mailClient, dataSetValidator,
                                    null, false);
                    map.put(threadName, registrator);
                }
            }
            return map;
        }

        public HashMap<String, ITopLevelDataSetRegistrator> getHandlerMap(
                @SuppressWarnings("hiding") HashMap<String, ITopLevelDataSetRegistrator> dropboxToHandlerMap)
        {
            HashMap<String, ITopLevelDataSetRegistrator> map =
                    new HashMap<String, ITopLevelDataSetRegistrator>();

            Properties putSection = section.getSubset(PUT_SECTION_KEY + ".", true);

            for (Object keyObject : putSection.keySet())
            {
                String key = (String) keyObject;
                String threadName = putSection.getProperty(key);
                ThreadParameters threadParams = threadParamMap.get(threadName);
                if (null != threadParams)
                {
                    ITopLevelDataSetRegistrator registrator = dropboxToHandlerMap.get(threadName);
                    map.put(key.toUpperCase(), registrator);
                }
            }
            return map;
        }

        private void initializeThreadMap()
        {
            ThreadParameters[] threadParams = params.getThreads();
            for (ThreadParameters threadParam : threadParams)
            {
                threadParamMap.put(threadParam.getThreadName(), threadParam);
            }
        }

        private void initializeSectionProperties()
        {
            section =
                    ExtendedProperties.createWith(params.getProperties()).getSubset(
                            DSS_RPC_SECTION_KEY + ".", true);
        }

    }

}
