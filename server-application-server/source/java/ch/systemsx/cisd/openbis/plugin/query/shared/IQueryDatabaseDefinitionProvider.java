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

package ch.systemsx.cisd.openbis.plugin.query.shared;

import java.util.Collection;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Provides definition of a database that can be used by custom queries module.
 * <p>
 * NOTE: {{@link #initDatabaseDefinitions()} should be called before any other method invocations.
 * 
 * @author Piotr Buczek
 */
public interface IQueryDatabaseDefinitionProvider
{
    /** Initializes definitions of databases configured for the module. */
    void initDatabaseDefinitions();

    /**
     * Returns {@link DatabaseDefinition} configured for given <var>dbKey</var> or null if such a definition doesn't exist.
     * 
     * @throws UserFailureException if this provider wasn't initialized before
     */
    DatabaseDefinition getDefinition(String dbKey) throws UserFailureException;

    /**
     * Returns a collection of all {@link DatabaseDefinition}s configured.
     * 
     * @throws UserFailureException if this provider wasn't initialized before
     */
    Collection<DatabaseDefinition> getAllDefinitions() throws UserFailureException;
}
