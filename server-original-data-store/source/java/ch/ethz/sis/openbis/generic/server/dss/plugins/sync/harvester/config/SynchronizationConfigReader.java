/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Ganime Betul Akin
 */
public class SynchronizationConfigReader
{

    private static final String DEFAULT_HARVESTER_TEMP_DIR = "temp";

    private static final String DATA_SOURCE_URL_PROPERTY_NAME = "resource-list-url";

    private static final String DATA_SOURCE_OPENBIS_URL_PROPERTY_NAME = "data-source-openbis-url";

    private static final String DATA_SOURCE_DSS_URL_PROPERTY_NAME = "data-source-dss-url";

    private static final String DATA_SOURCE_SPACES_PROPERTY_NAME = "data-source-spaces";

    private static final String DATA_SOURCE_ALIAS_PROPERTY_NAME = "data-source-alias";

    private static final String DATA_SOURCE_AUTH_REALM_PROPERTY_NAME = "data-source-auth-realm";

    private static final String DATA_SOURCE_AUTH_USER_PROPERTY_NAME = "data-source-auth-user";

    private static final String DATA_SOURCE_AUTH_PASS_PROPERTY_NAME = "data-source-auth-pass";

    private static final String HARVESTER_USER_PROPERTY_NAME = "harvester-user";

    private static final String HARVESTER_PASS_PROPERTY_NAME = "harvester-pass";

    private static final String HARVESTER_SPACES_PROPERTY_NAME = "harvester-spaces";

    private static final String HARVESTER_TEMP_DIR_PROPERTY_NAME = "harvester-tmp-dir";

    private static final String FILE_SERVICE_REPOSITORY_PATH_PROPERTY_NAME = "file-service-repository-path";

    private static final String HARVESTER_LAST_SYNC_TIMESTAMP_FILE_PROPERTY_NAME = "last-sync-timestamp-file";

    private static final String HARVESTER_NOT_SYNCED_ENTITIES_FILE_NAME = "not-synced-entities-file";

    private static final String EMAIL_ADDRESSES_PROPERTY_NAME = "email-addresses";

    private static final String TRANSLATE_USING_DATA_SOURCE_ALIAS_PROPERTY_NAME = "translate-using-data-source-alias";

    private static final String FULL_SYNC_PROPERTY_NAME = "full-sync";

    private static final String FULL_SYNC_INTERVAL_PROPERTY_NAME = "full-sync-interval";

    private static final String DRY_RUN_PROPERTY_NAME = "dry-run";

    private static final String VERBOSE_PROPERTY_NAME = "verbose";

    private static final String SPACE_BLACK_LIST_PROPERTY_NAME = "space-black-list";

    private static final String SPACE_WHITE_LIST_PROPERTY_NAME = "space-white-list";

    private static final String MASTER_DATA_UPDATE_ALLOWED_PROPERTY_NAME = "master-data-update-allowed";

    private static final String PROPERTY_UNASSIGNMENT_ALLOWED_PROPERTY_NAME = "property-unassignment-allowed";

    private static final String DELETION_ALLOWED_PROPERTY_NAME = "deletion-allowed";

    private static final String KEEP_ORIGINAL_TIMESTAMPS_AND_USERS_PROPERTY_NAME = "keep-original-timestamps-and-users";

    private static final String KEEP_ORIGINAL_FROZEN_FLAGS_PROPERTY_NAME = "keep-original-frozen-flags";

    private static final String WISHED_NUMBER_OF_STREAMS_PROPERTY_NAME = "whished-number-of-streams";

    private static final String PARALLELIZED_EXECUTION_PREFS_MACHINE_LOAD_PROPERTY_NAME = "machine-load";

    private static final String PARALLELIZED_EXECUTION_PREFS_MACHINE_MAX_THREADS_PROPERTY_NAME = "max-threads";

    private static final String PARALLELIZED_EXECUTION_PREFS_RETRIES_ON_FAILURE_PROPERTY_NAME = "retries-on-failure";

    private static final String PARALLELIZED_EXECUTION_PREFS_STOP_ON_FIRST_FAILURE_PROPERTY_NAME = "stop-on-first-failure";

    private static final Integer DEFAULT_FULL_SYNC_INTERVAL_IN_DAYS = 14;

    private static final String DEFAULT_LOG_FILE_PATH = "synchronization_{alias}.log";

    private static final String DEFAULT_LAST_SYNC_TIMEESTAMP_FILE_PATH = "last-sync-timestamp-file_{alias}.txt";

    private static final String DEFAULT_NOT_SYNCED_ENTITIES_FILE_PATH = "not-synced-entities_{alias}.txt";

    private static final double DEFAULT_MACHINE_LOAD = 0.5;

    private static final int DEFAULT_MAX_THREADS = 10;

    private static final int DEFAULT_RETRIES_ON_FAILURE = 0;

    private static final String LOG_FILE_PROPERTY_NAME = "log-file";

    public static List<SyncConfig> readConfiguration(File harvesterConfigFile) throws IOException
    {
        List<SyncConfig> configs = new ArrayList<>();
        ConfigReader reader = new ConfigReader(harvesterConfigFile);
        int sectionCount = reader.getSectionCount();
        for (int i = 0; i < sectionCount; i++)
        {
            String section = reader.getSection(i);
            SyncConfig config = new SyncConfig();
            config.setEmailAddresses(reader.getString(section, EMAIL_ADDRESSES_PROPERTY_NAME, null, true));
            config.setDataSourceAlias(reader.getString(section, DATA_SOURCE_ALIAS_PROPERTY_NAME, section, false));
            String defaultLogFilePath = DEFAULT_LOG_FILE_PATH.replaceFirst(Pattern.quote("{alias}"), config.getDataSourceAlias());
            config.setLogFilePath(reader.getString(section, LOG_FILE_PROPERTY_NAME, defaultLogFilePath, false)
                    + new SimpleDateFormat(".yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date()));
            config.setDataSourceURI(reader.getString(section, DATA_SOURCE_URL_PROPERTY_NAME, null, true));
            config.setDataSourceOpenbisURL(reader.getString(section, DATA_SOURCE_OPENBIS_URL_PROPERTY_NAME, null, true));
            config.setDataSourceDSSURL(reader.getString(section, DATA_SOURCE_DSS_URL_PROPERTY_NAME, null, true));
            String realm = reader.getString(section, DATA_SOURCE_AUTH_REALM_PROPERTY_NAME, null, true);
            String dataSourceUser = reader.getString(section, DATA_SOURCE_AUTH_USER_PROPERTY_NAME, null, true);
            String dataSourcePassword = reader.getString(section, DATA_SOURCE_AUTH_PASS_PROPERTY_NAME, null, true);
            config.setAuthCredentials(realm, dataSourceUser, dataSourcePassword);

            config.setHarvesterUser(reader.getString(section, HARVESTER_USER_PROPERTY_NAME, null, true));
            config.setHarvesterPass(reader.getString(section, HARVESTER_PASS_PROPERTY_NAME, null, true));

            config.setSpaceBlackList(reader.getStrings(section, SPACE_BLACK_LIST_PROPERTY_NAME, new ArrayList<>()));
            config.setSpaceWhiteList(reader.getStrings(section, SPACE_WHITE_LIST_PROPERTY_NAME, new ArrayList<>()));

            String dsSpaces = reader.getString(section, DATA_SOURCE_SPACES_PROPERTY_NAME, null, false);
            if (dsSpaces != null)
            {
                config.setDataSourceSpaces(dsSpaces);
            }
            String hrvSpaces = reader.getString(section, HARVESTER_SPACES_PROPERTY_NAME, null, false);
            if (hrvSpaces != null)
            {
                config.setHarvesterSpaces(hrvSpaces);
            }
            if (dsSpaces != null && hrvSpaces != null)
            {
                createDataSourceToHarvesterSpaceMappings(config);
            }

            config.setHarvesterTempDir(reader.getString(section, HARVESTER_TEMP_DIR_PROPERTY_NAME, DEFAULT_HARVESTER_TEMP_DIR, false));
            config.setFileServiceReporitoryPath(reader.getString(section, FILE_SERVICE_REPOSITORY_PATH_PROPERTY_NAME, null, true));
            config.setTranslateUsingDataSourceAlias(reader.getBoolean(section, TRANSLATE_USING_DATA_SOURCE_ALIAS_PROPERTY_NAME, true));

            // read full-sync configuration
            boolean fullSync = reader.getBoolean(section, FULL_SYNC_PROPERTY_NAME, false);
            config.setFullSyncEnabled(fullSync);
            if (fullSync)
            {
                config.setFullSyncInterval(reader.getInt(section, FULL_SYNC_INTERVAL_PROPERTY_NAME, DEFAULT_FULL_SYNC_INTERVAL_IN_DAYS, false));
            }

            String defaultLastSyncTimestampFilePath =
                    DEFAULT_LAST_SYNC_TIMEESTAMP_FILE_PATH.replaceFirst(Pattern.quote("{alias}"), config.getDataSourceAlias());
            config.setLastSyncTimestampFileName(
                    reader.getString(section, HARVESTER_LAST_SYNC_TIMESTAMP_FILE_PROPERTY_NAME, defaultLastSyncTimestampFilePath, false));

            String defaultNotSyncedEntitiesFilePath =
                    DEFAULT_NOT_SYNCED_ENTITIES_FILE_PATH.replaceFirst(Pattern.quote("{alias}"), config.getDataSourceAlias());
            config.setNotSyncedDataSetsFileName(
                    reader.getString(section, HARVESTER_NOT_SYNCED_ENTITIES_FILE_NAME, defaultNotSyncedEntitiesFilePath, false));
            configs.add(config);

            config.setDryRun(reader.getBoolean(section, DRY_RUN_PROPERTY_NAME, false));
            config.setVerbose(reader.getBoolean(section, VERBOSE_PROPERTY_NAME, true));
            if (config.isDryRun())
            {
                config.setVerbose(true);
            }
            config.setMasterDataUpdateAllowed(reader.getBoolean(section, MASTER_DATA_UPDATE_ALLOWED_PROPERTY_NAME, false));
            config.setDeletionAllowed(reader.getBoolean(section, DELETION_ALLOWED_PROPERTY_NAME, false));
            config.setPropertyUnassignmentAllowed(reader.getBoolean(section, PROPERTY_UNASSIGNMENT_ALLOWED_PROPERTY_NAME, false));
            config.setKeepOriginalTimestampsAndUsers(reader.getBoolean(section, KEEP_ORIGINAL_TIMESTAMPS_AND_USERS_PROPERTY_NAME, true));
            config.setKeepOriginalFrozenFlags(reader.getBoolean(section, KEEP_ORIGINAL_FROZEN_FLAGS_PROPERTY_NAME, true));
            config.setWishedNumberOfStreams(reader.getInt(section, WISHED_NUMBER_OF_STREAMS_PROPERTY_NAME, null, false));

            Double machineLoad = reader.getDouble(section, PARALLELIZED_EXECUTION_PREFS_MACHINE_LOAD_PROPERTY_NAME, DEFAULT_MACHINE_LOAD, false);
            Integer maxThreads =
                    reader.getInt(section, PARALLELIZED_EXECUTION_PREFS_MACHINE_MAX_THREADS_PROPERTY_NAME, DEFAULT_MAX_THREADS, false);
            Integer retriesOnFailure =
                    reader.getInt(section, PARALLELIZED_EXECUTION_PREFS_RETRIES_ON_FAILURE_PROPERTY_NAME, DEFAULT_RETRIES_ON_FAILURE, false);
            Boolean stopOnFailure =
                    reader.getBoolean(section, PARALLELIZED_EXECUTION_PREFS_STOP_ON_FIRST_FAILURE_PROPERTY_NAME, false);
            config.setParallelizedExecutionPrefs(machineLoad, maxThreads, retriesOnFailure, stopOnFailure);

        }
        return configs;
    }

    private static void createDataSourceToHarvesterSpaceMappings(SyncConfig config)
    {
        List<String> dataSourceSpaceList = config.getDataSourceSpaces();
        List<String> harvesterSpaceList = config.getHarvesterSpaces();
        if (dataSourceSpaceList.size() != harvesterSpaceList.size())
        {
            throw new ConfigurationFailureException("Please specify a harvester space for each data source space.");
        }
        for (int i = 0; i < dataSourceSpaceList.size(); i++)
        {
            String harvesterSpace = harvesterSpaceList.get(i);
            Space destSpace = ServiceProvider.getOpenBISService().tryGetSpace(new SpaceIdentifier(harvesterSpace));
            if (destSpace == null)
            {
                throw new ConfigurationFailureException("Space " + harvesterSpace + " does not exist");
            }
            config.getSpaceMappings().put(dataSourceSpaceList.get(i), harvesterSpace);
        }
    }
}
