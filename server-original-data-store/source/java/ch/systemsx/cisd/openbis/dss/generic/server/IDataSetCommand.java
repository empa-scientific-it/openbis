/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;

/**
 * Interface of all commands operating on data sets in a data store.
 * 
 * @author Franz-Josef Elmer
 */
interface IDataSetCommand extends Serializable
{
    /**
     * Executes the command on files in the provided data set directories.
     */
    void execute(IHierarchicalContentProvider contentProvider,
            IDataSetDirectoryProvider dataSetDirectoryProvider);
    
    /**
     * Returns the type of this command.
     */
    String getType();

    /**
     * Returns a textual description of this command.
     */
    String getDescription();

    /**
     * Returns the codes of all data sets involved in this command.
     */
    List<String> getDataSetCodes();
}
