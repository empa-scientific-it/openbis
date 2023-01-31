/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.recovery;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Simple data type with information about recovery state. - the file - time of last recovery try - total count of tries to recover
 * 
 * @author jakubs
 */
public class DataSetStorageRecoveryInfo implements Serializable
{
    public enum RecoveryStage
    {
        /**
         * After precommit, before the registration in as
         */
        PRECOMMIT("Precommit"),

        /**
         * After the registration has succeeded and post-registration hook executed
         */
        POST_REGISTRATION_HOOK_EXECUTED("After post-registration"),
        /**
         * All files have been moved to the store, but application server has not been informed yet
         */
        STORAGE_COMPLETED("Storage completed");

        private String description;

        private RecoveryStage(String description)
        {
            this.description = description;
        }

        /**
         * @return true if this stage is before or equal other
         */
        public boolean beforeOrEqual(RecoveryStage other)
        {
            return this.ordinal() <= other.ordinal();
        }

        /**
         * @return true if this stage is before other
         */
        public boolean before(RecoveryStage other)
        {
            return this.ordinal() < other.ordinal();
        }

        @Override
        public String toString()
        {
            return description;
        }
    }

    private static final long serialVersionUID = 1L;

    private final File recoveryStateFile;

    private Date lastTry;

    private int tryCount;

    private RecoveryStage recoveryStage;

    public DataSetStorageRecoveryInfo(File recoveryStateFile, Date lastTry, int tryCount,
            RecoveryStage recoveryStage)
    {
        super();
        this.recoveryStateFile = recoveryStateFile;
        this.lastTry = lastTry;
        this.tryCount = tryCount;
        this.recoveryStage = recoveryStage;
    }

    public void increaseTryCount()
    {
        tryCount++;
    }

    public void setLastTry(Date lastTry)
    {
        this.lastTry = lastTry;
    }

    public File getRecoveryStateFile()
    {
        return recoveryStateFile;
    }

    public Date getLastTry()
    {
        return lastTry;
    }

    public int getTryCount()
    {
        return tryCount;
    }

    public RecoveryStage getRecoveryStage()
    {
        return recoveryStage;
    }

    public void writeToFile(File informationFile)
    {
        FileUtilities.writeToFile(informationFile, this);
    }

    public static DataSetStorageRecoveryInfo loadFromFile(File informationFile)
    {
        return FileUtilities.loadToObject(informationFile, DataSetStorageRecoveryInfo.class);
    }
}
