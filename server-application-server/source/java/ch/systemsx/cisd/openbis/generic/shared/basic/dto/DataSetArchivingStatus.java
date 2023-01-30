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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Status of data set archiving process.
 * 
 * <pre>
 *                                                                                                                  |                                                         
 *                                                  
 *    -------> ARCHIVE_PENDING --------------> ARCHIVED 
 *    |                                            |             
 *    |                                            |  unarchive
 *    | archive(removeFromDS=true)                 |
 *    |                                            |  
 *    |                                            V             
 *    ----------------  AVAILABLE  <------- UNARCHIVE_PENDING
 *                      ^      ^  
 *                      |      |
 *                      |      |
 *                      |      | archive(removeFromDS=false)
 *                      |      |
 *                      |      |
 *  LOCKED <------------|      |-------------> BACKUP_PENDING
 * 
 * 
 * </pre>
 * 
 * @author Piotr Buczek
 */
public enum DataSetArchivingStatus implements IsSerializable
{
    AVAILABLE("AVAILABLE", true, true), // the data set is present in the data store only

    LOCKED("AVAILABLE (LOCKED)", true, true),

    ARCHIVED("ARCHIVED", false, true), // the data set is present in the archive only

    UNARCHIVE_PENDING("UNARCHIVE PENDING", false, false),

    ARCHIVE_PENDING("ARCHIVE PENDING", false, false),

    BACKUP_PENDING("BACKUP_PENDING", true, false);

    private final String description;

    private final boolean available;

    private final boolean deletable;

    DataSetArchivingStatus(String description, boolean available, boolean deletable)
    {
        this.description = description;
        this.available = available;
        this.deletable = deletable;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isAvailable()
    {
        return available;
    }

    /**
     * return <code>true</code> if users are allowed to delete datasets with this status.
     */
    public boolean isDeletable()
    {
        return deletable;
    }

}
