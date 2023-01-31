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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
public interface IDataSetFileOperationsManager
{

    /**
     * Copies specified dataset's data to destination specified in constructor. The path at the destination is defined by the original location of the
     * data set.
     */
    public Status copyToDestination(File originalData, DatasetDescription dataset);

    /**
     * Retrieves specified dataset's data from the destination specified in constructor. The path at the destination is defined by original location
     * of the data set.
     */
    public Status retrieveFromDestination(File originalData, DatasetDescription dataset);

    /**
     * Deletes specified dataset's data from the destination specified in constructor. The path at the destination is defined by original location of
     * the data set.
     */
    public Status deleteFromDestination(IDatasetLocation dataset);

    /**
     * Marks the specified data set as deleted. Does not delete the dataset's data.
     */
    public Status markAsDeleted(IDatasetLocation dataset);

    /**
     * Checks if specified dataset's data are present and synchronized in the destination specified in constructor. The path at the destination is
     * defined by original location of the data set.
     */
    public BooleanStatus isSynchronizedWithDestination(File originalData,
            DatasetDescription dataset);

    /**
     * Checks if specified dataset's data are present in the destination specified in constructor. The path at the destination is defined by original
     * location of the data set.
     */
    public BooleanStatus isPresentInDestination(DatasetDescription dataset);

    /**
     * @return true if the destination includes a host information (it means it is not local/mounted)
     */
    public boolean isHosted();

    /**
     * @return the dataset file in the destination location as a hierarchical content.
     */
    public IHierarchicalContent getAsHierarchicalContent(DatasetDescription dataset);
}