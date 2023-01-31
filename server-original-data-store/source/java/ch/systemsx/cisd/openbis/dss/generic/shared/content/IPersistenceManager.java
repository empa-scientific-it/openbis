/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.Serializable;

/**
 * Manages persistence of a statefull object.
 *
 * @author Franz-Josef Elmer
 */
public interface IPersistenceManager
{
    /**
     * Loads and returns the persistent object.
     * 
     * @param defaultObject will be returned if loading failed.
     */
    public Serializable load(Serializable defaultObject);

    /**
     * Requests for persisting the object. This can be done synchronously or asynchronously depending on the implementation.
     */
    public void requestPersistence();
}
