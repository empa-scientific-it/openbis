/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.file;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * An abstraction of a file that allows to store and retrieve lines.
 * 
 * @author Bernd Rinn
 */
interface ILineStore
{

    /**
     * Returns a unique identifier for this line store.
     */
    String getId();

    /**
     * Checks whether the store is operational.
     * <p>
     * Supposed to be called at program start up.
     * 
     * @throws ConfigurationFailureException If this store is not operational.
     */
    void check() throws ConfigurationFailureException;

    /**
     * Returns the lines currently in this store. If the store does not yet exist, return an empty list.
     */
    List<String> readLines() throws EnvironmentFailureException;

    /**
     * Writes the <var>lines</var> to the store.
     */
    void writeLines(List<String> lines) throws EnvironmentFailureException;

    /**
     * Returns <code>true</code> if the lines store has changed on disk since the last time it was read or written.
     */
    boolean hasChanged();
}
