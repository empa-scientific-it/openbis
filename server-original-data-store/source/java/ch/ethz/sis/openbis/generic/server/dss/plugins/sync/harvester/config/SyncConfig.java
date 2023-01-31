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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.common.mail.EMailAddress;

/**
 * @author Ganime Betul Akin
 */
public class SyncConfig
{
    private String dataSourceURI;

    public String getDataSourceURI()
    {
        return dataSourceURI;
    }

    public void setDataSourceURI(String dataSourceURI)
    {
        this.dataSourceURI = dataSourceURI;
    }

    public String getDataSourceOpenbisURL()
    {
        return dataSourceOpenbisURL;
    }

    public void setDataSourceOpenbisURL(String dataSourceOpenbisURL)
    {
        this.dataSourceOpenbisURL = dataSourceOpenbisURL;
    }

    public String getDataSourceDSSURL()
    {
        return dataSourceDSSURL;
    }

    public void setDataSourceDSSURL(String dataSourceDSSURL)
    {
        this.dataSourceDSSURL = dataSourceDSSURL;
    }

    public String getLastSyncTimestampFileName()
    {
        return lastSyncTimestampFileName;
    }

    public void setLastSyncTimestampFileName(String lastSyncTimestampFileName)
    {
        this.lastSyncTimestampFileName = lastSyncTimestampFileName;
    }

    public String getNotSyncedEntitiesFileName()
    {
        return notSyncedEntitiesFileName;
    }

    public void setNotSyncedDataSetsFileName(String notSyncedEntitiesFileName)
    {
        this.notSyncedEntitiesFileName = notSyncedEntitiesFileName;
    }

    public String getDataSourceAlias()
    {
        return dataSourceAlias;
    }

    public void setDataSourceAlias(String dataSourceAlias)
    {
        this.dataSourceAlias = dataSourceAlias;
    }

    public List<String> getDataSourceSpaces()
    {
        return dataSourceSpaces;
    }

    public void setDataSourceSpaces(String dataSourceSpaces)
    {
        if (dataSourceSpaces == null)
        {
            return;
        }
        for (String token : dataSourceSpaces.split(SEPARATOR))
        {
            this.dataSourceSpaces.add(token.trim());
        }
    }

    public List<String> getHarvesterSpaces()
    {
        return harvesterSpaces;
    }

    public void setHarvesterSpaces(String harvesterSpaces)
    {
        if (harvesterSpaces == null)
        {
            return;
        }
        for (String token : harvesterSpaces.split(SEPARATOR))
        {
            this.harvesterSpaces.add(token.trim());
        }
    }

    public String getFileServiceReporitoryPath()
    {
        return fileServiceReporitoryPath;
    }

    public void setFileServiceReporitoryPath(String fileServiceReporitoryPath)
    {
        this.fileServiceReporitoryPath = fileServiceReporitoryPath;
    }

    public String getHarvesterTempDir()
    {
        return harvesterTempDir;
    }

    public void setHarvesterTempDir(String harvesterTempDir)
    {
        this.harvesterTempDir = harvesterTempDir;
    }

    public String getHarvesterUser()
    {
        return harvesterUser;
    }

    public void setHarvesterUser(String harvesterUser)
    {
        this.harvesterUser = harvesterUser;
    }

    public String getHarvesterPass()
    {
        return harvesterPass;
    }

    public void setHarvesterPass(String harvesterPass)
    {
        this.harvesterPass = harvesterPass;
    }

    public void printConfig()
    {
        for (Field field : this.getClass().getDeclaredFields())
        {
            field.setAccessible(true);
            String name = field.getName();
            Object value;
            try
            {
                value = field.get(this);
                System.out.printf("%s : %s%n", name, value);
            } catch (IllegalArgumentException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // the credentials to be used in basic authentication against
    // the data source openbis
    private BasicAuthCredentials authCredentials;

    // Data sets and attachment downloads are parallelized
    private ParallelizedExecutionPreferences parallelizedExecutionPrefs;

    // the username/password pair to be used on the harvester side
    // to get the session token
    private String harvesterUser = "system";

    private String harvesterPass;

    private String dataSourceOpenbisURL;

    private String dataSourceDSSURL;

    private String lastSyncTimestampFileName;

    private String notSyncedEntitiesFileName;

    private String dataSourceAlias;

    private boolean translateUsingDataSourceAlias;

    private boolean fullSyncEnabled;;

    private Integer fullSyncInterval;

    private boolean dryRun;

    private boolean verbose;

    private boolean masterDataUpdateAllowed;

    private boolean propertyUnassignmentAllowed;

    private boolean deletionAllowed;

    private boolean keepOriginalTimestampsAndUsers;

    private boolean keepOriginalFrozenFlags;

    private List<String> dataSourceSpaces = new ArrayList<>();

    private List<String> harvesterSpaces = new ArrayList<>();

    private String harvesterTempDir;

    private String fileServiceReporitoryPath;

    private List<EMailAddress> emailAddresses = new ArrayList<>();

    private String logFilePath;

    private static final String SEPARATOR = ",";

    private HashMap<String, String> spaceMappings = new HashMap<String, String>();

    private Integer wishedNumberOfStreams;

    private List<String> spaceBlackList = new ArrayList<>();

    private List<String> spaceWhiteList = new ArrayList<>();

    public HashMap<String, String> getSpaceMappings()
    {
        return spaceMappings;
    }

    public String getLogFilePath()
    {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath)
    {
        this.logFilePath = logFilePath;
    }

    public List<EMailAddress> getEmailAddresses()
    {
        return emailAddresses;
    }

    public void setEmailAddresses(String emailAddresses)
    {
        for (String token : emailAddresses.split(SEPARATOR))
        {
            this.emailAddresses.add(new EMailAddress(token.trim()));
        }
    }

    public void setAuthCredentials(String realm, String user, String pass)
    {
        this.authCredentials = new BasicAuthCredentials(realm, user, pass);
    }

    public BasicAuthCredentials getAuthenticationCredentials()
    {
        return authCredentials;
    }

    public String getUser()
    {
        return this.authCredentials.getUser();
    }

    public String getPassword()
    {
        return this.authCredentials.getPassword();
    }

    public boolean isTranslateUsingDataSourceAlias()
    {
        return translateUsingDataSourceAlias;
    }

    public void setTranslateUsingDataSourceAlias(boolean translateUsingDataSourceAlias)
    {
        this.translateUsingDataSourceAlias = translateUsingDataSourceAlias;
    }

    public boolean isFullSyncEnabled()
    {
        return fullSyncEnabled;
    }

    public void setFullSyncEnabled(boolean fullSync)
    {
        this.fullSyncEnabled = fullSync;
    }

    public Integer getFullSyncInterval()
    {
        return fullSyncInterval;
    }

    public void setFullSyncInterval(Integer fullSyncInterval)
    {
        this.fullSyncInterval = fullSyncInterval;
    }

    public boolean isDryRun()
    {
        return dryRun;
    }

    public void setDryRun(boolean dryRun)
    {
        this.dryRun = dryRun;
    }

    public boolean isVerbose()
    {
        return verbose;
    }

    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    public boolean isMasterDataUpdateAllowed()
    {
        return masterDataUpdateAllowed;
    }

    public void setMasterDataUpdateAllowed(boolean masterDataUpdate)
    {
        this.masterDataUpdateAllowed = masterDataUpdate;
    }

    public boolean isPropertyUnassignmentAllowed()
    {
        return propertyUnassignmentAllowed;
    }

    public void setPropertyUnassignmentAllowed(boolean propertyUnassignmentAllowed)
    {
        this.propertyUnassignmentAllowed = propertyUnassignmentAllowed;
    }

    public boolean isDeletionAllowed()
    {
        return deletionAllowed;
    }

    public void setDeletionAllowed(boolean deletionAllowed)
    {
        this.deletionAllowed = deletionAllowed;
    }

    public boolean keepOriginalTimestampsAndUsers()
    {
        return keepOriginalTimestampsAndUsers;
    }

    public void setKeepOriginalTimestampsAndUsers(boolean keepOriginalTimestampsAndUsers)
    {
        this.keepOriginalTimestampsAndUsers = keepOriginalTimestampsAndUsers;
    }

    public boolean keepOriginalFrozenFlags()
    {
        return keepOriginalFrozenFlags;
    }

    public void setKeepOriginalFrozenFlags(boolean keepOriginalFrozenFlags)
    {
        this.keepOriginalFrozenFlags = keepOriginalFrozenFlags;
    }

    public void setParallelizedExecutionPrefs(double machineLoad, int maxThreads, int retriesOnFailure, boolean stopOnFailure)
    {
        this.parallelizedExecutionPrefs = new ParallelizedExecutionPreferences(machineLoad, maxThreads, retriesOnFailure, stopOnFailure);
    }

    public ParallelizedExecutionPreferences getParallelizedExecutionPrefs()
    {
        return parallelizedExecutionPrefs;
    }

    public Integer getWishedNumberOfStreams()
    {
        return wishedNumberOfStreams;
    }

    public void setWishedNumberOfStreams(Integer wishedNumberOfStreams)
    {
        this.wishedNumberOfStreams = wishedNumberOfStreams;
    }

    public List<String> getSpaceBlackList()
    {
        return spaceBlackList;
    }

    public void setSpaceBlackList(List<String> spaceBlackList)
    {
        this.spaceBlackList = spaceBlackList;
    }

    public List<String> getSpaceWhiteList()
    {
        return spaceWhiteList;
    }

    public void setSpaceWhiteList(List<String> spaceWhiteList)
    {
        this.spaceWhiteList = spaceWhiteList;
    }
}
