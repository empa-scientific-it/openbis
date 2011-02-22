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

package ch.systemsx.cisd.openbis.dss.generic.shared;

/**
 * Manager of share IDs.
 *
 * @author Franz-Josef Elmer
 */
public interface IShareIdManager
{
    /**
     * Returns current share id of specified data set.
     * 
     * @throws IllegalArgumentException if data set is unknown.
     */
    public String getShareId(String dataSetCode);
    
    /**
     * Sets to share id of specified data set. In case of a lock the method waits until lock
     * has been released.
     */
    public void setShareId(String dataSetCode, String shareId);
    
    /**
     * Locks specified data set.  
     * 
     * @throws IllegalArgumentException if data set is unknown.
     */
    public void lock(String dataSetCode);
    
    /**
     * Unlocks specified data set. Does nothing if lock already released or data set hasn't been
     * locked.
     */
    public void releaseLock(String dataSetCode);
    
    /**
     * Release all locks which have been requested in the same thread calling this method.
     */
    public void releaseLocks();
}
