/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration.java;

import ch.systemsx.cisd.common.db.Script;

/**
 * A migration step executor.
 * 
 * @author Christian Ribeaud
 */
public interface IMigrationStepExecutor
{

    /**
     * Initializes the migration step executor with given <var>migrationScript</var>.
     * <p>
     * Is called just before {@link #performPreMigration()} and/or {@link #performPostMigration()}.
     * </p>
     */
    public void init(final Script migrationScript);

    /**
     * Performs some step after the migration has happened.
     */
    public void performPostMigration();

    /**
     * Performs some step before the migration has happened.
     */
    public void performPreMigration();

    /**
     * Resets the migration step executor.
     */
    public void finish();

}
